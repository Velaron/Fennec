package dev.velaron.fennec.db.interfaces;

import java.util.List;

import dev.velaron.fennec.db.serialize.ISerializeAdapter;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by Ruslan Kolbasa on 20.06.2017.
 * phoenix
 */
public interface ITempDataStorage {
    <T> Single<List<T>> getData(int ownerId, int sourceId, ISerializeAdapter<T> serializer);
    <T> Completable put(int ownerId, int sourceId, List<T> data, ISerializeAdapter<T> serializer);

    Completable delete(int ownerId);
}