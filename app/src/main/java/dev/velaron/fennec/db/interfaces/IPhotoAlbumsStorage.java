package dev.velaron.fennec.db.interfaces;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;

import java.util.List;

import dev.velaron.fennec.db.model.entity.PhotoAlbumEntity;
import dev.velaron.fennec.model.criteria.PhotoAlbumsCriteria;
import dev.velaron.fennec.util.Optional;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by ruslan.kolbasa on 29.11.2016.
 * phoenix
 */
public interface IPhotoAlbumsStorage extends IStorage {

    @CheckResult
    Single<Optional<PhotoAlbumEntity>> findAlbumById(int accountId, int ownerId, int albumId);

    @CheckResult
    Single<List<PhotoAlbumEntity>> findAlbumsByCriteria(@NonNull PhotoAlbumsCriteria criteria);

    @CheckResult
    Completable store(int accountId, int ownerId, @NonNull List<PhotoAlbumEntity> albums, boolean clearBefore);

    @CheckResult
    Completable removeAlbumById(int accountId, int ownerId, int albumId);
}