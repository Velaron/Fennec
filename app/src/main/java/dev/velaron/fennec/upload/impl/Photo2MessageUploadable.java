package dev.velaron.fennec.upload.impl;

import android.content.Context;

import java.io.InputStream;
import java.util.Collections;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import dev.velaron.fennec.api.PercentagePublisher;
import dev.velaron.fennec.api.interfaces.INetworker;
import dev.velaron.fennec.api.model.server.UploadServer;
import dev.velaron.fennec.db.AttachToType;
import dev.velaron.fennec.db.interfaces.IMessagesStorage;
import dev.velaron.fennec.domain.IAttachmentsRepository;
import dev.velaron.fennec.domain.mappers.Dto2Model;
import dev.velaron.fennec.exception.NotFoundException;
import dev.velaron.fennec.model.Photo;
import dev.velaron.fennec.upload.IUploadable;
import dev.velaron.fennec.upload.Upload;
import dev.velaron.fennec.upload.UploadResult;
import dev.velaron.fennec.upload.UploadUtils;
import io.reactivex.Completable;
import io.reactivex.Single;

import static dev.velaron.fennec.util.Objects.nonNull;
import static dev.velaron.fennec.util.RxUtils.safelyCloseAction;
import static dev.velaron.fennec.util.Utils.safelyClose;

public class Photo2MessageUploadable implements IUploadable<Photo> {

    private final Context context;
    private final INetworker networker;
    private final IAttachmentsRepository attachmentsRepository;
    private final IMessagesStorage messagesStorage;

    public Photo2MessageUploadable(Context context, INetworker networker, IAttachmentsRepository attachmentsRepository, IMessagesStorage messagesStorage) {
        this.context = context;
        this.networker = networker;
        this.attachmentsRepository = attachmentsRepository;
        this.messagesStorage = messagesStorage;
    }

    @Override
    public Single<UploadResult<Photo>> doUpload(@NonNull Upload upload,
                                                @Nullable UploadServer initialServer,
                                                @Nullable PercentagePublisher listener) {
        final int accountId = upload.getAccountId();
        final int messageId = upload.getDestination().getId();

        Single<UploadServer> serverSingle;
        if (nonNull(initialServer)) {
            serverSingle = Single.just(initialServer);
        } else {
            serverSingle = networker.vkDefault(accountId)
                    .photos()
                    .getMessagesUploadServer().map(s -> s);
        }

        return serverSingle.flatMap(server -> {
            final InputStream[] is = new InputStream[1];

            try {
                is[0] = UploadUtils.openStream(context, upload.getFileUri(), upload.getSize());
                return networker.uploads()
                        .uploadPhotoToMessageRx(server.getUrl(), is[0], listener)
                        .doFinally(safelyCloseAction(is[0]))
                        .flatMap(dto -> networker.vkDefault(accountId)
                                .photos()
                                .saveMessagesPhoto(dto.server, dto.photo, dto.hash)
                                .flatMap(photos -> {
                                    if(photos.isEmpty()){
                                        return Single.error(new NotFoundException());
                                    }

                                    Photo photo = Dto2Model.transform(photos.get(0));
                                    UploadResult<Photo> result = new UploadResult<>(server, photo);

                                    if(upload.isAutoCommit()){
                                        return attachIntoDatabaseRx(attachmentsRepository, messagesStorage, accountId, messageId, photo)
                                                .andThen(Single.just(result));
                                    } else {
                                        return Single.just(result);
                                    }
                                }));
            } catch (Exception e){
                safelyClose(is[0]);
                return Single.error(e);
            }
        });
    }

    private static Completable attachIntoDatabaseRx(IAttachmentsRepository repository, IMessagesStorage storage,
                                                    int accountId, int messageId, Photo photo){
        return repository
                .attach(accountId, AttachToType.MESSAGE, messageId, Collections.singletonList(photo))
                .andThen(storage.notifyMessageHasAttachments(accountId, messageId));
    }
}