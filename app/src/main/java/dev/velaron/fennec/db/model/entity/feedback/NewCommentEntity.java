package dev.velaron.fennec.db.model.entity.feedback;

import dev.velaron.fennec.db.model.entity.CommentEntity;
import dev.velaron.fennec.db.model.entity.Entity;
import dev.velaron.fennec.db.model.entity.EntityWrapper;

/**
 * Created by ruslan.kolbasa on 09.12.2016.
 * phoenix
 * base class for types [comment_post, comment_photo, comment_video]
 */
public class NewCommentEntity extends FeedbackEntity {

    private EntityWrapper commented = EntityWrapper.empty();

    private CommentEntity comment;

    public NewCommentEntity(int type) {
        super(type);
    }

    public NewCommentEntity setComment(CommentEntity comment) {
        this.comment = comment;
        return this;
    }

    public NewCommentEntity setCommented(Entity commented) {
        this.commented = new EntityWrapper(commented);
        return this;
    }

    public CommentEntity getComment() {
        return comment;
    }

    public Entity getCommented() {
        return commented.get();
    }
}