package dev.velaron.fennec.place;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.net.Uri;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import dev.velaron.fennec.R;
import dev.velaron.fennec.domain.IOwnersRepository;
import dev.velaron.fennec.domain.Repository;
import dev.velaron.fennec.model.AbsModel;
import dev.velaron.fennec.model.EditingPostType;
import dev.velaron.fennec.model.Post;
import dev.velaron.fennec.model.WallEditorAttrs;
import dev.velaron.fennec.util.RxUtils;
import dev.velaron.fennec.util.Utils;
import io.reactivex.disposables.Disposable;

/**
 * Created by admin on 03.06.2017.
 * phoenix
 */
public class PlaceUtil {

    public static void goToPostEditor(@NonNull Activity activity, final int accountId, final Post post) {
        ProgressDialog dialog = createProgressDialog(activity);
        WeakReference<Dialog> dialogWeakReference = new WeakReference<>(dialog);
        WeakReference<Activity> reference = new WeakReference<>(activity);

        final int ownerId = post.getOwnerId();

        Set<Integer> ids = new HashSet<>();
        ids.add(accountId);
        ids.add(ownerId);

        Disposable disposable = Repository.INSTANCE.getOwners()
                .findBaseOwnersDataAsBundle(accountId, ids, IOwnersRepository.MODE_NET)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(owners -> {
                    WallEditorAttrs attrs = new WallEditorAttrs(owners.getById(ownerId), owners.getById(accountId));

                    Dialog d = dialogWeakReference.get();
                    if (d != null) {
                        d.dismiss();
                    }

                    Activity a = reference.get();

                    if (a != null) {
                        PlaceFactory.getEditPostPlace(accountId, post, attrs).tryOpenWith(a);
                    }
                }, throwable -> safelyShowError(reference, throwable));

        dialog.setOnCancelListener(d -> disposable.dispose());
    }

    private static void safelyShowError(WeakReference<Activity> reference, Throwable throwable) {
        Activity a = reference.get();
        if (a != null) {
            new MaterialAlertDialogBuilder(a)
                    .setTitle(R.string.error)
                    .setMessage(Utils.getCauseIfRuntime(throwable).getMessage())
                    .setPositiveButton(R.string.button_ok, null)
                    .show();
        }
    }

    public static void goToPostCreation(@NonNull Activity activity, int accountId, int ownerId,
                                        @EditingPostType int editingType, @Nullable List<AbsModel> input) {
        goToPostCreation(activity, accountId, ownerId, editingType, input, null, null);
    }

    public static void goToPostCreation(@NonNull Activity activity, int accountId, int ownerId,
                                        @EditingPostType int editingType, @Nullable List<AbsModel> input, @Nullable ArrayList<Uri> streams, @Nullable String body) {

        ProgressDialog dialog = createProgressDialog(activity);
        WeakReference<Dialog> dialogWeakReference = new WeakReference<>(dialog);
        WeakReference<Activity> reference = new WeakReference<>(activity);

        Set<Integer> ids = new HashSet<>();
        ids.add(accountId);
        ids.add(ownerId);

        Disposable disposable = Repository.INSTANCE.getOwners()
                .findBaseOwnersDataAsBundle(accountId, ids, IOwnersRepository.MODE_NET)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(owners -> {
                    WallEditorAttrs attrs = new WallEditorAttrs(owners.getById(ownerId), owners.getById(accountId));

                    Dialog d = dialogWeakReference.get();
                    if (d != null) {
                        d.dismiss();
                    }

                    Activity a = reference.get();
                    if (a != null) {
                        PlaceFactory.getCreatePostPlace(accountId, ownerId, editingType, input, attrs, streams, body).tryOpenWith(a);
                    }
                }, throwable -> safelyShowError(reference, throwable));

        dialog.setOnCancelListener(d -> disposable.dispose());
    }

    private static ProgressDialog createProgressDialog(Activity activity) {
        ProgressDialog dialog = new ProgressDialog(activity);
        dialog.setTitle(R.string.please_wait);
        dialog.setMessage(activity.getString(R.string.message_obtaining_owner_information));
        dialog.setCancelable(true);
        dialog.show();
        return dialog;
    }
}