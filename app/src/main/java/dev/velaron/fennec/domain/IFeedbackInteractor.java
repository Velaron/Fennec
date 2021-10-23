package dev.velaron.fennec.domain;

import java.util.List;

import dev.velaron.fennec.model.feedback.Feedback;
import dev.velaron.fennec.util.Pair;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by Ruslan Kolbasa on 11.09.2017.
 * phoenix
 */
public interface IFeedbackInteractor {
    Single<List<Feedback>> getCachedFeedbacks(int accountId);
    Single<Pair<List<Feedback>, String>> getActualFeedbacks(int accountId, int count, String startFrom);

    Completable maskAaViewed(int accountId);
}