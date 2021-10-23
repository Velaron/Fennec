package dev.velaron.fennec.api.model.feedback;

import dev.velaron.fennec.api.model.Commentable;
import dev.velaron.fennec.api.model.VKApiComment;

/**
 * Created by ruslan.kolbasa on 09.12.2016.
 * phoenix
 */
public class VkApiLikeCommentFeedback extends VkApiBaseFeedback {

    public UserArray users;

    public VKApiComment comment;

    public Commentable commented;
}
