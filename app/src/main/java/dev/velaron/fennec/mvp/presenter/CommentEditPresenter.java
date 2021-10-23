package dev.velaron.fennec.mvp.presenter;

import static dev.velaron.fennec.util.Objects.isNull;
import static dev.velaron.fennec.util.Objects.nonNull;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.mvp.reflect.OnGuiCreated;
import dev.velaron.fennec.Injection;
import dev.velaron.fennec.R;
import dev.velaron.fennec.domain.ICommentsInteractor;
import dev.velaron.fennec.domain.Repository;
import dev.velaron.fennec.domain.impl.CommentsInteractor;
import dev.velaron.fennec.model.AbsModel;
import dev.velaron.fennec.model.AttachmenEntry;
import dev.velaron.fennec.model.Comment;
import dev.velaron.fennec.model.Commented;
import dev.velaron.fennec.model.LocalPhoto;
import dev.velaron.fennec.model.Photo;
import dev.velaron.fennec.mvp.view.ICommentEditView;
import dev.velaron.fennec.upload.Method;
import dev.velaron.fennec.upload.Upload;
import dev.velaron.fennec.upload.UploadDestination;
import dev.velaron.fennec.upload.UploadIntent;
import dev.velaron.fennec.upload.UploadResult;
import dev.velaron.fennec.upload.UploadUtils;
import dev.velaron.fennec.util.Pair;
import dev.velaron.fennec.util.Predicate;
import dev.velaron.fennec.util.RxUtils;
import dev.velaron.fennec.util.Utils;

/**
 * Created by admin on 06.05.2017.
 * phoenix
 */
public class CommentEditPresenter extends AbsAttachmentsEditPresenter<ICommentEditView> {

    private static final String TAG = CommentEditPresenter.class.getSimpleName();

    private final Comment orig;
    private final UploadDestination destination;
    private boolean editingNow;
    private boolean canGoBack;

    private final ICommentsInteractor commentsInteractor;

    public CommentEditPresenter(Comment comment, int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.commentsInteractor = new CommentsInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores(), Repository.INSTANCE.getOwners());
        this.orig = comment;
        this.destination = new UploadDestination(comment.getId(), comment.getCommented().getSourceOwnerId(), Method.PHOTO_TO_COMMENT);

        if (isNull(savedInstanceState)) {
            super.setTextBody(orig.getText());
            initialPopulateEntries();
        }

        appendDisposable(uploadManager.get(getAccountId(), destination)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onUploadsReceived));

        Predicate<Upload> predicate = upload -> upload.getAccountId() == getAccountId() && destination.compareTo(upload.getDestination());

        appendDisposable(uploadManager.observeAdding()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(added -> onUploadQueueUpdates(added, predicate)));

        appendDisposable(uploadManager.observeDeleting(false)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadObjectRemovedFromQueue));

        appendDisposable(uploadManager.obseveStatus()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadStatusUpdate));

        appendDisposable(uploadManager.observeProgress()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadProgressUpdate));

        appendDisposable(uploadManager.observeResults()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadsQueueChanged));
    }

    private void onUploadsQueueChanged(Pair<Upload, UploadResult<?>> pair) {
        Upload upload = pair.getFirst();
        UploadResult<?> result = pair.getSecond();

        int index = findUploadIndexById(upload.getId());

        final AttachmenEntry entry;
        if (result.getResult() instanceof Photo) {
            entry = new AttachmenEntry(true, (Photo) result.getResult());
        } else {
            // not supported!!!
            return;
        }

        if (index != -1) {
            getData().set(index, entry);
        } else {
            getData().add(0, entry);
        }

        safeNotifyDataSetChanged();
    }

    @Override
    void onAttachmentRemoveClick(int index, @NonNull AttachmenEntry attachment) {
        super.manuallyRemoveElement(index);
    }

    @Override
    protected void doUploadPhotos(List<LocalPhoto> photos, int size) {
        List<UploadIntent> intents = UploadUtils.createIntents(getAccountId(), destination, photos, size, false);
        uploadManager.enqueue(intents);
    }

    private void onUploadsReceived(List<Upload> uploads) {
        getData().addAll(createFrom(uploads));
        safeNotifyDataSetChanged();
    }

    @Override
    ArrayList<AttachmenEntry> getNeedParcelSavingEntries() {
        // сохраняем все, кроме аплоада
        return Utils.copyToArrayListWithPredicate(getData(), entry -> !(entry.getAttachment() instanceof Upload));
    }

    @OnGuiCreated
    private void resolveButtonsAvailability() {
        if (isGuiReady()) {
            getView().setSupportedButtons(true, false, true, true, false, false);
        }
    }

    private void initialPopulateEntries() {
        if (nonNull(orig.getAttachments())) {
            List<AbsModel> models = orig.getAttachments().toList();

            for (AbsModel m : models) {
                getData().add(new AttachmenEntry(true, m));
            }
        }
    }

    public void fireReadyClick() {
        if (hasUploads()) {
            safeShowError(getView(), R.string.upload_not_resolved_exception_message);
            return;
        }

        List<AbsModel> models = new ArrayList<>();
        for (AttachmenEntry entry : super.getData()) {
            models.add(entry.getAttachment());
        }

        setEditingNow(true);

        final int accountId = super.getAccountId();
        final Commented commented = this.orig.getCommented();
        final int commentId = this.orig.getId();
        final String body = super.getTextBody();

        appendDisposable(commentsInteractor.edit(accountId, commented, commentId, body, models)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onEditComplete, this::onEditError));
    }

    private void onEditError(Throwable t) {
        setEditingNow(false);
        showError(getView(), t);
    }

    private void onEditComplete(@Nullable Comment comment) {
        setEditingNow(false);

        this.canGoBack = true;

        callView(view -> view.goBackWithResult(comment));
    }

    private void setEditingNow(boolean editingNow) {
        this.editingNow = editingNow;
        resolveProgressDialog();
    }

    @OnGuiCreated
    private void resolveProgressDialog() {
        if (isGuiReady()) {
            if (editingNow) {
                getView().displayProgressDialog(R.string.please_wait, R.string.saving, false);
            } else {
                getView().dismissProgressDialog();
            }
        }
    }

    public boolean onBackPressed() {
        if (canGoBack) {
            return true;
        }

        getView().showConfirmWithoutSavingDialog();
        return false;
    }

    public void fireSavingCancelClick() {
        uploadManager.cancelAll(getAccountId(), destination);
        this.canGoBack = true;
        getView().goBack();
    }
}