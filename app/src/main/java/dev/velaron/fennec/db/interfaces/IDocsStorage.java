package dev.velaron.fennec.db.interfaces;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;

import java.util.List;

import dev.velaron.fennec.db.model.entity.DocumentEntity;
import dev.velaron.fennec.model.criteria.DocsCriteria;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by admin on 25.12.2016.
 * phoenix
 */
public interface IDocsStorage extends IStorage {

    @CheckResult
    Single<List<DocumentEntity>> get(@NonNull DocsCriteria criteria);

    @CheckResult
    Completable store(int accountId, int ownerId, List<DocumentEntity> entities, boolean clearBeforeInsert);

    @CheckResult
    Completable delete(int accountId, int docId, int ownerId);
}