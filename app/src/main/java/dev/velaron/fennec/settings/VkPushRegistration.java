package dev.velaron.fennec.settings;

import com.google.gson.annotations.SerializedName;

import androidx.annotation.NonNull;

public class VkPushRegistration {

    @SerializedName("userId")
    private final int userId;

    @SerializedName("deviceId")
    private final String deviceId;

    @SerializedName("vkToken")
    private final String vkToken;

    @SerializedName("gmcToken")
    private final String gmcToken;

    public VkPushRegistration(int userId, @NonNull String deviceId, @NonNull String vkToken, @NonNull String gmcToken) {
        this.userId = userId;
        this.deviceId = deviceId;
        this.vkToken = vkToken;
        this.gmcToken = gmcToken;
    }

    public int getUserId() {
        return userId;
    }

    public String getVkToken() {
        return vkToken;
    }

    public String getGmcToken() {
        return gmcToken;
    }

    public String getDeviceId() {
        return deviceId;
    }
}