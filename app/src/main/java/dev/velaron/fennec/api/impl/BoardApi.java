package dev.velaron.fennec.api.impl;

import java.util.Collection;

import dev.velaron.fennec.api.IServiceProvider;
import dev.velaron.fennec.api.interfaces.IBoardApi;
import dev.velaron.fennec.api.model.IAttachmentToken;
import dev.velaron.fennec.api.model.VKApiTopic;
import dev.velaron.fennec.api.model.response.DefaultCommentsResponse;
import dev.velaron.fennec.api.model.response.TopicsResponse;
import dev.velaron.fennec.api.services.IBoardService;
import io.reactivex.Single;

/**
 * Created by admin on 03.01.2017.
 * phoenix
 */
class BoardApi extends AbsApi implements IBoardApi {

    BoardApi(int accountId, IServiceProvider provider) {
        super(accountId, provider);
    }

    @Override
    public Single<DefaultCommentsResponse> getComments(int groupId, int topicId, Boolean needLikes, Integer startCommentId, Integer offset, Integer count, Boolean extended, String sort, String fields) {
        return provideService(IBoardService.class)
                .flatMap(service -> service
                        .getComments(groupId, topicId, integerFromBoolean(needLikes), startCommentId, offset, count, integerFromBoolean(extended), sort, fields)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Boolean> restoreComment(int groupId, int topicId, int commentId) {
        return provideService(IBoardService.class)
                .flatMap(service -> service.restoreComment(groupId, topicId, commentId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Boolean> deleteComment(int groupId, int topicId, int commentId) {
        return provideService(IBoardService.class)
                .flatMap(service -> service.deleteComment(groupId, topicId, commentId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<TopicsResponse> getTopics(int groupId, Collection<Integer> topicIds, Integer order,
                                            Integer offset, Integer count, Boolean extended,
                                            Integer preview, Integer previewLength, String fields) {
        return provideService(IBoardService.class)
                .flatMap(service -> service
                        .getTopics(groupId, join(topicIds, ","), order, offset, count, integerFromBoolean(extended),
                                preview, previewLength, fields)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> {
                            // fix (не приходит owner_id)
                            for (VKApiTopic topic : response.items) {
                                topic.owner_id = -groupId;
                            }
                            return response;
                        }));
    }

    @Override
    public Single<Boolean> editComment(int groupId, int topicId, int commentId, String message,
                                       Collection<IAttachmentToken> attachments) {
        return provideService(IBoardService.class)
                .flatMap(service -> service.editComment(groupId, topicId, commentId, message, join(attachments, ",", AbsApi::formatAttachmentToken))
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Integer> addComment(Integer groupId, int topicId, String message, Collection<IAttachmentToken> attachments, Boolean fromGroup, Integer stickerId, Integer generatedUniqueId) {
        return provideService(IBoardService.class)
                .flatMap(service -> service
                        .addComment(groupId, topicId, message, join(attachments, ",", AbsApi::formatAttachmentToken),
                                integerFromBoolean(fromGroup), stickerId, generatedUniqueId)
                        .map(extractResponseWithErrorHandling()));
    }
}
