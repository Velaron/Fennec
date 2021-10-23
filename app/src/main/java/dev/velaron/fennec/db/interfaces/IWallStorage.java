package dev.velaron.fennec.db.interfaces;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import dev.velaron.fennec.db.model.PostPatch;
import dev.velaron.fennec.db.model.entity.OwnerEntities;
import dev.velaron.fennec.db.model.entity.PostEntity;
import dev.velaron.fennec.model.EditingPostType;
import dev.velaron.fennec.model.criteria.WallCriteria;
import dev.velaron.fennec.util.Optional;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by ruslan.kolbasa on 03-Jun-16.
 * phoenix
 */
public interface IWallStorage extends IStorage {

    @CheckResult
    Single<int[]> storeWallEntities(int accountId, @NonNull List<PostEntity> posts,
                                    @Nullable OwnerEntities owners,
                                    @Nullable IClearWallTask clearWall);

    @CheckResult
    Single<Integer> replacePost(int accountId, @NonNull PostEntity post);

    @CheckResult
    Single<PostEntity> getEditingPost(int accountId, int ownerId, @EditingPostType int type, boolean includeAttachment);

    @CheckResult
    Completable deletePost(int accountId, int dbid);

    @CheckResult
    Single<Optional<PostEntity>> findPostById(int accountId, int dbid);

    @CheckResult
    Single<Optional<PostEntity>> findPostById(int accountId, int ownerId, int vkpostId, boolean includeAttachment);

    interface IClearWallTask {
        int getOwnerId();
    }

    Single<List<PostEntity>> findDbosByCriteria(@NonNull WallCriteria criteria);

    @CheckResult
    Completable update(int accountId, int ownerId, int postId, @NonNull PostPatch update);

    /**
     * Уведомить хранилище, что пост более не существует
     * @param accountId
     * @param postVkid
     * @param postOwnerId
     * @return
     */
    Completable invalidatePost(int accountId, int postVkid, int postOwnerId);
}