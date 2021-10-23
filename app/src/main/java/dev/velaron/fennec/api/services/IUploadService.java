package dev.velaron.fennec.api.services;

import dev.velaron.fennec.api.model.upload.UploadDocDto;
import dev.velaron.fennec.api.model.upload.UploadOwnerPhotoDto;
import dev.velaron.fennec.api.model.upload.UploadPhotoToAlbumDto;
import dev.velaron.fennec.api.model.upload.UploadPhotoToMessageDto;
import dev.velaron.fennec.api.model.upload.UploadPhotoToWallDto;
import io.reactivex.Single;
import okhttp3.MultipartBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Url;

/**
 * Created by ruslan.kolbasa on 26.12.2016.
 * phoenix
 */
public interface IUploadService {

    @Multipart
    @POST
    Single<UploadDocDto> uploadDocumentRx(@Url String server, @Part MultipartBody.Part file);

    @Multipart
    @POST
    Single<UploadOwnerPhotoDto> uploadOwnerPhotoRx(@Url String server, @Part MultipartBody.Part photo);

    @Multipart
    @POST
    Single<UploadPhotoToWallDto> uploadPhotoToWallRx(@Url String server, @Part MultipartBody.Part photo);

    @Multipart
    @POST
    Single<UploadPhotoToMessageDto> uploadPhotoToMessageRx(@Url String server, @Part MultipartBody.Part photo);

    @Multipart
    @POST
    Single<UploadPhotoToAlbumDto> uploadPhotoToAlbumRx(@Url String server, @Part MultipartBody.Part file1);
}