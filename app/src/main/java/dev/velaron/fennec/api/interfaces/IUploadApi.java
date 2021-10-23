package dev.velaron.fennec.api.interfaces;

import androidx.annotation.NonNull;

import java.io.InputStream;

import dev.velaron.fennec.api.PercentagePublisher;
import dev.velaron.fennec.api.model.upload.UploadDocDto;
import dev.velaron.fennec.api.model.upload.UploadOwnerPhotoDto;
import dev.velaron.fennec.api.model.upload.UploadPhotoToAlbumDto;
import dev.velaron.fennec.api.model.upload.UploadPhotoToMessageDto;
import dev.velaron.fennec.api.model.upload.UploadPhotoToWallDto;
import io.reactivex.Single;

/**
 * Created by Ruslan Kolbasa on 31.07.2017.
 * phoenix
 */
public interface IUploadApi {
    Single<UploadDocDto> uploadDocumentRx(String server, String filename, @NonNull InputStream doc, PercentagePublisher listener);

    Single<UploadOwnerPhotoDto> uploadOwnerPhotoRx(String server, @NonNull InputStream photo, PercentagePublisher listener);

    Single<UploadPhotoToWallDto> uploadPhotoToWallRx(String server, @NonNull InputStream photo, PercentagePublisher listener);

    Single<UploadPhotoToMessageDto> uploadPhotoToMessageRx(String server, @NonNull InputStream is, PercentagePublisher listener);

    Single<UploadPhotoToAlbumDto> uploadPhotoToAlbumRx(String server, @NonNull InputStream file1, PercentagePublisher listener);
}