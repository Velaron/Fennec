package dev.velaron.fennec.db.interfaces;

import androidx.annotation.NonNull;

import java.util.List;

import dev.velaron.fennec.db.AttachToType;
import dev.velaron.fennec.db.model.entity.Entity;
import dev.velaron.fennec.util.Pair;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by admin on 15.11.2016.
 * phoenix
 */
public interface IAttachmentsStorage extends IStorage {

    Completable remove(int accountId, @AttachToType int attachToType, int attachToDbid, int generatedAttachmentId);

    Single<int[]> attachDbos(int accountId, @AttachToType int attachToType, int attachToDbid, @NonNull List<Entity> entities);

    Single<Integer> getCount(int accountId, @AttachToType int attachToType, int attachToDbid);

    Single<List<Pair<Integer, Entity>>> getAttachmentsDbosWithIds(int accountId, @AttachToType int attachToType, int attachToDbid);

    List<Entity> getAttachmentsDbosSync(int accountId, @AttachToType int attachToType, int attachToDbid, @NonNull Cancelable cancelable);
}