package dev.velaron.fennec.mvp.presenter;

import static dev.velaron.fennec.util.Objects.isNull;
import static dev.velaron.fennec.util.Objects.nonNull;
import static dev.velaron.fennec.util.RxUtils.applyCompletableIOToMainSchedulers;
import static dev.velaron.fennec.util.Utils.copyToArrayListWithPredicate;
import static dev.velaron.fennec.util.Utils.getCauseIfRuntime;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import java.util.ArrayList;
import java.util.List;

import dev.velaron.fennec.mvp.reflect.OnGuiCreated;
import dev.velaron.fennec.Injection;
import dev.velaron.fennec.R;
import dev.velaron.fennec.api.model.VKApiCommunity;
import dev.velaron.fennec.api.model.VKApiPost;
import dev.velaron.fennec.domain.IWallsRepository;
import dev.velaron.fennec.domain.Repository;
import dev.velaron.fennec.model.AbsModel;
import dev.velaron.fennec.model.AttachmenEntry;
import dev.velaron.fennec.model.Community;
import dev.velaron.fennec.model.LocalPhoto;
import dev.velaron.fennec.model.Owner;
import dev.velaron.fennec.model.Photo;
import dev.velaron.fennec.model.Poll;
import dev.velaron.fennec.model.Post;
import dev.velaron.fennec.model.WallEditorAttrs;
import dev.velaron.fennec.mvp.view.IBaseAttachmentsEditView;
import dev.velaron.fennec.mvp.view.IPostEditView;
import dev.velaron.fennec.upload.Upload;
import dev.velaron.fennec.upload.UploadDestination;
import dev.velaron.fennec.upload.UploadIntent;
import dev.velaron.fennec.upload.UploadResult;
import dev.velaron.fennec.upload.UploadUtils;
import dev.velaron.fennec.util.Analytics;
import dev.velaron.fennec.util.Logger;
import dev.velaron.fennec.util.Pair;
import dev.velaron.fennec.util.Predicate;
import dev.velaron.fennec.util.RxUtils;
import dev.velaron.fennec.util.Unixtime;
import dev.velaron.fennec.util.Utils;

/**
 * Created by admin on 30.01.2017.
 * phoenix
 */
public class PostEditPresenter extends AbsPostEditPresenter<IPostEditView> {

    private static final String TAG = PostEditPresenter.class.getSimpleName();

    private static final String SAVE_POST = "save_post";

    private final Post post;
    private final UploadDestination uploadDestination;
    private final Predicate<Upload> uploadPredicate;

    private final IWallsRepository wallInteractor;

    private final WallEditorAttrs attrs;

    private boolean editingNow;
    private boolean canExit;

    public PostEditPresenter(int accountId, @NonNull Post post, @NonNull WallEditorAttrs attrs, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.wallInteractor = Repository.INSTANCE.getWalls();
        this.attrs = attrs;

        if (isNull(savedInstanceState)) {
            this.post = safelyClone(post);

            super.setTextBody(post.getText());

            if (post.getPostType() == VKApiPost.Type.POSTPONE) {
                super.setTimerValue(post.getDate());
            }

            if (nonNull(post.getAttachments())) {
                List<AbsModel> list = post.getAttachments().toList();

                for (AbsModel model : list) {
                    super.getData().add(new AttachmenEntry(true, model));
                }
            }

            if (post.hasCopyHierarchy()) {
                super.getData().add(0, new AttachmenEntry(false, post.getCopyHierarchy().get(0)));
            }
        } else {
            this.post = savedInstanceState.getParcelable(SAVE_POST);
        }

        Owner owner = getOwner();

        super.setFriendsOnlyOptionAvailable(owner.getOwnerId() > 0 && owner.getOwnerId() == accountId);
        checkFriendsOnly(post.isFriendsOnly());

        super.setAddSignatureOptionAvailable(canAddSignature());

        super.addSignature.setValue(post.getSignerId() > 0);
        super.setFromGroupOptionAvailable(false); // only for publishing

        this.uploadDestination = UploadDestination.forPost(post.getVkid(), post.getOwnerId());
        this.uploadPredicate = object -> object.getAccountId() == getAccountId()
                && object.getDestination().compareTo(uploadDestination);

        appendDisposable(uploadManager.observeAdding()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(updates -> onUploadQueueUpdates(updates, uploadPredicate)));

        appendDisposable(uploadManager.observeProgress()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadProgressUpdate));

        appendDisposable(uploadManager.obseveStatus()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadStatusUpdate));

        appendDisposable(uploadManager.observeDeleting(false)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadObjectRemovedFromQueue));

        appendDisposable(uploadManager.observeResults()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadComplete));
    }

    @NonNull
    private Owner getOwner() {
        return attrs.getOwner();
    }

    private Owner getMe() {
        return attrs.getEditor();
    }

    @Override
    void onShowAuthorChecked(boolean checked) {
        super.onShowAuthorChecked(checked);
        resolveSignerInfoVisibility();
    }

    private boolean isEditorOrHigher() {
        Owner owner = getOwner();
        return owner instanceof Community && ((Community) owner).getAdminLevel() >= VKApiCommunity.AdminLevel.EDITOR;
    }

    private boolean postIsSuggest() {
        return post.getPostType() == VKApiPost.Type.SUGGEST;
    }

    private boolean postIsMine() {
        if (post.getCreatorId() > 0 && post.getCreatorId() == getAccountId()) {
            return true;
        }

        return post.getSignerId() > 0 && post.getSignerId() == getAccountId();

    }

    private boolean supportSignerInfoDisplaying() {
        if (!isAddSignatureOptionAvailable()) {
            return false;
        }

        // потому что она может быть недоступна (signer == null)
        if (postIsSuggest() && !postIsMine()) {
            return true;
        }

        return nonNull(post.getCreator());
    }

    @Override
    public void onGuiCreated(@NonNull IPostEditView view) {
        super.onGuiCreated(view);

        @StringRes
        int titleRes = isPublishingSuggestPost() ? R.string.publication : R.string.editing;

        view.setToolbarTitle(getString(titleRes));
        view.setToolbarSubtitle(getOwner().getFullName());
    }

    private Owner getDisplayedSigner() {
        if (postIsSuggest()) {
            return post.getAuthor();
        }

        if (nonNull(post.getCreator())) {
            return post.getCreator();
        }

        return getMe();
    }

    @OnGuiCreated
    private void resolveSignerInfoVisibility() {
        if (isGuiReady()) {
            Owner signer = getDisplayedSigner();

            getView().displaySignerInfo(signer.getFullName(), signer.get100photoOrSmaller());
            getView().setSignerInfoVisible(supportSignerInfoDisplaying() && addSignature.get());
        }
    }

    @Override
    ArrayList<AttachmenEntry> getNeedParcelSavingEntries() {
        return copyToArrayListWithPredicate(getData(), entry -> !(entry.getAttachment() instanceof Upload));
    }

    private static Post safelyClone(Post post) {
        try {
            return post.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalArgumentException("Unable to clone post");
        }
    }

    private void onUploadComplete(Pair<Upload, UploadResult<?>> data) {
        Upload upload = data.getFirst();
        UploadResult<?> result = data.getSecond();

        int index = findUploadIndexById(upload.getId());
        if (index == -1) {
            return;
        }

        if (result.getResult() instanceof Photo) {
            Photo photo = (Photo) result.getResult();
            getData().set(index, new AttachmenEntry(true, photo));
        } else {
            super.getData().remove(index);
        }

        callView(IBaseAttachmentsEditView::notifyDataSetChanged);
    }

    private void setEditingNow(boolean editingNow) {
        this.editingNow = editingNow;
        resolveProgressDialog();
    }

    @OnGuiCreated
    private void resolveProgressDialog() {
        if (isGuiReady()) {
            if (editingNow) {
                getView().displayProgressDialog(R.string.please_wait, R.string.publication, false);
            } else {
                getView().dismissProgressDialog();
            }
        }
    }

    private boolean isPublishingSuggestPost() {
        // если пост предложенный - постим, если нет - редактируем
        // если пост мой и он предложенный - редактируем
        return postIsSuggest() && !postIsMine();
    }

    private void save() {
        Logger.d(TAG, "save, author: " + post.getAuthor() + ", signer: " + post.getCreator());

        appendDisposable(uploadManager
                .get(getAccountId(), uploadDestination)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> {
                    if (data.isEmpty()) {
                        doCommitImpl();
                    } else {
                        safeShowLongToast(getView(), R.string.wait_until_file_upload_is_complete);
                    }
                }, Analytics::logUnexpectedError));
    }

    private boolean isGroup() {
        return getOwner() instanceof Community && ((Community) getOwner()).getType() == VKApiCommunity.Type.GROUP;
    }

    private boolean isCommunity() {
        return getOwner() instanceof Community && ((Community) getOwner()).getType() == VKApiCommunity.Type.PAGE;
    }

    private boolean canAddSignature() {
        if (!isEditorOrHigher()) {
            // только редакторы и выше могу указывать автора
            return false;
        }

        if (isGroup()) {
            // если группа - то только, если пост от имени группы
            return post.getAuthor() instanceof Community;
        }

        // в публичных страницах всегда можно
        return isCommunity();

    }

    private void doCommitImpl() {
        if (isPublishingSuggestPost()) {
            postImpl();
            return;
        }

        boolean timerCancelled = post.getPostType() == VKApiPost.Type.POSTPONE && isNull(getTimerValue());

        if (timerCancelled) {
            postImpl();
            return;
        }

        saveImpl();
    }

    private void saveImpl() {
        Long publishDate = getTimerValue();

        setEditingNow(true);
        final Boolean signed = canAddSignature() ? super.addSignature.get() : null;
        final Boolean friendsOnly = isFriendsOnlyOptionAvailable() ? super.friendsOnly.get() : null;

        appendDisposable(wallInteractor
                .editPost(getAccountId(), post.getOwnerId(), post.getVkid(), friendsOnly,
                        getTextBody(), getAttachmentTokens(), null, signed, publishDate,
                        null, null, null, null)
                .compose(applyCompletableIOToMainSchedulers())
                .subscribe(this::onEditResponse, throwable -> onEditError(getCauseIfRuntime(throwable))));
    }

    private void postImpl() {
        final int accountId = getAccountId();
        final Long publishDate = getTimerValue();
        final String body = getTextBody();
        final Boolean signed = isAddSignatureOptionAvailable() ? addSignature.get() : null;

        // Эта опция не может быть доступна (так как публикация - исключительно для PAGE)
        final Boolean fromGroup = null;

        setEditingNow(true);
        appendDisposable(wallInteractor
                .post(accountId, post.getOwnerId(), null, fromGroup, body, getAttachmentTokens(), null,
                        signed, publishDate, null, null, null, post.getVkid(), null, null, null)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(post -> onEditResponse(), throwable -> onEditError(getCauseIfRuntime(throwable))));
    }

    private void onEditError(Throwable throwable) {
        setEditingNow(false);
        throwable.printStackTrace();

        showError(getView(), throwable);
    }

    private void onEditResponse() {
        setEditingNow(false);
        this.canExit = true;

        if (isGuiReady()) {
            getView().closeAsSuccess();
        }
    }

    private List<AbsModel> getAttachmentTokens() {
        List<AbsModel> result = new ArrayList<>();

        for (AttachmenEntry entry : super.getData()) {
            if (entry.getAttachment() instanceof Post) {
                continue;
            }

            result.add(entry.getAttachment());
        }

        return result;
    }

    @Override
    void onAttachmentRemoveClick(int index, @NonNull AttachmenEntry attachment) {
        super.manuallyRemoveElement(index);

        if (attachment.getAttachment() instanceof Poll) {
            // because only 1 poll is supported
            resolveSupportButtons();
        }
    }

    @Override
    protected void doUploadPhotos(List<LocalPhoto> photos, int size) {
        List<UploadIntent> intents = UploadUtils.createIntents(getAccountId(), uploadDestination, photos, size, false);
        uploadManager.enqueue(intents);
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putParcelable(SAVE_POST, post);
    }

    @OnGuiCreated
    private void resolveSupportButtons() {
        if (isGuiReady()) {
            if (post.hasCopyHierarchy()) {
                getView().setSupportedButtons(false, false, false, false, false, false);
            } else {
                getView().setSupportedButtons(true, true, true, true, isPollSupported(), supportTimer());
            }
        }
    }

    private boolean isPollSupported() {
        for (AttachmenEntry entry : getData()) {
            if (entry.getAttachment() instanceof Poll) {
                return false;
            }
        }

        return true;
    }

    private boolean supportTimer() {
        if (getOwner() instanceof Community && ((Community) getOwner()).getAdminLevel() < VKApiCommunity.AdminLevel.EDITOR) {
            // если сообщество и я не одмен, то нет
            return false;
        }

        return Utils.intValueIn(post.getPostType(), VKApiPost.Type.POSTPONE, VKApiPost.Type.SUGGEST);
    }

    public final void fireReadyClick() {
        save();
    }

    public boolean onBackPressed() {
        if (canExit) {
            return true;
        }

        getView().showConfirmExitDialog();
        return false;
    }

    @Override
    protected void onTimerClick() {
        if (!supportTimer()) {
            return;
        }

        if (nonNull(getTimerValue())) {
            setTimerValue(null);
            return;
        }

        long initialDate = Unixtime.now() + 24 * 60 * 60;

        getView().showEnterTimeDialog(initialDate);
    }

    @Override
    public void fireTimerTimeSelected(long unixtime) {
        if (!validatePublishDate(unixtime)) {
            safeShowError(getView(), R.string.date_is_invalid);
            return;
        }

        super.setTimerValue(unixtime);
    }

    private static boolean validatePublishDate(long unixtime) {
        return Unixtime.now() < unixtime;
    }

    @Override
    protected void onPollCreateClick() {
        getView().openPollCreationWindow(getAccountId(), post.getOwnerId());
    }

    public void fireExitWithSavingConfirmed() {
        save();
    }

    public void fireExitWithoutSavingClick() {
        this.canExit = true;
        uploadManager.cancelAll(getAccountId(),uploadDestination);
        getView().closeAsSuccess();
    }
}