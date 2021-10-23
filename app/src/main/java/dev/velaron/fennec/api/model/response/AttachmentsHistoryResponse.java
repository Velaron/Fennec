package dev.velaron.fennec.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.velaron.fennec.api.model.VkApiAttachments;

public class AttachmentsHistoryResponse {

    @SerializedName("items")
    public List<One> items;

    @SerializedName("next_from")
    public String next_from;

    public static class One {

        @SerializedName("message_id")
        public int messageId;

        @SerializedName("attachment")
        public VkApiAttachments.Entry entry;
    }
}
