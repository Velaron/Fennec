package dev.velaron.fennec.api.model.response;

import com.google.gson.annotations.SerializedName;

import dev.velaron.fennec.api.model.Items;
import dev.velaron.fennec.api.model.VKApiUser;

/**
 * Created by admin on 09.01.2017.
 * phoenix
 */
public class FriendsWithCountersResponse {

    @SerializedName("friends")
    public Items<VKApiUser> friends;

    @SerializedName("counters")
    public VKApiUser.Counters counters;
}
