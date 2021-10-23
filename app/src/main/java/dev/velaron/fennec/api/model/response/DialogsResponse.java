package dev.velaron.fennec.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.velaron.fennec.api.model.VKApiCommunity;
import dev.velaron.fennec.api.model.VKApiUser;
import dev.velaron.fennec.api.model.VkApiDialog;

/**
 * Created by ruslan.kolbasa on 28.12.2016.
 * phoenix
 */
public class DialogsResponse {

    @SerializedName("items")
    public List<VkApiDialog> dialogs;

    @SerializedName("count")
    public int count;

    @SerializedName("unread_count")
    public int unreadCount;

    @SerializedName("profiles")
    public List<VKApiUser> profiles;

    @SerializedName("groups")
    public List<VKApiCommunity> groups;
}