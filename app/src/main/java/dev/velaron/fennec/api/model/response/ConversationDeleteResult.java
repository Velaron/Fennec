package dev.velaron.fennec.api.model.response;

import com.google.gson.annotations.SerializedName;

public class ConversationDeleteResult {
    @SerializedName("last_deleted_id")
    public int lastDeletedId;
}