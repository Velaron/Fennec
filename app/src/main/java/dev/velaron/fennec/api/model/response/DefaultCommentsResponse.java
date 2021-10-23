package dev.velaron.fennec.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.velaron.fennec.api.model.VKApiComment;
import dev.velaron.fennec.api.model.VKApiCommunity;
import dev.velaron.fennec.api.model.VKApiUser;

/**
 * Created by Ruslan Kolbasa on 08.06.2017.
 * phoenix
 */
public class DefaultCommentsResponse {

    @SerializedName("count")
    public int count;

    @SerializedName("items")
    public List<VKApiComment> items;

    @SerializedName("groups")
    public List<VKApiCommunity> groups;

    @SerializedName("profiles")
    public List<VKApiUser> profiles;

}
