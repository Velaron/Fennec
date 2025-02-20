package dev.velaron.fennec.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.velaron.fennec.api.model.VKApiCommunity;
import dev.velaron.fennec.api.model.VKApiUser;
import dev.velaron.fennec.api.model.feedback.VkApiBaseFeedback;

/**
 * Created by ruslan.kolbasa on 28.12.2016.
 * phoenix
 */
public class NotificationsResponse {

    @SerializedName("count")
    public int count;

    @SerializedName("items")
    public List<VkApiBaseFeedback> notifications;

    @SerializedName("profiles")
    public List<VKApiUser> profiles;

    @SerializedName("groups")
    public List<VKApiCommunity> groups;

    @SerializedName("next_from")
    public String nextFrom;

    @SerializedName("last_viewed")
    public long lastViewed;
}
