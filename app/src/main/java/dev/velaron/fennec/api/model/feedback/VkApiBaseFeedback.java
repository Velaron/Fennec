package dev.velaron.fennec.api.model.feedback;

import dev.velaron.fennec.api.model.VKApiComment;

/**
 * Created by ruslan.kolbasa on 09.12.2016.
 * phoenix
 */
public abstract class VkApiBaseFeedback {

    public String type;
    public long date;

    public VKApiComment reply;


}
