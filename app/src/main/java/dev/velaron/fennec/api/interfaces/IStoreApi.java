package dev.velaron.fennec.api.interfaces;

import dev.velaron.fennec.api.model.Items;
import dev.velaron.fennec.api.model.VKApiStickerSet;
import io.reactivex.Single;

/**
 * Created by admin on 08.01.2017.
 * phoenix
 */
public interface IStoreApi {
    Single<Items<VKApiStickerSet.Product>> getProducts(Boolean extended, String filters, String type);
}