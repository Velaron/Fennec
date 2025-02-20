package dev.velaron.fennec.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.velaron.fennec.api.model.Error;

/**
 * Created by admin on 09.01.2017.
 * phoenix
 */
public class VkReponse {

    @SerializedName("error")
    public Error error;

    @SerializedName("execute_errors")
    public List<Error> executeErrors;
}
