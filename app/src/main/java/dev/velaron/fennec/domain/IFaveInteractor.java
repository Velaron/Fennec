package dev.velaron.fennec.domain;

import java.util.List;

import dev.velaron.fennec.model.EndlessData;
import dev.velaron.fennec.model.FaveLink;
import dev.velaron.fennec.model.FavePage;
import dev.velaron.fennec.model.Photo;
import dev.velaron.fennec.model.Post;
import dev.velaron.fennec.model.Video;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by Ruslan Kolbasa on 14.07.2017.
 * phoenix
 */
public interface IFaveInteractor {
    Single<List<Post>> getPosts(int accountId, int count, int offset);

    Single<List<Post>> getCachedPosts(int accountId);

    Single<List<Photo>> getCachedPhotos(int accountId);

    Single<List<Photo>> getPhotos(int accountId, int count, int offset);

    Single<List<Video>> getCachedVideos(int accountId);

    Single<List<Video>> getVideos(int accountId, int count, int offset);

    Single<List<FavePage>> getCachedPages(int accountId);

    Single<EndlessData<FavePage>> getPages(int accountId, int count, int offset);

    Completable removePage(int accountId, int ownerId);

    Single<List<FaveLink>> getCachedLinks(int accountId);

    Single<EndlessData<FaveLink>> getLinks(int accountId, int count, int offset);

    Completable removeLink(int accountId, String id);

    Completable addPage(int accountId, int ownerId);
}