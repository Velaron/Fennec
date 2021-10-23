package dev.velaron.fennec.db.interfaces;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;

import java.util.List;

import dev.velaron.fennec.db.model.entity.OwnerEntities;
import dev.velaron.fennec.db.model.entity.PollEntity;
import dev.velaron.fennec.db.model.entity.TopicEntity;
import dev.velaron.fennec.model.criteria.TopicsCriteria;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by admin on 13.12.2016.
 * phoenix
 */
public interface ITopicsStore {

    @CheckResult
    Single<List<TopicEntity>> getByCriteria(@NonNull TopicsCriteria criteria);

    @CheckResult
    Completable store(int accountId, int ownerId, List<TopicEntity> topics, OwnerEntities owners, boolean canAddTopic, int defaultOrder, boolean clearBefore);

    @CheckResult
    Completable attachPoll(int accountId, int ownerId, int topicId, PollEntity pollDbo);
}