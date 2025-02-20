package dev.velaron.fennec.mvp.presenter;

import static dev.velaron.fennec.util.AppPerms.hasCameraPermision;
import static dev.velaron.fennec.util.AppPerms.hasReadStoragePermision;
import static dev.velaron.fennec.util.Objects.isNull;
import static dev.velaron.fennec.util.Objects.nonNull;
import static dev.velaron.fennec.util.Utils.findInfoByPredicate;
import static dev.velaron.fennec.util.Utils.nonEmpty;
import static dev.velaron.fennec.util.Utils.safeCountOfMultiple;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.velaron.fennec.mvp.reflect.OnGuiCreated;
import dev.velaron.fennec.Injection;
import dev.velaron.fennec.R;
import dev.velaron.fennec.model.AbsModel;
import dev.velaron.fennec.model.AttachmenEntry;
import dev.velaron.fennec.model.Audio;
import dev.velaron.fennec.model.Document;
import dev.velaron.fennec.model.LocalPhoto;
import dev.velaron.fennec.model.Photo;
import dev.velaron.fennec.model.Poll;
import dev.velaron.fennec.model.Video;
import dev.velaron.fennec.mvp.presenter.base.AccountDependencyPresenter;
import dev.velaron.fennec.mvp.view.IBaseAttachmentsEditView;
import dev.velaron.fennec.settings.Settings;
import dev.velaron.fennec.upload.IUploadManager;
import dev.velaron.fennec.upload.Upload;
import dev.velaron.fennec.util.FileUtil;
import dev.velaron.fennec.util.Pair;
import dev.velaron.fennec.util.Predicate;

/**
 * Created by admin on 05.12.2016.
 * phoenix
 */
public abstract class AbsAttachmentsEditPresenter<V extends IBaseAttachmentsEditView>
        extends AccountDependencyPresenter<V> {

    private static final String SAVE_DATA = "save_data";
    private static final String SAVE_TIMER = "save_timer";
    private static final String SAVE_BODY = "save_body";
    private static final String SAVE_CURRENT_PHOTO_CAMERA_URI = "save_current_photo_camera_uri";

    private String textBody;
    private final ArrayList<AttachmenEntry> data;
    private Uri currentPhotoCameraUri;
    private Long timerValue;
    final IUploadManager uploadManager;

    AbsAttachmentsEditPresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.uploadManager = Injection.provideUploadManager();

        if (nonNull(savedInstanceState)) {
            currentPhotoCameraUri = savedInstanceState.getParcelable(SAVE_CURRENT_PHOTO_CAMERA_URI);
            textBody = savedInstanceState.getString(SAVE_BODY);
            timerValue = savedInstanceState.containsKey(SAVE_TIMER) ? savedInstanceState.getLong(SAVE_TIMER) : null;
        }

        data = new ArrayList<>();
        if(nonNull(savedInstanceState)){
            ArrayList<AttachmenEntry> savedEntries = savedInstanceState.getParcelableArrayList(SAVE_DATA);
            if(nonEmpty(savedEntries)){
                data.addAll(savedEntries);
            }
        }
    }

    Long getTimerValue() {
        return timerValue;
    }

    ArrayList<AttachmenEntry> getData() {
        return data;
    }

    @OnGuiCreated
    void resolveTimerInfoView(){
        if(isGuiReady()){
            getView().setTimerValue(timerValue);
        }
    }

    void setTimerValue(Long timerValue) {
        this.timerValue = timerValue;
        resolveTimerInfoView();
    }

    @OnGuiCreated
    void resolveTextView() {
        if (isGuiReady()) {
            getView().setTextBody(textBody);
        }
    }

    String getTextBody() {
        return textBody;
    }

    void setTextBody(String body) {
        this.textBody = body;
        resolveTextView();
    }

    ArrayList<AttachmenEntry> getNeedParcelSavingEntries(){
        return new ArrayList<>(0);
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putParcelable(SAVE_CURRENT_PHOTO_CAMERA_URI, currentPhotoCameraUri);
        outState.putParcelableArrayList(SAVE_DATA, getNeedParcelSavingEntries());
        outState.putString(SAVE_BODY, textBody);
        if(nonNull(timerValue)){
            outState.putLong(SAVE_TIMER, timerValue);
        }
    }

    void onUploadProgressUpdate(List<IUploadManager.IProgressUpdate> updates){
        for(IUploadManager.IProgressUpdate update : updates){
            Predicate<AttachmenEntry> predicate = entry -> entry.getAttachment() instanceof Upload
                    && ((Upload) entry.getAttachment()).getId() == update.getId();

            Pair<Integer, AttachmenEntry> info = findInfoByPredicate(getData(), predicate);

            if(nonNull(info)){
                AttachmenEntry entry = info.getSecond();

                Upload object = (Upload) entry.getAttachment();
                if(object.getStatus() != Upload.STATUS_UPLOADING) {
                    continue;
                }

                callView(v -> v.updateProgressAtIndex(entry.getId(), update.getProgress()));
            }
        }
    }

    void onUploadObjectRemovedFromQueue(int[] ids){
        for(int id : ids){
            int index = findUploadIndexById(id);
            if(index != -1){
                manuallyRemoveElement(index);
            }
        }
    }

    void onUploadQueueUpdates(List<Upload> updates, Predicate<Upload> predicate){
        int startSize = data.size();
        int count = 0;

        for(Upload u : updates){
            if(predicate.test(u)){
                data.add(new AttachmenEntry(true, u));
                count++;
            }
        }

        if(count > 0){
            safelyNotifyItemsAdded(startSize, count);
        }
    }

    void safelyNotifyItemsAdded(int position, int count){
        if (isGuiReady()){
            getView().notifyItemRangeInsert(position, count);
        }
    }

    List<AttachmenEntry> combine(List<AttachmenEntry> first, List<AttachmenEntry> second){
        List<AttachmenEntry> data = new ArrayList<>(safeCountOfMultiple(first, second));
        data.addAll(first);
        data.addAll(second);
        return data;
    }

    void onUploadStatusUpdate(Upload update){
        int index = findUploadIndexById(update.getId());
        if(index != -1){
            safeNotifyDataSetChanged();
        }
    }

    static List<AttachmenEntry> createFrom(List<Upload> objects){
        List<AttachmenEntry> data = new ArrayList<>(objects.size());
        for (Upload object : objects) {
            data.add(new AttachmenEntry(true, object));
        }

        return data;
    }

    static List<AttachmenEntry> createFrom(List<Pair<Integer, AbsModel>> pairs, boolean canDelete){
        List<AttachmenEntry> data = new ArrayList<>(pairs.size());
        for (Pair<Integer, AbsModel> pair : pairs) {
            data.add(new AttachmenEntry(canDelete, pair.getSecond()).setOptionalId(pair.getFirst()));
        }
        return data;
    }

    @Override
    public void onGuiCreated(@NonNull V viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayInitialModels(data);
    }

    public final void fireRemoveClick(int index, @NonNull AttachmenEntry attachment) {
        if (attachment.getAttachment() instanceof Upload) {
            Upload upload = (Upload) attachment.getAttachment();
            uploadManager.cancel(upload.getId());
            return;
        }

        onAttachmentRemoveClick(index, attachment);
    }

    void safelyNotifyItemRemoved(int position){
        if(isGuiReady()){
            getView().notifyItemRemoved(position);
        }
    }

    void onAttachmentRemoveClick(int index, @NonNull AttachmenEntry attachment) {
        throw new UnsupportedOperationException();
    }

    void manuallyRemoveElement(int index) {
        data.remove(index);
        safelyNotifyItemRemoved(index);

        //safeNotifyDataSetChanged();
    }

    public final void fireTitleClick(int index, @NonNull AttachmenEntry attachment) {

    }

    private int getMaxCountOfAttachments() {
        return 10;
    }

    private boolean canAttachMore() {
        return data.size() < getMaxCountOfAttachments();
    }

    private int getMaxFutureAttachmentCount() {
        int count = data.size() - getMaxCountOfAttachments();
        return count < 0 ? 0 : count;
    }

    public final void firePhotoFromVkChoose() {
        getView().openAddVkPhotosWindow(getMaxFutureAttachmentCount(), getAccountId(), getAccountId());
    }

    private boolean checkAbilityToAttachMore() {
        if (canAttachMore()) {
            return true;
        } else {
            safeShowError(getView(), R.string.reached_maximum_count_of_attachments);
            return false;
        }
    }

    public final void firePhotoFromLocalGalleryChoose() {
        if (!hasReadStoragePermision(getApplicationContext())) {
            getView().requestReadExternalStoragePermission();
            return;
        }

        getView().openAddPhotoFromGalleryWindow(getMaxFutureAttachmentCount());
    }

    public final void firePhotoFromCameraChoose() {
        if (!hasCameraPermision(getApplicationContext())) {
            getView().requestCameraPermission();
            return;
        }

        createImageFromCamera();
    }

    private void createImageFromCamera() {
        try {
            File photoFile = FileUtil.createImageFile();
            currentPhotoCameraUri = FileUtil.getExportedUriForFile(getApplicationContext(), photoFile);

            getView().openCamera(currentPhotoCameraUri);
        } catch (IOException e) {
            safeShowError(getView(), e.getMessage());
        }
    }

    public final void firePhotoMaked() {
        getView().notifySystemAboutNewPhoto(currentPhotoCameraUri);

        LocalPhoto makedPhoto = new LocalPhoto().setFullImageUri(currentPhotoCameraUri);
        doUploadPhotos(Collections.singletonList(makedPhoto));
    }

    protected void doUploadPhotos(List<LocalPhoto> photos, int size) {
        throw new UnsupportedOperationException();
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

    public final void firePhotosFromGallerySelected(ArrayList<LocalPhoto> photos) {
        doUploadPhotos(photos);
    }

    public final void fireButtonPhotoClick() {
        if (checkAbilityToAttachMore()) {
            getView().displayChoosePhotoTypeDialog();
        }
    }

    public final void fireButtonAudioClick() {
        if (checkAbilityToAttachMore()) {
            getView().openAddAudiosWindow(getMaxFutureAttachmentCount(), getAccountId());
        }
    }

    public final void fireButtonVideoClick() {
        if (checkAbilityToAttachMore()) {
            getView().openAddVideosWindow(getMaxFutureAttachmentCount(), getAccountId());
        }
    }

    public final void fireButtonDocClick() {
        if (checkAbilityToAttachMore()) {
            getView().openAddDocumentsWindow(getMaxFutureAttachmentCount(), getAccountId());
        }
    }

    protected void onPollCreateClick() {
        throw new UnsupportedOperationException();
    }

    protected void onTimerClick() {
        throw new UnsupportedOperationException();
    }

    public final void fireButtonPollClick() {
        onPollCreateClick();
    }

    public final void fireButtonTimerClick() {
        onTimerClick();
    }

    protected void onModelsAdded(List<? extends AbsModel> models) {
        for (AbsModel model : models) {
            data.add(new AttachmenEntry(true, model));
        }

        safeNotifyDataSetChanged();
    }

    public final void fireAudiosSelected(@NonNull ArrayList<Audio> audios) {
        onModelsAdded(audios);
    }

    public final void fireVideosSelected(@NonNull ArrayList<Video> videos) {
        onModelsAdded(videos);
    }

    public final void fireDocumentsSelected(@NonNull ArrayList<Document> documents) {
        onModelsAdded(documents);
    }

    public void fireUploadPhotoSizeSelected(@NonNull List<LocalPhoto> photos, int size) {
        doUploadPhotos(photos, size);
    }

    public final void firePollCreated(@NonNull Poll poll) {
        onModelsAdded(Collections.singletonList(poll));
    }

    protected void safeNotifyDataSetChanged() {
        if (isGuiReady()) {
            getView().notifyDataSetChanged();
        }
    }

    public final void fireTextChanged(CharSequence s) {
        textBody = isNull(s) ? null : s.toString();
    }

    public final void fireVkPhotosSelected(@NonNull ArrayList<Photo> photos) {
        onModelsAdded(photos);
    }

    public void fireCameraPermissionResolved() {
        if (hasCameraPermision(getApplicationContext())) {
            createImageFromCamera();
        }
    }

    public void fireReadStoragePermissionResolved() {
        if (hasReadStoragePermision(getApplicationContext())) {
            getView().openAddPhotoFromGalleryWindow(getMaxFutureAttachmentCount());
        }
    }

    boolean hasUploads(){
        for(AttachmenEntry entry : data){
            if(entry.getAttachment() instanceof Upload){
                return true;
            }
        }

        return false;
    }

    int findUploadIndexById(int id) {
        for (int i = 0; i < data.size(); i++) {
            AttachmenEntry item = data.get(i);
            if (item.getAttachment() instanceof Upload && ((Upload) item.getAttachment()).getId() == id) {
                return i;
            }
        }

        return -1;
    }

    public void fireTimerTimeSelected(long unixtime) {
        throw new UnsupportedOperationException();
    }
}
