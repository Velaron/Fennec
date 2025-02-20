package dev.velaron.fennec.mvp.presenter;

import static dev.velaron.fennec.util.Utils.isEmpty;
import static dev.velaron.fennec.util.Utils.join;
import static dev.velaron.fennec.util.Utils.nonEmpty;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.velaron.fennec.mvp.reflect.OnGuiCreated;
import dev.velaron.fennec.R;
import dev.velaron.fennec.api.Apis;
import dev.velaron.fennec.api.interfaces.INetworker;
import dev.velaron.fennec.mvp.presenter.base.AccountDependencyPresenter;
import dev.velaron.fennec.mvp.view.IRequestExecuteView;
import dev.velaron.fennec.util.AppPerms;
import dev.velaron.fennec.util.Objects;
import dev.velaron.fennec.util.Pair;
import dev.velaron.fennec.util.RxUtils;
import dev.velaron.fennec.util.Utils;
import io.reactivex.Single;

/**
 * Created by Ruslan Kolbasa on 05.07.2017.
 * phoenix
 */
public class RequestExecutePresenter extends AccountDependencyPresenter<IRequestExecuteView> {

    private String body;
    private String method;
    private final INetworker networker;

    public RequestExecutePresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.networker = Apis.get();
    }

    private void executeRequest() {
        final String trimmedMethod = nonEmpty(method) ? method.trim() : null;
        final String trimmedBody = nonEmpty(body) ? body.trim() : null;

        if (isEmpty(trimmedMethod)) {
            showError(getView(), new Exception("Method can't be empty"));
            return;
        }

        final int accountId = super.getAccountId();

        Map<String, String> params = new HashMap<>();

        if (nonEmpty(trimmedBody)) {
            try {
                String lines[] = trimmedBody.split("\\r?\\n");

                for (String line : lines) {
                    String[] parts = line.split("=");
                    String name = parts[0].toLowerCase();
                    String value = parts[1];

                    params.put(name, value);
                }
            } catch (Exception e) {
                showError(getView(), e);
                return;
            }
        }

        setLoadinNow(true);

        appendDisposable(executeSingle(accountId, trimmedMethod, params)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onRequestResponse, throwable -> onRequestError(Utils.getCauseIfRuntime(throwable))));
    }

    private boolean hasWritePermission() {
        return AppPerms.hasWriteStoragePermision(getApplicationContext());
    }

    private void saveToFile() {
        if (!hasWritePermission()) {
            getView().requestWriteExternalStoragePermission();
            return;
        }

        FileOutputStream out = null;

        try {
            final String filename = this.method + ".txt";

            final File file = new File(Environment.getExternalStorageDirectory(), filename);
            file.delete();

            byte[] bytes = fullResponseBody.getBytes(Charset.forName("UTF-8"));

            out = new FileOutputStream(file);
            out.write(bytes);
            out.flush();

            getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));

            safeShowToast(getView(), R.string.saved_to_param_file_name, false, file.getAbsolutePath());
        } catch (Exception e) {
            showError(getView(), e);
        } finally {
            Utils.safelyClose(out);
        }
    }

    @Override
    public void onGuiCreated(@NonNull IRequestExecuteView view) {
        super.onGuiCreated(view);
        view.displayBody(trimmedReposenBody);
    }

    private String fullResponseBody;
    private String trimmedReposenBody;

    private void onRequestResponse(Pair<String, String> body) {
        setLoadinNow(false);

        this.fullResponseBody = body.getFirst();
        this.trimmedReposenBody = body.getSecond();

        callView(view -> view.displayBody(trimmedReposenBody));
    }

    private void onRequestError(Throwable throwable) {
        setLoadinNow(false);
        showError(getView(), throwable);
    }

    private boolean loadinNow;

    private void setLoadinNow(boolean loadinNow) {
        this.loadinNow = loadinNow;
        resolveProgresDialog();
    }

    @OnGuiCreated
    private void resolveProgresDialog() {
        if (isGuiReady()) {
            if (loadinNow) {
                getView().displayProgressDialog(R.string.please_wait, R.string.waiting_for_response_message, false);
            } else {
                getView().dismissProgressDialog();
            }
        }
    }

    /**
     * Convert a JSON string to pretty print version
     */
    private static String toPrettyFormat(String jsonString) {
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(jsonString).getAsJsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(json);
    }

    private Single<Pair<String, String>> executeSingle(final int accountId, final String method, final Map<String, String> params) {
        return networker.vkDefault(accountId)
                .other()
                .rawRequest(method, params)
                .map(optional -> {
                    String responseString = optional.get();

                    String fullJson = Objects.isNull(responseString) ? null : toPrettyFormat(responseString);

                    String trimmedJson = null;

                    if (nonEmpty(fullJson)) {
                        String lines[] = fullJson.split("\\r?\\n");

                        List<String> trimmed = new ArrayList<>();

                        for (String line : lines) {
                            if (trimmed.size() > 50) {
                                trimmed.add("\n");
                                trimmed.add("... and more " + (lines.length - 50) + " lines");
                                break;
                            }

                            trimmed.add(line);
                        }

                        trimmedJson = join("\n", trimmed);
                    }

                    return Pair.Companion.create(fullJson, trimmedJson);
                });
    }

    public void fireSaveClick() {
        saveToFile();
    }

    public void fireWritePermissionResolved() {
        if (hasWritePermission()) {
            saveToFile();
        }
    }

    public void fireExecuteClick() {
        getView().hideKeyboard();

        executeRequest();
    }

    public void fireMethodEdit(CharSequence s) {
        method = s.toString();
    }

    public void fireBodyEdit(CharSequence s) {
        body = s.toString();
    }

    public void fireCopyClick() {
        ClipboardManager clipboard = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("response", fullResponseBody);
        clipboard.setPrimaryClip(clip);

        safeShowToast(getView(), R.string.copied_to_clipboard, false);
    }
}