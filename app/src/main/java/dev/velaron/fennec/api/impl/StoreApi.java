package dev.velaron.fennec.api.impl;

import dev.velaron.fennec.api.IServiceProvider;
import dev.velaron.fennec.api.TokenType;
import dev.velaron.fennec.api.interfaces.IStoreApi;
import dev.velaron.fennec.api.model.Items;
import dev.velaron.fennec.api.model.VKApiStickerSet;
import dev.velaron.fennec.api.services.IStoreService;
import io.reactivex.Single;

/**
 * Created by admin on 08.01.2017.
 * phoenix
 */
class StoreApi extends AbsApi implements IStoreApi {

    StoreApi(int accountId, IServiceProvider provider) {
        super(accountId, provider);
    }

    @Override
    public Single<Items<VKApiStickerSet.Product>> getProducts(Boolean extended, String filters, String type) {
        return provideService(IStoreService.class, TokenType.USER)
                .flatMap(service -> service
                        .getProducts(integerFromBoolean(extended), filters, type)
                        .map(extractResponseWithErrorHandling()));
    }
}