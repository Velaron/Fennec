package dev.velaron.fennec.db.interfaces;

import android.graphics.Bitmap;

import java.util.List;

import dev.velaron.fennec.model.LocalImageAlbum;
import dev.velaron.fennec.model.LocalPhoto;
import io.reactivex.Single;

/**
 * Created by admin on 03.10.2016.
 * phoenix
 */
public interface ILocalMediaStorage extends IStorage {

    Single<List<LocalPhoto>> getPhotos(long albumId);

    Single<List<LocalImageAlbum>> getImageAlbums();

    Bitmap getImageThumbnail(long imageId);

    //Single<List<LocalVideo>> getVideos();

    //Bitmap getVideoThumbnail(long videoId);
}
