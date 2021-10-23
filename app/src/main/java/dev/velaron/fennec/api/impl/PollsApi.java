package dev.velaron.fennec.api.impl;

import com.google.gson.JsonArray;

import java.util.Collection;
import java.util.Set;

import dev.velaron.fennec.api.IServiceProvider;
import dev.velaron.fennec.api.TokenType;
import dev.velaron.fennec.api.interfaces.IPollsApi;
import dev.velaron.fennec.api.model.VKApiPoll;
import dev.velaron.fennec.api.services.IPollsService;
import io.reactivex.Single;

/**
 * Created by admin on 09.01.2017.
 * phoenix
 */
class PollsApi extends AbsApi implements IPollsApi {

    PollsApi(int accountId, IServiceProvider provider) {
        super(accountId, provider);
    }

    @Override
    public Single<VKApiPoll> create(String question, Boolean isAnonymous, Boolean isMultiple, Integer ownerId, Collection<String> addAnswers) {
        JsonArray array = new JsonArray();
        for (String answer : addAnswers) {
            array.add(answer);
        }

        return provideService(IPollsService.class, TokenType.USER)
                .flatMap(service -> service
                        .create(question, integerFromBoolean(isAnonymous), integerFromBoolean(isMultiple), ownerId, array.toString())
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Boolean> deleteVote(Integer ownerId, int pollId, int answerId, Boolean isBoard) {
        return provideService(IPollsService.class, TokenType.USER)
                .flatMap(service -> service.deleteVote(ownerId, pollId, answerId, integerFromBoolean(isBoard))
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Boolean> addVote(Integer ownerId, int pollId, Set<Integer> answerIds, Boolean isBoard) {
        return provideService(IPollsService.class, TokenType.USER)
                .flatMap(service -> service.addVote(ownerId, pollId, join(answerIds, ","), integerFromBoolean(isBoard))
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<VKApiPoll> getById(Integer ownerId, Boolean isBoard, Integer pollId) {
        return provideService(IPollsService.class, TokenType.USER)
                .flatMap(service -> service.getById(ownerId, integerFromBoolean(isBoard), pollId)
                        .map(extractResponseWithErrorHandling()));
    }
}
