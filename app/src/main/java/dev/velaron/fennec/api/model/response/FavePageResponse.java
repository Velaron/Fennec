package dev.velaron.fennec.api.model.response;

import com.google.gson.annotations.SerializedName;

import dev.velaron.fennec.api.model.VKApiCommunity;
import dev.velaron.fennec.api.model.VKApiUser;

public class FavePageResponse {

    @SerializedName("description")
    public String description;

    @SerializedName("type")
    public String type;

    @SerializedName("updated_date")
    public long updated_date;

    @SerializedName("user")
    public VKApiUser user;

    @SerializedName("group")
    public VKApiCommunity group;
}
