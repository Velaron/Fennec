package dev.velaron.fennec.api.services;

import dev.velaron.fennec.api.model.Items;
import dev.velaron.fennec.api.model.VKApiStickerSet;
import dev.velaron.fennec.api.model.response.BaseResponse;
import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by admin on 08.01.2017.
 * phoenix
 */
public interface IStoreService {
    @FormUrlEncoded
    @POST("store.getProducts") //extended=1&filters=active&type=stickers&v=5.64" Thanks for Kate Mobile
    Single<BaseResponse<Items<VKApiStickerSet.Product>>> getProducts(@Field("extended") Integer extended,
                                                                     @Field("filters") String filters,
                                                                     @Field("type") String type);
}