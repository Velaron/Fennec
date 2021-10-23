package dev.velaron.fennec.domain.impl;

import java.util.List;

import dev.velaron.fennec.api.interfaces.INetworker;
import dev.velaron.fennec.api.model.VKApiStickerSet;
import dev.velaron.fennec.db.interfaces.IStickersStorage;
import dev.velaron.fennec.domain.IStickersInteractor;
import dev.velaron.fennec.domain.mappers.Dto2Entity;
import dev.velaron.fennec.domain.mappers.Entity2Model;
import dev.velaron.fennec.model.StickerSet;
import io.reactivex.Completable;
import io.reactivex.Single;

import static dev.velaron.fennec.domain.mappers.MapUtil.mapAll;
import static dev.velaron.fennec.util.Utils.listEmptyIfNull;

/**
 * Created by admin on 20.03.2017.
 * phoenix
 */
public class StickersInteractor implements IStickersInteractor {

    private final INetworker networker;
    private final IStickersStorage storage;

    public StickersInteractor(INetworker networker, IStickersStorage storage) {
        this.networker = networker;
        this.storage = storage;
    }

    @Override
    public Completable getAndStore(int accountId) {
        return networker.vkDefault(accountId)
                .store()
                .getProducts(true, "active", "stickers")
                .flatMapCompletable(items -> {
                    List<VKApiStickerSet.Product> list = listEmptyIfNull(items.items);
                    return storage.store(accountId, mapAll(list, Dto2Entity::mapStikerSet));
                });
    }

    @Override
    public Single<List<StickerSet>> getStickers(int accountId) {
        return storage.getPurchasedAndActive(accountId)
                .map(entities -> mapAll(entities, Entity2Model::map));
    }
}