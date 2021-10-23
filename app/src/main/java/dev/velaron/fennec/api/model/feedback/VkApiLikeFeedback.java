package dev.velaron.fennec.api.model.feedback;

import dev.velaron.fennec.api.model.Likeable;

/**
 * Created by ruslan.kolbasa on 09.12.2016.
 * phoenix
 */
public class VkApiLikeFeedback extends VkApiBaseFeedback {

    public UserArray users;
    public Likeable liked;

}