package dev.velaron.fennec.api.model.feedback;

import dev.velaron.fennec.api.model.Commentable;
import dev.velaron.fennec.api.model.VKApiComment;

/**
 * Created by ruslan.kolbasa on 09.12.2016.
 * phoenix
 * base class for types [comment_post, comment_photo, comment_video]
 */
public class VkApiCommentFeedback extends VkApiBaseFeedback {
    public Commentable comment_of;
    public VKApiComment comment;
}