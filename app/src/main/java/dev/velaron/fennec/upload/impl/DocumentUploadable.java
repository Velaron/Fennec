package dev.velaron.fennec.upload.impl;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import dev.velaron.fennec.api.PercentagePublisher;
import dev.velaron.fennec.api.interfaces.INetworker;
import dev.velaron.fennec.api.model.server.UploadServer;
import dev.velaron.fennec.db.interfaces.IDocsStorage;
import dev.velaron.fennec.db.model.entity.DocumentEntity;
import dev.velaron.fennec.domain.mappers.Dto2Entity;
import dev.velaron.fennec.domain.mappers.Dto2Model;
import dev.velaron.fennec.exception.NotFoundException;
import dev.velaron.fennec.model.Document;
import dev.velaron.fennec.upload.IUploadable;
import dev.velaron.fennec.upload.Upload;
import dev.velaron.fennec.upload.UploadResult;
import io.reactivex.Completable;
import io.reactivex.Single;

import static dev.velaron.fennec.util.RxUtils.safelyCloseAction;
import static dev.velaron.fennec.util.Utils.safelyClose;

public class DocumentUploadable implements IUploadable<Document> {

    private final Context context;
    private final INetworker networker;
    private final IDocsStorage storage;

    public DocumentUploadable(Context context, INetworker networker, IDocsStorage storage) {
        this.context = context;
        this.networker = networker;
        this.storage = storage;
    }

    @Override
    public Single<UploadResult<Document>> doUpload(@NonNull Upload upload, @Nullable UploadServer initialServer, @Nullable PercentagePublisher listener) {
        final int ownerId = upload.getDestination().getOwnerId();
        final Integer groupId = ownerId >= 0 ? null : ownerId;
        final int accountId = upload.getAccountId();

        Single<UploadServer> serverSingle;
        if(initialServer == null){
            serverSingle = networker.vkDefault(accountId)
                    .docs()
                    .getUploadServer(groupId, null)
                    .map(s -> s);
        } else {
            serverSingle = Single.just(initialServer);
        }

        return serverSingle.flatMap(server -> {
            final InputStream[] is = new InputStream[1];

            try {
                Uri uri = upload.getFileUri();

                File file = new File(uri.getPath());
                if(file.isFile()){
                    is[0] = new FileInputStream(file);
                } else {
                    is[0] = context.getContentResolver().openInputStream(uri);
                }

                if(is[0] == null){
                    return Single.error(new NotFoundException("Unable to open InputStream, URI: " + uri));
                }

                final String filename = findFileName(context, uri);
                return networker.uploads()
                        .uploadDocumentRx(server.getUrl(), filename, is[0], listener)
                        .doFinally(safelyCloseAction(is[0]))
                        .flatMap(dto -> networker
                                .vkDefault(accountId)
                                .docs()
                                .save(dto.file, filename, null)
                                .flatMap(tmpList -> {
                                    if(tmpList.isEmpty()){
                                        return Single.error(new NotFoundException());
                                    }

                                    Document document = Dto2Model.transform(tmpList.get(0));
                                    UploadResult<Document> result = new UploadResult<>(server, document);

                                    if(upload.isAutoCommit()){
                                        DocumentEntity entity = Dto2Entity.mapDoc(tmpList.get(0));
                                        return commit(storage, upload, entity).andThen(Single.just(result));
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

    private static String findFileName(Context context, Uri uri) {
        String fileName = uri.getLastPathSegment();
        try {
            String scheme = uri.getScheme();
            if (scheme.equals("file")) {
                fileName = uri.getLastPathSegment();
            } else if (scheme.equals("content")) {
                String[] proj = {MediaStore.Images.Media.TITLE};

                Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
                if (cursor != null && cursor.getCount() != 0) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE);
                    cursor.moveToFirst();
                    fileName = cursor.getString(columnIndex);
                }

                if (cursor != null) {
                    cursor.close();
                }
            }

        } catch (Exception ignored) {

        }

        return fileName;
    }

    private Completable commit(IDocsStorage storage, Upload upload, DocumentEntity entity){
        return storage.store(upload.getAccountId(), entity.getOwnerId(), Collections.singletonList(entity), false);
    }
}