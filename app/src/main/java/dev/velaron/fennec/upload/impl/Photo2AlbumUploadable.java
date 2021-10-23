package dev.velaron.fennec.upload.impl;

import android.content.Context;

import java.io.InputStream;
import java.util.Collections;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.exifinterface.media.ExifInterface;
import dev.velaron.fennec.api.PercentagePublisher;
import dev.velaron.fennec.api.interfaces.INetworker;
import dev.velaron.fennec.api.model.server.UploadServer;
import dev.velaron.fennec.db.interfaces.IPhotosStorage;
import dev.velaron.fennec.db.model.entity.PhotoEntity;
import dev.velaron.fennec.domain.mappers.Dto2Entity;
import dev.velaron.fennec.domain.mappers.Dto2Model;
import dev.velaron.fennec.exception.NotFoundException;
import dev.velaron.fennec.model.Photo;
import dev.velaron.fennec.upload.IUploadable;
import dev.velaron.fennec.upload.Upload;
import dev.velaron.fennec.upload.UploadResult;
import dev.velaron.fennec.upload.UploadUtils;
import dev.velaron.fennec.util.ExifGeoDegree;
import dev.velaron.fennec.util.Objects;
import io.reactivex.Completable;
import io.reactivex.Single;

import static dev.velaron.fennec.util.RxUtils.safelyCloseAction;
import static dev.velaron.fennec.util.Utils.safelyClose;

public class Photo2AlbumUploadable implements IUploadable<Photo> {

    private final Context context;
    private final INetworker networker;
    private final IPhotosStorage storage;

    public Photo2AlbumUploadable(Context context, INetworker networker, IPhotosStorage storage) {
        this.context = context;
        this.networker = networker;
        this.storage = storage;
    }

    @Override
    public Single<UploadResult<Photo>> doUpload(@NonNull Upload upload, @Nullable UploadServer initialServer, @Nullable PercentagePublisher listener) {
        final int accountId = upload.getAccountId();
        final int albumId = upload.getDestination().getId();
        final Integer groupId = upload.getDestination().getOwnerId() < 0 ? Math.abs(upload.getDestination().getOwnerId()) : null;

        Single<UploadServer> serverSingle;
        if(Objects.nonNull(initialServer)){
            serverSingle = Single.just(initialServer);
        } else {
            serverSingle = networker.vkDefault(accountId)
                    .photos()
                    .getUploadServer(albumId, groupId)
                    .map(s -> s);
        }

        return serverSingle.flatMap(server -> {
            final InputStream[] is = new InputStream[1];

            try {
                is[0] = UploadUtils.openStream(context, upload.getFileUri(), upload.getSize());
                return networker.uploads()
                        .uploadPhotoToAlbumRx(server.getUrl(), is[0], listener)
                        .doFinally(safelyCloseAction(is[0]))
                        .flatMap(dto -> {
                            Double latitude = null;
                            Double longitude = null;

                            try {
                                ExifInterface exif = new ExifInterface(upload.getFileUri().getPath());
                                ExifGeoDegree exifGeoDegree = new ExifGeoDegree(exif);
                                if (exifGeoDegree.isValid()) {
                                    latitude = exifGeoDegree.getLatitude();
                                    longitude = exifGeoDegree.getLongitude();
                                }
                            } catch (Exception ignored) {
                            }

                            return networker
                                    .vkDefault(accountId)
                                    .photos()
                                    .save(albumId, groupId, dto.server, dto.photosList, dto.hash, latitude, longitude, null)
                                    .flatMap(photos -> {
                                        if(photos.isEmpty()){
                                            return Single.error(new NotFoundException());
                                        }

                                        PhotoEntity entity = Dto2Entity.mapPhoto(photos.get(0));
                                        Photo photo = Dto2Model.transform(photos.get(0));
                                        Single<UploadResult<Photo>> result = Single.just(new UploadResult<>(server, photo));
                                        return upload.isAutoCommit() ? commit(storage, upload, entity).andThen(result) : result;
                                    });
                        });
            } catch (Exception e){
                safelyClose(is[0]);
                return Single.error(e);
            }
        });
    }

    private Completable commit(IPhotosStorage storage, Upload upload, PhotoEntity entity){
        return storage.insertPhotosRx(upload.getAccountId(), entity.getOwnerId(), entity.getAlbumId(), Collections.singletonList(entity), false);
    }
}