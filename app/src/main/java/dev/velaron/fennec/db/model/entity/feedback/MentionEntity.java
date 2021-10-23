package dev.velaron.fennec.db.model.entity.feedback;

import dev.velaron.fennec.db.model.entity.Entity;
import dev.velaron.fennec.db.model.entity.EntityWrapper;

/**
 * Base class for types [mention]
 */
public class MentionEntity extends FeedbackEntity {

    private EntityWrapper where = EntityWrapper.empty();

    public MentionEntity(int type) {
        super(type);
    }

    public MentionEntity setWhere(Entity where) {
        this.where = new EntityWrapper(where);
        return this;
    }

    public Entity getWhere() {
        return where.get();
    }
}