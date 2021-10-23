package dev.velaron.fennec.domain;

import java.util.List;

import dev.velaron.fennec.model.StickerSet;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by admin on 20.03.2017.
 * phoenix
 */
public interface IStickersInteractor {
    Completable getAndStore(int accountId);
    Single<List<StickerSet>> getStickers(int accountId);
}