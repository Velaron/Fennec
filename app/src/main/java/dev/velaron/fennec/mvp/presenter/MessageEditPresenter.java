package dev.velaron.fennec.mvp.presenter;

import static dev.velaron.fennec.util.Objects.isNull;
import static dev.velaron.fennec.util.Objects.nonNull;
import static dev.velaron.fennec.util.Utils.findIndexByPredicate;
import static dev.velaron.fennec.util.Utils.nonEmpty;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import biz.dealnote.mvp.reflect.OnGuiCreated;
import dev.velaron.fennec.Injection;
import dev.velaron.fennec.model.AbsModel;
import dev.velaron.fennec.model.AttachmenEntry;
import dev.velaron.fennec.model.Attachments;
import dev.velaron.fennec.model.FwdMessages;
import dev.velaron.fennec.model.LocalPhoto;
import dev.velaron.fennec.model.Message;
import dev.velaron.fennec.model.Photo;
import dev.velaron.fennec.mvp.presenter.base.RxSupportPresenter;
import dev.velaron.fennec.mvp.view.IMessageEditView;
import dev.velaron.fennec.settings.Settings;
import dev.velaron.fennec.upload.IUploadManager;
import dev.velaron.fennec.upload.Upload;
import dev.velaron.fennec.upload.UploadDestination;
import dev.velaron.fennec.upload.UploadIntent;
import dev.velaron.fennec.upload.UploadUtils;
import dev.velaron.fennec.util.AppPerms;
import dev.velaron.fennec.util.FileUtil;

/**
 * Created by admin on 14.04.2017.
 * phoenix
 */
public class MessageEditPresenter extends RxSupportPresenter<IMessageEditView> {

    private static final String SAVE_CAMERA_FILE_URI = "save-camera-file-uri";
    private final Message message;

    private final List<AttachmenEntry> entries;
    private final UploadDestination destination;
    private final IUploadManager uploadManager;

    private final int accountId;
    private Uri currentPhotoCameraUri;

    public MessageEditPresenter(int accountId, Message message, @Nullable Bundle savedInstanceState) {
        super(savedInstanceState);
        this.message = message;
        this.accountId = accountId;
        this.destination = UploadDestination.forMessage(message.getId());
        this.entries = new ArrayList<>();
        this.uploadManager = Injection.provideUploadManager();

        if (nonNull(savedInstanceState)) {
            this.currentPhotoCameraUri = savedInstanceState.getParcelable(SAVE_CAMERA_FILE_URI);
        }

        appendDisposable(uploadManager.observeAdding()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadsAdded));

        appendDisposable(uploadManager.observeDeleting(true)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadsRemoved));

        appendDisposable(uploadManager.obseveStatus()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadStatusChanges));

        appendDisposable(uploadManager.observeProgress()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadProgressUpdates));

        if (nonEmpty(message.getFwd())) {
            entries.add(new AttachmenEntry(true, new FwdMessages(message.getFwd())));
        }

        if (message.isHasAttachments()) {
            Attachments attachments = message.getAttachments();
            List<AbsModel> list = attachments.toList();

            for (AbsModel model : list) {
                entries.add(new AttachmenEntry(true, model));
            }
        }
    }

    @OnGuiCreated
    private void resolveEmptyViewVisibility() {
        if (isGuiReady()) {
            getView().setEmptyViewVisible(entries.isEmpty());
        }
    }

    private void onUploadProgressUpdates(List<IUploadManager.IProgressUpdate> updates) {
        for (IUploadManager.IProgressUpdate update : updates) {
            int index = findUploadObjectIndex(update.getId());
            if (index != -1) {
                Upload upload = (Upload) entries.get(index).getAttachment();
                if (upload.getStatus() != Upload.STATUS_UPLOADING) {
                    // for uploading only
                    continue;
                }

                upload.setProgress(update.getProgress());
                callView(view -> view.changePercentageSmoothly(index, update.getProgress()));
            }
        }
    }

    private void onUploadStatusChanges(Upload upload) {
        int index = findUploadObjectIndex(upload.getId());
        if (index != -1) {
            ((Upload) entries.get(index).getAttachment())
                    .setStatus(upload.getStatus())
                    .setErrorText(upload.getErrorText());

            callView(view -> view.notifyItemChanged(index));
        }
    }

    private void onUploadsRemoved(int[] ids) {
        for (int id : ids) {
            int index = findUploadObjectIndex(id);
            if (index != -1) {
                entries.remove(index);
                callView(view -> view.notifyEntryRemoved(index));
                resolveEmptyViewVisibility();
            }
        }
    }

    private void onUploadsAdded(List<Upload> uploads) {
        int count = 0;
        for (int i = uploads.size() - 1; i >= 0; i--) {
            Upload upload = uploads.get(i);
            if (this.destination.compareTo(upload.getDestination())) {
                AttachmenEntry entry = new AttachmenEntry(true, upload);
                entries.add(0, entry);
                count++;
            }
        }

        int finalCount = count;
        callView(view -> view.notifyDataAdded(0, finalCount));
        resolveEmptyViewVisibility();
    }

    private int findUploadObjectIndex(int id) {
        return findIndexByPredicate(entries, entry -> {
            AbsModel model = entry.getAttachment();
            return model instanceof Upload && ((Upload) model).getId() == id;
        });
    }

    @Override
    public void onGuiCreated(@NonNull IMessageEditView view) {
        super.onGuiCreated(view);
        view.displayAttachments(entries);
    }

    public void fireAddPhotoButtonClick() {
        // Если сообщения группы - предлагать фотографии сообщества, а не группы
        getView().addPhoto(accountId, message.getAccountId());
    }

    public void firePhotosSelected(ArrayList<Photo> photos, ArrayList<LocalPhoto> localPhotos) {
        if (nonEmpty(photos)) {
            fireAttachmentsSelected(photos);
        } else if (nonEmpty(localPhotos)) {
            doUploadPhotos(localPhotos);
        }
    }

    private void doUploadPhotos(List<LocalPhoto> photos) {
        Integer size = Settings.get()
                .main()
                .getUploadImageSize();

        if (isNull(size)) {
            getView().displaySelectUploadPhotoSizeDialog(photos);
        } else {
            doUploadPhotos(photos, size);
        }
    }

    private void doUploadPhotos(List<LocalPhoto> photos, int size) {
        List<UploadIntent> intents = UploadUtils.createIntents(accountId, destination, photos, size, true);
        uploadManager.enqueue(intents);
    }

    public void fireRemoveClick(AttachmenEntry entry) {
        if (entry.getAttachment() instanceof Upload) {
            uploadManager.cancel(((Upload) entry.getAttachment()).getId());
            return;
        }

        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getId() == entry.getId()) {
                entries.remove(i);
                getView().notifyEntryRemoved(i);
                break;
            }
        }
    }

    public void fireUploadPhotoSizeSelected(List<LocalPhoto> photos, int imageSize) {
        doUploadPhotos(photos, imageSize);
    }

    public void fireCameraPermissionResolved() {
        if (AppPerms.hasCameraPermision(getApplicationContext())) {
            makePhotoInternal();
        }
    }

    public void fireButtonCameraClick() {
        if (AppPerms.hasCameraPermision(getApplicationContext())) {
            makePhotoInternal();
        } else {
            getView().requestCameraPermission();
        }
    }

    private void makePhotoInternal() {
        try {
            File file = FileUtil.createImageFile();
            this.currentPhotoCameraUri = FileUtil.getExportedUriForFile(getApplicationContext(), file);
            getView().startCamera(currentPhotoCameraUri);
        } catch (IOException e) {
            safeShowError(getView(), e.getMessage());
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putParcelable(SAVE_CAMERA_FILE_URI, currentPhotoCameraUri);
    }

    public void firePhotoMaked() {
        final Uri uri = this.currentPhotoCameraUri;
        this.currentPhotoCameraUri = null;

        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
        getApplicationContext().sendBroadcast(scanIntent);

        LocalPhoto makedPhoto = new LocalPhoto().setFullImageUri(uri);
        doUploadPhotos(Collections.singletonList(makedPhoto));
    }

    public void fireButtonVideoClick() {
        getView().startAddVideoActivity(accountId, message.getAccountId());
    }

    public void fireButtonDocClick() {
        getView().startAddDocumentActivity(accountId); // TODO: 16.08.2017
    }

    public void fireAttachmentsSelected(ArrayList<? extends AbsModel> models) {
        int startCount = entries.size();

        for(AbsModel model : models){
            entries.add(new AttachmenEntry(true, model));
        }

        resolveEmptyViewVisibility();
        callView(view -> view.notifyDataAdded(startCount, models.size()));
    }
}