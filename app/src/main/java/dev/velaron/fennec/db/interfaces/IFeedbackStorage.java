package dev.velaron.fennec.db.interfaces;

import androidx.annotation.NonNull;

import java.util.List;

import dev.velaron.fennec.db.model.entity.OwnerEntities;
import dev.velaron.fennec.db.model.entity.feedback.FeedbackEntity;
import dev.velaron.fennec.model.criteria.NotificationsCriteria;
import io.reactivex.Single;

/**
 * Created by ruslan.kolbasa on 13-Jun-16.
 * phoenix
 */
public interface IFeedbackStorage extends IStorage {
    Single<int[]> insert(int accountId, List<FeedbackEntity> dbos, OwnerEntities owners, boolean clearBefore);
    Single<List<FeedbackEntity>> findByCriteria(@NonNull NotificationsCriteria criteria);
}