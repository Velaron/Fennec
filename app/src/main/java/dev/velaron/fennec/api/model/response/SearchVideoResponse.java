package dev.velaron.fennec.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.velaron.fennec.api.model.VKApiCommunity;
import dev.velaron.fennec.api.model.VKApiUser;
import dev.velaron.fennec.api.model.VKApiVideo;

/**
 * Created by admin on 03.01.2017.
 * phoenix
 */
public class SearchVideoResponse {

    @SerializedName("count")
    public int count;

    @SerializedName("items")
    public List<VKApiVideo> items;

    @SerializedName("profiles")
    public List<VKApiUser> profiles;

    @SerializedName("groups")
    public List<VKApiCommunity> groups;
}
