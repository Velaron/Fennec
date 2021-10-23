package dev.velaron.fennec.api.interfaces;

import androidx.annotation.CheckResult;

import java.util.Collection;
import java.util.Set;

import dev.velaron.fennec.api.model.VKApiPoll;
import io.reactivex.Single;

/**
 * Created by admin on 09.01.2017.
 * phoenix
 */
public interface IPollsApi {

    @CheckResult
    Single<VKApiPoll> create(String question, Boolean isAnonymous, Boolean isMultiple, Integer ownerId, Collection<String> addAnswers);

    @CheckResult
    Single<Boolean> deleteVote(Integer ownerId, int pollId, int answerId, Boolean isBoard);

    @CheckResult
    Single<Boolean> addVote(Integer ownerId, int pollId, Set<Integer> answerIds, Boolean isBoard);

    @CheckResult
    Single<VKApiPoll> getById(Integer ownerId, Boolean isBoard, Integer pollId);

}
