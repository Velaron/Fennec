package dev.velaron.fennec.api.interfaces;

import androidx.annotation.CheckResult;

import java.util.Collection;

import dev.velaron.fennec.api.model.IAttachmentToken;
import dev.velaron.fennec.api.model.IdPair;
import dev.velaron.fennec.api.model.response.DefaultCommentsResponse;
import dev.velaron.fennec.api.model.response.PostsResponse;
import dev.velaron.fennec.api.model.response.RepostReponse;
import dev.velaron.fennec.api.model.response.WallResponse;
import dev.velaron.fennec.api.model.response.WallSearchResponse;
import io.reactivex.Single;

/**
 * Created by admin on 03.01.2017.
 * phoenix
 */
public interface IWallApi {

    Single<WallSearchResponse> search(int ownerId, String query, Boolean ownersOnly,
                                      int count, int offset, Boolean extended, String fields);

    @CheckResult
    Single<Boolean> edit(Integer ownerId, Integer postId, Boolean friendsOnly, String message,
                         Collection<IAttachmentToken> attachments, String services,
                         Boolean signed, Long publishDate, Double latitude,
                         Double longitude, Integer placeId, Boolean markAsAds);

    @CheckResult
    Single<Boolean> pin(Integer ownerId, int postId);

    @CheckResult
    Single<Boolean> unpin(Integer ownerId, int postId);

    @CheckResult
    Single<RepostReponse> repost(int postOwnerId, int postId, String message, Integer groupId, Boolean markAsAds);

    @CheckResult
    Single<Integer> post(Integer ownerId, Boolean friendsOnly, Boolean fromGroup, String message,
                         Collection<IAttachmentToken> attachments, String services, Boolean signed,
                         Long publishDate, Double latitude, Double longitude, Integer placeId,
                         Integer postId, Integer guid, Boolean markAsAds, Boolean adsPromotedStealth);

    @CheckResult
    Single<Boolean> delete(Integer ownerId, int postId);

    @CheckResult
    Single<Boolean> restoreComment(Integer ownerId, int commentId);

    @CheckResult
    Single<Boolean> deleteComment(Integer ownerId, int commentId);

    @CheckResult
    Single<Boolean> restore(Integer ownerId, int postId);

    @CheckResult
    Single<Boolean> editComment(Integer ownerId, int commentId, String message, Collection<IAttachmentToken> attachments);

    @CheckResult
    Single<Integer> createComment(Integer ownerId, int postId, Integer fromGroup,
                                  String message, Integer replyToComment,
                                  Collection<IAttachmentToken> attachments, Integer stickerId,
                                  Integer generatedUniqueId);

    @CheckResult
    Single<WallResponse> get(Integer ownerId, String domain, Integer offset, Integer count,
                             String filter, Boolean extended, String fields);

    @CheckResult
    Single<PostsResponse> getById(Collection<IdPair> ids, Boolean extended,
                                  Integer copyHistoryDepth, String fields);

    @CheckResult
    Single<DefaultCommentsResponse> getComments(int ownerId, int postId, Boolean needLikes,
                                                Integer startCommentId, Integer offset, Integer count,
                                                String sort, Boolean extended, String fields);

}
