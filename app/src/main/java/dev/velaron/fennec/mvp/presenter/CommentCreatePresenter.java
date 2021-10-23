package dev.velaron.fennec.mvp.presenter;

import static dev.velaron.fennec.util.Objects.isNull;
import static dev.velaron.fennec.util.RxUtils.subscribeOnIOAndIgnore;
import static dev.velaron.fennec.util.Utils.nonEmpty;
import static dev.velaron.fennec.util.Utils.removeIf;
import static dev.velaron.fennec.util.Utils.safeCountOf;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import dev.velaron.fennec.mvp.reflect.OnGuiCreated;
import dev.velaron.fennec.Injection;
import dev.velaron.fennec.db.AttachToType;
import dev.velaron.fennec.domain.IAttachmentsRepository;
import dev.velaron.fennec.model.AbsModel;
import dev.velaron.fennec.model.AttachmenEntry;
import dev.velaron.fennec.model.LocalPhoto;
import dev.velaron.fennec.mvp.view.ICreateCommentView;
import dev.velaron.fennec.upload.Upload;
import dev.velaron.fennec.upload.UploadDestination;
import dev.velaron.fennec.upload.UploadUtils;
import dev.velaron.fennec.util.Analytics;
import dev.velaron.fennec.util.Pair;
import dev.velaron.fennec.util.Predicate;
import dev.velaron.fennec.util.RxUtils;
import io.reactivex.Single;

/**
 * Created by admin on 27.03.2017.
 * phoenix
 */
public class CommentCreatePresenter extends AbsAttachmentsEditPresenter<ICreateCommentView> {

    private static final String TAG = CommentCreatePresenter.class.getSimpleName();

    private final int commentId;
    private final UploadDestination destination;
    private final IAttachmentsRepository attachmentsRepository;

    public CommentCreatePresenter(int accountId, int commentDbid, int sourceOwnerId, String body, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.attachmentsRepository = Injection.provideAttachmentsRepository();
        this.commentId = commentDbid;
        this.destination = UploadDestination.forComment(commentId, sourceOwnerId);

        if (isNull(savedInstanceState)) {
            setTextBody(body);
        }

        Predicate<Upload> predicate = o -> destination.compareTo(o.getDestination());

        appendDisposable(uploadManager.observeAdding()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(updates -> onUploadQueueUpdates(updates, predicate)));

        appendDisposable(uploadManager.obseveStatus()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadStatusUpdate));

        appendDisposable(uploadManager.observeProgress()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadProgressUpdate));

        appendDisposable(uploadManager.observeDeleting(true)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadObjectRemovedFromQueue));

        appendDisposable(attachmentsRepository.observeAdding()
                .filter(this::filterAttachEvents)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::handleAttachmentsAdding));

        appendDisposable(attachmentsRepository.observeRemoving()
                .filter(this::filterAttachEvents)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::handleAttachmentRemoving));

        loadAttachments();
    }

    private boolean filterAttachEvents(IAttachmentsRepository.IBaseEvent event) {
        return event.getAccountId() == getAccountId()
                && event.getAttachToId() == commentId
                && event.getAttachToType() == AttachToType.COMMENT;
    }

    private void handleAttachmentRemoving(IAttachmentsRepository.IRemoveEvent event) {
        if (removeIf(getData(), attachment -> attachment.getOptionalId() == event.getGeneratedId())) {
            safeNotifyDataSetChanged();
        }
    }

    private void handleAttachmentsAdding(IAttachmentsRepository.IAddEvent event) {
        addAll(event.getAttachments());
    }

    private void addAll(List<Pair<Integer, AbsModel>> data) {
        for (Pair<Integer, AbsModel> pair : data) {
            getData().add(new AttachmenEntry(true, pair.getSecond()).setOptionalId(pair.getFirst()));
        }

        if (safeCountOf(data) > 0) {
            safeNotifyDataSetChanged();
        }
    }

    private Single<List<AttachmenEntry>> attachmentsSingle() {
        return attachmentsRepository
                .getAttachmentsWithIds(getAccountId(), AttachToType.COMMENT, commentId)
                .map(pairs -> createFrom(pairs, true));
    }

    private Single<List<AttachmenEntry>> uploadsSingle() {
        return uploadManager.get(getAccountId(), destination)
                .map(AbsAttachmentsEditPresenter::createFrom);
    }

    private void loadAttachments() {
        appendDisposable(attachmentsSingle()
                .zipWith(uploadsSingle(), this::combine)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onAttachmentsRestored, Analytics::logUnexpectedError));
    }

    private void onAttachmentsRestored(List<AttachmenEntry> entries) {
        getData().addAll(entries);

        if (nonEmpty(entries)) {
            safeNotifyDataSetChanged();
        }
    }

    @Override
    void onAttachmentRemoveClick(int index, @NonNull AttachmenEntry attachment) {
        if (attachment.getOptionalId() != 0) {
            subscribeOnIOAndIgnore(attachmentsRepository.remove(getAccountId(), AttachToType.COMMENT, commentId, attachment.getOptionalId()));
            // из списка не удаляем, так как удаление из репозитория "слушается"
            // (будет удалено асинхронно и после этого удалится из списка)
        } else {
            // такого в комментах в принципе быть не может !!!
            manuallyRemoveElement(index);
        }
    }

    @Override
    protected void onModelsAdded(List<? extends AbsModel> models) {
        subscribeOnIOAndIgnore(attachmentsRepository.attach(getAccountId(), AttachToType.COMMENT, commentId, models));
    }

    @Override
    protected void doUploadPhotos(List<LocalPhoto> photos, int size) {
        uploadManager.enqueue(UploadUtils.createIntents(getAccountId(), destination, photos, size, true));
    }

    @OnGuiCreated
    private void resolveButtonsVisibility() {
        if (isGuiReady()) {
            getView().setSupportedButtons(true, true, true, true, false, false);
        }
    }

    private void returnDataToParent() {
        getView().returnDataToParent(getTextBody());
    }

    public void fireReadyClick() {
        getView().goBack();
    }

    public boolean onBackPressed() {
        returnDataToParent();
        return true;
    }
}