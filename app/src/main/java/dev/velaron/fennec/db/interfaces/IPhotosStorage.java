package dev.velaron.fennec.db.interfaces;

import androidx.annotation.NonNull;

import java.util.List;

import dev.velaron.fennec.db.model.PhotoPatch;
import dev.velaron.fennec.db.model.entity.PhotoEntity;
import dev.velaron.fennec.model.criteria.PhotoCriteria;
import io.reactivex.Completable;
import io.reactivex.Single;

public interface IPhotosStorage extends IStorage {

    Completable insertPhotosRx(int accountId, int ownerId, int albumId, @NonNull List<PhotoEntity> photos, boolean clearBefore);

    Single<List<PhotoEntity>> findPhotosByCriteriaRx(@NonNull PhotoCriteria criteria);

    Completable applyPatch(int accountId, int ownerId, int photoId, PhotoPatch patch);
}