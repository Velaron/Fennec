package dev.velaron.fennec.db.model.entity.feedback;

import dev.velaron.fennec.db.model.entity.CommentEntity;
import dev.velaron.fennec.db.model.entity.Entity;
import dev.velaron.fennec.db.model.entity.EntityWrapper;

/**
 * Created by ruslan.kolbasa on 09.12.2016.
 * phoenix
 */
public class ReplyCommentEntity extends FeedbackEntity {

    private EntityWrapper commented = EntityWrapper.empty();

    private CommentEntity ownComment;

    private CommentEntity feedbackComment;

    public ReplyCommentEntity(int type) {
        super(type);
    }

    public ReplyCommentEntity setCommented(Entity commented) {
        this.commented = new EntityWrapper(commented);
        return this;
    }

    public ReplyCommentEntity setFeedbackComment(CommentEntity feedbackComment) {
        this.feedbackComment = feedbackComment;
        return this;
    }

    public ReplyCommentEntity setOwnComment(CommentEntity ownComment) {
        this.ownComment = ownComment;
        return this;
    }

    public Entity getCommented() {
        return commented.get();
    }

    public CommentEntity getFeedbackComment() {
        return feedbackComment;
    }

    public CommentEntity getOwnComment() {
        return ownComment;
    }
}