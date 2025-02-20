package dev.velaron.fennec.settings;

import android.content.Context;
import android.preference.PreferenceManager;

import dev.velaron.fennec.util.Objects;

/**
 * Created by ruslan.kolbasa on 02.12.2016.
 * phoenix
 */
class OtherSettings implements ISettings.IOtherSettings {

    private static final String KEY_JSON_STATE = "json_list_state";

    private final Context app;

    OtherSettings(Context context) {
        this.app = context.getApplicationContext();
    }

    @Override
    public String getFeedSourceIds(int accountId) {
        return PreferenceManager.getDefaultSharedPreferences(app)
                .getString("source_ids" + accountId, null);
    }

    @Override
    public void setFeedSourceIds(int accountId, String sourceIds) {
        PreferenceManager.getDefaultSharedPreferences(app)
                .edit()
                .putString("source_ids" + accountId, sourceIds)
                .apply();
    }

    @Override
    public void storeFeedScrollState(int accountId, String state) {
        if(Objects.nonNull(state)){
            PreferenceManager
                    .getDefaultSharedPreferences(app)
                    .edit()
                    .putString("json_list_state" + accountId, state)
                    .apply();
        } else {
            PreferenceManager
                    .getDefaultSharedPreferences(app)
                    .edit()
                    .remove(KEY_JSON_STATE + accountId)
                    .apply();
        }
    }

    @Override
    public String restoreFeedScrollState(int accountId) {
        return PreferenceManager.getDefaultSharedPreferences(app).getString(KEY_JSON_STATE + accountId, null);
    }

    @Override
    public String restoreFeedNextFrom(int accountId) {
        return PreferenceManager
                .getDefaultSharedPreferences(app)
                .getString("next_from" + accountId, null);
    }

    @Override
    public void storeFeedNextFrom(int accountId, String nextFrom) {
        PreferenceManager.getDefaultSharedPreferences(app)
                .edit()
                .putString("next_from" + accountId, nextFrom)
                .apply();
    }

    @Override
    public boolean isAudioBroadcastActive() {
        return PreferenceManager.getDefaultSharedPreferences(app).getBoolean("broadcast", false);
    }

    @Override
    public void setAudioBroadcastActive(boolean active) {
        PreferenceManager.getDefaultSharedPreferences(app)
                .edit()
                .putBoolean("broadcast", active)
                .apply();
    }

    @Override
    public boolean isCommentsDesc() {
        return PreferenceManager.getDefaultSharedPreferences(app).getBoolean("comments_desc", true);
    }

    @Override
    public boolean toggleCommentsDirection() {
        boolean descNow = isCommentsDesc();

        PreferenceManager.getDefaultSharedPreferences(app)
                .edit()
                .putBoolean("comments_desc", !descNow)
                .apply();

        return !descNow;
    }

    @Override
    public String getOauthDomain() {
        return PreferenceManager.getDefaultSharedPreferences(app).getString("oauth_domain", "https://vk-oauth-proxy.xtrafrancyz.net/");
    }

    @Override
    public String getApiDomain() {
        return PreferenceManager.getDefaultSharedPreferences(app).getString("api_domain", "https://vk-api-proxy.xtrafrancyz.net/method/");
    }

    @Override
    public boolean isKeepLongpoll() {
        return PreferenceManager.getDefaultSharedPreferences(app).getBoolean("keep_longpoll", false);
    }

    @Override
    public boolean isForceExoplayer() {
        return PreferenceManager.getDefaultSharedPreferences(app).getBoolean("force_exoplayer", false);
    }
}