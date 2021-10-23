package dev.velaron.fennec.api.model.response;

import java.util.List;

import dev.velaron.fennec.api.model.VKApiChat;
import dev.velaron.fennec.api.model.VKApiCommunity;
import dev.velaron.fennec.api.model.VKApiUser;

/**
 * Created by admin on 02.05.2017.
 * phoenix
 */
public class SearchDialogsResponse {

    private List<AbsChattable> data;

    public abstract static class AbsChattable {

    }

    public SearchDialogsResponse setData(List<AbsChattable> data) {
        this.data = data;
        return this;
    }

    public List<AbsChattable> getData() {
        return data;
    }

    public static class Chat extends AbsChattable {

        private final VKApiChat chat;

        public Chat(VKApiChat chat) {
            this.chat = chat;
        }

        public VKApiChat getChat() {
            return chat;
        }
    }

    public static class User extends AbsChattable {

        private final VKApiUser user;

        public User(VKApiUser user) {
            this.user = user;
        }

        public VKApiUser getUser() {
            return user;
        }
    }

    public static class Community extends AbsChattable {

        private final VKApiCommunity community;

        public Community(VKApiCommunity community) {
            this.community = community;
        }

        public VKApiCommunity getCommunity() {
            return community;
        }
    }
}
