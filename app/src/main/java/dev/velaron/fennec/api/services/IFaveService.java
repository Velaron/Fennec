package dev.velaron.fennec.api.services;

import dev.velaron.fennec.api.model.FaveLinkDto;
import dev.velaron.fennec.api.model.Items;
import dev.velaron.fennec.api.model.VKApiPhoto;
import dev.velaron.fennec.api.model.VKApiVideo;
import dev.velaron.fennec.api.model.response.BaseResponse;
import dev.velaron.fennec.api.model.response.FavePageResponse;
import dev.velaron.fennec.api.model.response.FavePostsResponse;
import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by admin on 09.01.2017.
 * phoenix
 */
public interface IFaveService {

    @FormUrlEncoded
    @POST("fave.getPages")
    Single<BaseResponse<Items<FavePageResponse>>> getUsers(@Field("offset") Integer offset,
                                                           @Field("count") Integer count,
                                                           @Field("type") String type,
                                                           @Field("fields") String fields);

    @FormUrlEncoded
    @POST("fave.getPages")
    Single<BaseResponse<Items<FavePageResponse>>> getGroups(@Field("offset") Integer offset,
                                                            @Field("count") Integer count,
                                                            @Field("type") String type,
                                                            @Field("fields") String fields);

    @FormUrlEncoded
    @POST("fave.getPhotos")
    Single<BaseResponse<Items<VKApiPhoto>>> getPhotos(@Field("offset") Integer offset,
                                                      @Field("count") Integer count);

    @FormUrlEncoded
    @POST("fave.getVideos")
    Single<BaseResponse<Items<VKApiVideo>>> getVideos(@Field("offset") Integer offset,
                                                      @Field("count") Integer count,
                                                      @Field("extended") Integer extended);

    @FormUrlEncoded
    @POST("fave.getPosts")
    Single<BaseResponse<FavePostsResponse>> getPosts(@Field("offset") Integer offset,
                                                     @Field("count") Integer count,
                                                     @Field("extended") Integer extended);

    @FormUrlEncoded
    @POST("fave.getLinks")
    Single<BaseResponse<Items<FaveLinkDto>>> getLinks(@Field("offset") Integer offset,
                                                      @Field("count") Integer count);

    @FormUrlEncoded
    @POST("fave.addPage")
    Single<BaseResponse<Integer>> addPage(@Field("user_id") Integer userId,
                                          @Field("group_id") Integer groupId);

    //https://vk.com/dev/fave.removePage
    @FormUrlEncoded
    @POST("fave.removePage")
    Single<BaseResponse<Integer>> removePage(@Field("user_id") Integer userId,
                                             @Field("group_id") Integer groupId);

    @FormUrlEncoded
    @POST("fave.removeLink")
    Single<BaseResponse<Integer>> removeLink(@Field("link_id") String linkId);

}
