package dev.velaron.fennec.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.velaron.fennec.api.model.VKApiCommunity;
import dev.velaron.fennec.api.model.VKApiPhoto;
import dev.velaron.fennec.api.model.VKApiPost;
import dev.velaron.fennec.api.model.VKApiTopic;
import dev.velaron.fennec.api.model.VKApiUser;
import dev.velaron.fennec.api.model.VKApiVideo;

/**
 * Created by admin on 07.05.2017.
 * phoenix
 */
public class NewsfeedCommentsResponse {

    @SerializedName("items")
    public List<Dto> items;

    @SerializedName("profiles")
    public List<VKApiUser> profiles;

    @SerializedName("groups")
    public List<VKApiCommunity> groups;

    @SerializedName("next_from")
    public String nextFrom;

    public static abstract class Dto {

    }

    public static class PostDto extends Dto {

        public final VKApiPost post;

        public PostDto(VKApiPost post) {
            this.post = post;
        }
    }

    public static class PhotoDto extends Dto {

        public final VKApiPhoto photo;

        public PhotoDto(VKApiPhoto photo) {
            this.photo = photo;
        }
    }

    public static class VideoDto extends Dto {

        public final VKApiVideo video;

        public VideoDto(VKApiVideo video) {
            this.video = video;
        }
    }

    public static class TopicDto extends Dto {

        public final VKApiTopic topic;

        public TopicDto(VKApiTopic topic) {
            this.topic = topic;
        }
    }
}