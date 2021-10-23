package dev.velaron.fennec.db.interfaces;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;

import java.util.List;

import dev.velaron.fennec.db.model.entity.FaveLinkEntity;
import dev.velaron.fennec.db.model.entity.FavePageEntity;
import dev.velaron.fennec.db.model.entity.OwnerEntities;
import dev.velaron.fennec.db.model.entity.PhotoEntity;
import dev.velaron.fennec.db.model.entity.PostEntity;
import dev.velaron.fennec.db.model.entity.VideoEntity;
import dev.velaron.fennec.model.criteria.FavePhotosCriteria;
import dev.velaron.fennec.model.criteria.FavePostsCriteria;
import dev.velaron.fennec.model.criteria.FaveVideosCriteria;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by hp-dv6 on 28.05.2016.
 * VKMessenger
 */
public interface IFaveStorage extends IStorage {

    @CheckResult
    Single<List<PostEntity>> getFavePosts(@NonNull FavePostsCriteria criteria);

    @CheckResult
    Completable storePosts(int accountId, List<PostEntity> posts, OwnerEntities owners, boolean clearBeforeStore);

    @CheckResult
    Single<List<FaveLinkEntity>> getFaveLinks(int accountId);

    Completable removeLink(int accountId, String id);

    Completable storeLinks(int accountId, List<FaveLinkEntity> entities, boolean clearBefore);

    @CheckResult
    Completable storePages(int accountId, List<FavePageEntity> users, boolean clearBeforeStore);

    Single<List<FavePageEntity>> getFaveUsers(int accountId);

    Completable removePage(int accountId, int ownerId);

    @CheckResult
    Single<int[]> storePhotos(int accountId, List<PhotoEntity> photos, boolean clearBeforeStore);

    @CheckResult
    Single<List<PhotoEntity>> getPhotos(FavePhotosCriteria criteria);

    @CheckResult
    Single<List<VideoEntity>> getVideos(FaveVideosCriteria criteria);

    @CheckResult
    Single<int[]> storeVideos(int accountId, List<VideoEntity> videos, boolean clearBeforeStore);
}