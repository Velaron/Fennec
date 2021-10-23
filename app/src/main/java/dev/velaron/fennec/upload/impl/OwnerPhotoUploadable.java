package dev.velaron.fennec.upload.impl;

import android.content.Context;

import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import dev.velaron.fennec.api.PercentagePublisher;
import dev.velaron.fennec.api.interfaces.INetworker;
import dev.velaron.fennec.api.model.server.UploadServer;
import dev.velaron.fennec.domain.IWallsRepository;
import dev.velaron.fennec.exception.NotFoundException;
import dev.velaron.fennec.model.Post;
import dev.velaron.fennec.upload.IUploadable;
import dev.velaron.fennec.upload.Upload;
import dev.velaron.fennec.upload.UploadResult;
import dev.velaron.fennec.upload.UploadUtils;
import io.reactivex.Single;

import static dev.velaron.fennec.util.Utils.safelyClose;

public class OwnerPhotoUploadable implements IUploadable<Post> {

    private final Context context;
    private final INetworker networker;
    private final IWallsRepository walls;

    public OwnerPhotoUploadable(Context context, INetworker networker, IWallsRepository walls) {
        this.context = context;
        this.networker = networker;
        this.walls = walls;
    }

    @Override
    public Single<UploadResult<Post>> doUpload(@NonNull Upload upload, @Nullable UploadServer initialServer, @Nullable PercentagePublisher listener) {
        final int accountId = upload.getAccountId();
        final int ownerId = upload.getDestination().getOwnerId();

        Single<UploadServer> serverSingle;
        if (initialServer == null) {
            serverSingle = networker.vkDefault(accountId)
                    .photos()
                    .getOwnerPhotoUploadServer(ownerId)
                    .map(s -> s);
        } else {
            serverSingle = Single.just(initialServer);
        }

        return serverSingle.flatMap(server -> {
            final InputStream[] is = new InputStream[1];

            try {
                is[0] = UploadUtils.openStream(context, upload.getFileUri(), upload.getSize());
                return networker.uploads()
                        .uploadOwnerPhotoRx(server.getUrl(), is[0], listener)
                        .doFinally(() -> safelyClose(is[0]))
                        .flatMap(dto -> networker.vkDefault(accountId)
                                .photos()
                                .saveOwnerPhoto(dto.server, dto.hash, dto.photo)
                                .flatMap(response -> {
                                    if (response.postId == 0) {
                                        return Single.error(new NotFoundException("Post id=0"));
                                    }

                                    return walls.getById(accountId, ownerId, response.postId)
                                            .map(post -> new UploadResult<>(server, post));
                                }));
            } catch (Exception e) {
                safelyClose(is[0]);
                return Single.error(e);
            }
        });
    }
}