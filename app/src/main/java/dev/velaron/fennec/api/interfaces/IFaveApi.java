package dev.velaron.fennec.api.interfaces;

import androidx.annotation.CheckResult;

import dev.velaron.fennec.api.model.FaveLinkDto;
import dev.velaron.fennec.api.model.Items;
import dev.velaron.fennec.api.model.VKApiPhoto;
import dev.velaron.fennec.api.model.VKApiVideo;
import dev.velaron.fennec.api.model.response.FavePageResponse;
import dev.velaron.fennec.api.model.response.FavePostsResponse;
import io.reactivex.Single;

/**
 * Created by admin on 09.01.2017.
 * phoenix
 */
public interface IFaveApi {

    @CheckResult
    Single<Items<FavePageResponse>> getPages(Integer offset, Integer count, String fields);

    @CheckResult
    Single<Items<VKApiPhoto>> getPhotos(Integer offset, Integer count);

    @CheckResult
    Single<Items<VKApiVideo>> getVideos(Integer offset, Integer count, Boolean extended);

    @CheckResult
    Single<FavePostsResponse> getPosts(Integer offset, Integer count, Boolean extended);

    @CheckResult
    Single<Items<FaveLinkDto>> getLinks(Integer offset, Integer count);

    @CheckResult
    Single<Boolean> addPage(Integer userId, Integer groupId);

    @CheckResult
    Single<Boolean> removePage(Integer userId, Integer groupId);

    @CheckResult
    Single<Boolean> removeLink(String linkId);

}
