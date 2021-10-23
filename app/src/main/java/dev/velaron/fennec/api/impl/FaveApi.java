package dev.velaron.fennec.api.impl;

import dev.velaron.fennec.api.IServiceProvider;
import dev.velaron.fennec.api.interfaces.IFaveApi;
import dev.velaron.fennec.api.model.FaveLinkDto;
import dev.velaron.fennec.api.model.Items;
import dev.velaron.fennec.api.model.VKApiPhoto;
import dev.velaron.fennec.api.model.VKApiVideo;
import dev.velaron.fennec.api.model.response.FavePageResponse;
import dev.velaron.fennec.api.model.response.FavePostsResponse;
import dev.velaron.fennec.api.services.IFaveService;
import io.reactivex.Single;

/**
 * Created by admin on 09.01.2017.
 * phoenix
 */
class FaveApi extends AbsApi implements IFaveApi {

    FaveApi(int accountId, IServiceProvider provider) {
        super(accountId, provider);
    }

    @Override
    public Single<Items<FavePageResponse>> getPages(Integer offset, Integer count, String fields) {
        return provideService(IFaveService.class)
                .flatMap(service -> service.getUsers(offset, count, null, fields)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiPhoto>> getPhotos(Integer offset, Integer count) {
        return provideService(IFaveService.class)
                .flatMap(service -> service.getPhotos(offset, count)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiVideo>> getVideos(Integer offset, Integer count, Boolean extended) {
        return provideService(IFaveService.class)
                .flatMap(service -> service.getVideos(offset, count, integerFromBoolean(extended))
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<FavePostsResponse> getPosts(Integer offset, Integer count, Boolean extended) {
        return provideService(IFaveService.class)
                .flatMap(service -> service.getPosts(offset, count, integerFromBoolean(extended))
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<FaveLinkDto>> getLinks(Integer offset, Integer count) {
        return provideService(IFaveService.class)
                .flatMap(service -> service.getLinks(offset, count)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Boolean> addPage(Integer userId, Integer groupId) {
        return provideService(IFaveService.class)
                .flatMap(service -> service.addPage(userId, groupId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Boolean> removePage(Integer userId, Integer groupId) {
        return provideService(IFaveService.class)
                .flatMap(service -> service.removePage(userId, groupId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Boolean> removeLink(String linkId) {
        return provideService(IFaveService.class)
                .flatMap(service -> service.removeLink(linkId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }
}