package dev.velaron.fennec.api.model.feedback;

import dev.velaron.fennec.api.model.Copyable;

/**
 * Created by ruslan.kolbasa on 09.12.2016.
 * phoenix
 * base class for types [copy_post, copy_photo, copy_video]
 */
public class VkApiCopyFeedback extends VkApiBaseFeedback {

    public Copyable what;
    public Copies copies;

}
