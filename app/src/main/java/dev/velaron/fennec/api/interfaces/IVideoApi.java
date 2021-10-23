package dev.velaron.fennec.api.interfaces;

import androidx.annotation.CheckResult;

import java.util.Collection;

import dev.velaron.fennec.api.model.AccessIdPair;
import dev.velaron.fennec.api.model.IAttachmentToken;
import dev.velaron.fennec.api.model.Items;
import dev.velaron.fennec.api.model.VKApiVideo;
import dev.velaron.fennec.api.model.VKApiVideoAlbum;
import dev.velaron.fennec.api.model.response.DefaultCommentsResponse;
import dev.velaron.fennec.api.model.response.SearchVideoResponse;
import io.reactivex.Single;

/**
 * Created by admin on 03.01.2017.
 * phoenix
 */
public interface IVideoApi {

    @CheckResult
    Single<DefaultCommentsResponse> getComments(Integer ownerId, int videoId, Boolean needLikes,
                                                Integer startCommentId, Integer offset, Integer count, String sort,
                                                Boolean extended, String fields);

    @CheckResult
    Single<Integer> addVideo(Integer targetId, Integer videoId, Integer ownerId);

    @CheckResult
    Single<Integer> deleteVideo(Integer videoId, Integer ownerId, Integer targetId);

    @CheckResult
    Single<Items<VKApiVideoAlbum>> getAlbums(Integer ownerId, Integer offset, Integer count, Boolean needSystem);

    @CheckResult
    Single<SearchVideoResponse> search(String query, Integer sort, Boolean hd, Boolean adult, String filters,
                                       Boolean searchOwn, Integer offset, Integer longer, Integer shorter,
                                       Integer count, Boolean extended);

    @CheckResult
    Single<Boolean> restoreComment(Integer ownerId, int commentId);

    @CheckResult
    Single<Boolean> deleteComment(Integer ownerId, int commentId);

    @CheckResult
    Single<Items<VKApiVideo>> get(Integer ownerId, Collection<AccessIdPair> ids, Integer albumId,
                                  Integer count, Integer offset, Boolean extended);

    @CheckResult
    Single<Integer> createComment(Integer ownerId, int videoId, String message,
                                  Collection<IAttachmentToken> attachments, Boolean fromGroup,
                                  Integer replyToComment, Integer stickerId, Integer uniqueGeneratedId);


    @CheckResult
    Single<Boolean> editComment(Integer ownerId, int commentId, String message, Collection<IAttachmentToken> attachments);

}
