package dev.velaron.fennec.db.interfaces;

import androidx.annotation.NonNull;

import java.util.List;

import dev.velaron.fennec.db.model.entity.VideoAlbumEntity;
import dev.velaron.fennec.model.VideoAlbumCriteria;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by admin on 21.11.2016.
 * phoenix
 */
public interface IVideoAlbumsStorage extends IStorage {
    Single<List<VideoAlbumEntity>> findByCriteria(@NonNull VideoAlbumCriteria criteria);
    Completable insertData(int accountId, int ownerId, @NonNull List<VideoAlbumEntity> data, boolean invalidateBefore);
}