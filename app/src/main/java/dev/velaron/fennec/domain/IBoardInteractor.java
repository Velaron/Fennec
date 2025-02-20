package dev.velaron.fennec.domain;

import java.util.List;

import dev.velaron.fennec.model.Topic;
import io.reactivex.Single;

/**
 * Created by Ruslan Kolbasa on 19.09.2017.
 * phoenix
 */
public interface IBoardInteractor {
    Single<List<Topic>> getCachedTopics(int accountId, int ownerId);
    Single<List<Topic>> getActualTopics(int accountId, int ownerId, int count, int offset);
}