package dev.velaron.fennec.db.interfaces;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import dev.velaron.fennec.db.model.entity.FeedListEntity;
import dev.velaron.fennec.db.model.entity.NewsEntity;
import dev.velaron.fennec.db.model.entity.OwnerEntities;
import dev.velaron.fennec.model.FeedSourceCriteria;
import dev.velaron.fennec.model.criteria.FeedCriteria;
import io.reactivex.Completable;
import io.reactivex.Single;

public interface IFeedStorage extends IStorage {

    Single<List<NewsEntity>> findByCriteria(@NonNull FeedCriteria criteria);

    Single<int[]> store(int accountId, @NonNull List<NewsEntity> data, @Nullable OwnerEntities owners, boolean clearBeforeStore);

    Completable storeLists(int accountid, @NonNull List<FeedListEntity> entities);

    Single<List<FeedListEntity>> getAllLists(@NonNull FeedSourceCriteria criteria);
}