package dev.velaron.fennec.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.velaron.fennec.api.model.VKApiUser;

/**
 * Created by admin on 09.01.2017.
 * phoenix
 */
public class OnlineFriendsResponse {

    @SerializedName("uids")
    public int[] uids;

    @SerializedName("profiles")
    public List<VKApiUser> profiles;

}
