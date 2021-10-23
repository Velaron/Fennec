package dev.velaron.fennec.domain;

import java.util.List;

import dev.velaron.fennec.fragment.search.criteria.VideoSearchCriteria;
import dev.velaron.fennec.model.Video;
import dev.velaron.fennec.model.VideoAlbum;
import dev.velaron.fennec.util.Pair;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by admin on 11.06.2017.
 * phoenix
 */
public interface IVideosInteractor {
    Single<List<Video>> get(int accountId, int ownerId, int albumId, int count, int offset);
    Single<List<Video>> getCachedVideos(int accountId, int ownerId, int albumId);

    Single<Video> getById(int accountId, int ownerId, int videoId, String accessKey, boolean cache);
    Completable addToMy(int accountId, int targetOwnerId, int videoOwnerId, int videoId);

    Single<Pair<Integer, Boolean>> likeOrDislike(int accountId, int ownerId, int videoId, String accessKey, boolean like);

    Single<List<VideoAlbum>> getCachedAlbums(int accoutnId, int ownerId);
    Single<List<VideoAlbum>> getActualAlbums(int accoutnId, int ownerId, int count, int offset);

    Single<List<Video>> seacrh(int accountId, VideoSearchCriteria criteria, int count, int offset);
}