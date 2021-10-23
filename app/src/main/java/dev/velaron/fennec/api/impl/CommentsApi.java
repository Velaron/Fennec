package dev.velaron.fennec.api.impl;

import dev.velaron.fennec.api.IServiceProvider;
import dev.velaron.fennec.api.interfaces.ICommentsApi;
import dev.velaron.fennec.api.model.response.CustomCommentsResponse;
import dev.velaron.fennec.api.services.ICommentsService;
import io.reactivex.Single;

/**
 * Created by admin on 03.01.2017.
 * phoenix
 */
class CommentsApi extends AbsApi implements ICommentsApi {

    CommentsApi(int accountId, IServiceProvider provider) {
        super(accountId, provider);
    }

    @Override
    public Single<CustomCommentsResponse> get(String sourceType, int ownerId, int sourceId, Integer offset,
                                              Integer count, String sort, Integer startCommentId, String accessKey, String fields) {
        return provideService(ICommentsService.class)
                .flatMap(service -> service
                        .get(sourceType, ownerId, sourceId, offset, count, sort, startCommentId, accessKey, fields)
                        .map(handleExecuteErrors("execute.getComments"))
                        .map(extractResponseWithErrorHandling()));
    }
}