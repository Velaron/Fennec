package dev.velaron.fennec.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import dev.velaron.fennec.Injection;
import dev.velaron.fennec.push.IPushRegistrationResolver;
import dev.velaron.fennec.util.RxUtils;
import io.reactivex.Flowable;
import io.reactivex.processors.PublishProcessor;

import static dev.velaron.fennec.util.Objects.nonNull;
import static dev.velaron.fennec.util.Utils.nonEmpty;

/**
 * Created by ruslan.kolbasa on 02.12.2016.
 * phoenix
 */
class AccountsSettings implements ISettings.IAccountsSettings {

    private static final String KEY_ACCOUNT_UIDS = "account_uids";
    private static final String KEY_CURRENT = "current_account_id";

    private final Context app;

    private SharedPreferences preferences;
    private Map<Integer, String> tokens;

    @SuppressLint("UseSparseArrays")
    AccountsSettings(Context context) {
        this.app = context.getApplicationContext();
        this.tokens = Collections.synchronizedMap(new HashMap<>(1));
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);

        Collection<Integer> aids = getRegistered();
        for (Integer aid : aids) {
            String token = preferences.getString(tokenKeyFor(aid), null);

            if (nonEmpty(token)) {
                tokens.put(aid, token);
            }
        }
    }

    private static String tokenKeyFor(int uid) {
        return "token" + uid;
    }

    private final PublishProcessor<ISettings.IAccountsSettings> changesPublisher = PublishProcessor.create();

    private void notifyAboutRegisteredChanges(){
        changesPublisher.onNext(this);
    }

    @Override
    public Flowable<ISettings.IAccountsSettings> observeRegistered() {
        return changesPublisher.onBackpressureBuffer();
    }

    private PublishProcessor<Integer> currentPublisher = PublishProcessor.create();

    @Override
    public Flowable<Integer> observeChanges() {
        return currentPublisher.onBackpressureBuffer();
    }

    @NonNull
    @Override
    public List<Integer> getRegistered() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(app);
        Set<String> uids = preferences.getStringSet(KEY_ACCOUNT_UIDS, new HashSet<>(0));

        List<Integer> ids = new ArrayList<>(uids.size());
        for (String stringuid : uids) {
            int uid = Integer.parseInt(stringuid);
            ids.add(uid);
        }

        return ids;
    }

    @Override
    public void setCurrent(int accountId) {
        if (getCurrent() == accountId) return;

        PreferenceManager.getDefaultSharedPreferences(app)
                .edit()
                .putInt(KEY_CURRENT, accountId)
                .apply();
        fireAccountChange();
    }

    private void fireAccountChange() {
        final IPushRegistrationResolver registrationResolver = Injection.providePushRegistrationResolver();
        registrationResolver.resolvePushRegistration()
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(RxUtils.dummy(), RxUtils.ignore());

        currentPublisher.onNext(getCurrent());
    }

    @Override
    public int getCurrent() {
        return preferences.getInt(KEY_CURRENT, INVALID_ID);
    }

    @NonNull
    private Set<String> copyUidsSet() {
        return new HashSet<>(preferences.getStringSet(KEY_ACCOUNT_UIDS, new HashSet<>(1)));
    }

    @Override
    public void remove(int accountId) {
        int currentAccountId = getCurrent();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(app);
        Set<String> uids = copyUidsSet();

        uids.remove(String.valueOf(accountId));
        preferences.edit()
                .putStringSet(KEY_ACCOUNT_UIDS, uids)
                .apply();

        if (accountId == currentAccountId) {
            List<Integer> accountIds = getRegistered();

            Integer fisrtUserAccountId = null;

            // делаем активным первый аккаунт ПОЛЬЗОВАТЕЛЯ
            for (Integer existsId : accountIds) {
                if (existsId > 0) {
                    fisrtUserAccountId = existsId;
                    break;
                }
            }

            if (nonNull(fisrtUserAccountId)) {
                preferences.edit()
                        .putInt(KEY_CURRENT, fisrtUserAccountId)
                        .apply();
            } else {
                preferences.edit()
                        .remove(KEY_CURRENT)
                        .apply();
            }
        }

        notifyAboutRegisteredChanges();
        fireAccountChange();
    }

    @Override
    public void registerAccountId(int accountId, boolean setCurrent) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(app);

        Set<String> uids = copyUidsSet();
        uids.add(String.valueOf(accountId));

        SharedPreferences.Editor editor = preferences.edit();

        editor.putStringSet(KEY_ACCOUNT_UIDS, uids);

        if (setCurrent) {
            editor.putInt(KEY_CURRENT, accountId);
        }

        editor.apply();

        notifyAboutRegisteredChanges();

        if (setCurrent) {
            fireAccountChange();
        }
    }

    @Override
    public void storeAccessToken(int accountId, String accessToken) {
        tokens.put(accountId, accessToken);
        preferences.edit()
                .putString(tokenKeyFor(accountId), accessToken)
                .apply();
    }

    @Override
    public String getAccessToken(int accountId) {
        return tokens.get(accountId);
    }

    @Override
    public void removeAccessToken(int accountId) {
        tokens.remove(accountId);
        preferences.edit()
                .remove(tokenKeyFor(accountId))
                .apply();
    }
}