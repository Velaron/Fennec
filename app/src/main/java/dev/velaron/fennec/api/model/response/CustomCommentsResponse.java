package dev.velaron.fennec.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.velaron.fennec.api.model.VKApiComment;
import dev.velaron.fennec.api.model.VKApiCommunity;
import dev.velaron.fennec.api.model.VKApiPoll;
import dev.velaron.fennec.api.model.VKApiUser;

/**
 * Created by admin on 28.12.2016.
 * phoenix
 */
public class CustomCommentsResponse {

    // Parse manually in CustomCommentsResponseAdapter

    public Main main;

    public Integer firstId;

    public Integer lastId;

    public Integer admin_level;

    public static class Main {

        @SerializedName("count")
        public int count;

        @SerializedName("items")
        public List<VKApiComment> comments;

        @SerializedName("profiles")
        public List<VKApiUser> profiles;

        @SerializedName("groups")
        public List<VKApiCommunity> groups;

        @SerializedName("poll")
        public VKApiPoll poll;
    }

}
