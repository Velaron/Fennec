package dev.velaron.fennec.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.velaron.fennec.api.model.VKApiCommunity;
import dev.velaron.fennec.api.model.VKApiNews;
import dev.velaron.fennec.api.model.VKApiUser;

/**
 * Created by admin on 27.12.2016.
 * phoenix
 */
public class NewsfeedResponse {

    @SerializedName("items")
    public List<VKApiNews> items;

    @SerializedName("profiles")
    public List<VKApiUser> profiles;

    @SerializedName("groups")
    public List<VKApiCommunity> groups;

    @SerializedName("next_from")
    public String nextFrom;

}
