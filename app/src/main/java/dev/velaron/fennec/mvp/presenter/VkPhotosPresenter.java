package dev.velaron.fennec.mvp.presenter;

import static dev.velaron.fennec.util.Objects.isNull;
import static dev.velaron.fennec.util.Objects.nonNull;
import static dev.velaron.fennec.util.Utils.findIndexById;
import static dev.velaron.fennec.util.Utils.getCauseIfRuntime;
import static dev.velaron.fennec.util.Utils.nonEmpty;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.velaron.fennec.mvp.reflect.OnGuiCreated;
import dev.velaron.fennec.Injection;
import dev.velaron.fennec.api.model.VKApiCommunity;
import dev.velaron.fennec.domain.IOwnersRepository;
import dev.velaron.fennec.domain.IPhotosInteractor;
import dev.velaron.fennec.domain.InteractorFactory;
import dev.velaron.fennec.domain.Repository;
import dev.velaron.fennec.model.Community;
import dev.velaron.fennec.model.LocalPhoto;
import dev.velaron.fennec.model.Owner;
import dev.velaron.fennec.model.ParcelableOwnerWrapper;
import dev.velaron.fennec.model.Photo;
import dev.velaron.fennec.model.PhotoAlbum;
import dev.velaron.fennec.model.wrappers.SelectablePhotoWrapper;
import dev.velaron.fennec.mvp.presenter.base.AccountDependencyPresenter;
import dev.velaron.fennec.mvp.view.IVkPhotosView;
import dev.velaron.fennec.upload.IUploadManager;
import dev.velaron.fennec.upload.Upload;
import dev.velaron.fennec.upload.UploadDestination;
import dev.velaron.fennec.upload.UploadIntent;
import dev.velaron.fennec.upload.UploadResult;
import dev.velaron.fennec.upload.UploadUtils;
import dev.velaron.fennec.util.AssertUtils;
import dev.velaron.fennec.util.Pair;
import dev.velaron.fennec.util.RxUtils;
import dev.velaron.fennec.util.Utils;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by Ruslan Kolbasa on 13.07.2017.
 * phoenix
 */
public class VkPhotosPresenter extends AccountDependencyPresenter<IVkPhotosView> {

    private static final String SAVE_ALBUM = "save-album";
    private static final String SAVE_OWNER = "save-owner";
    private static final int COUNT = 50;

    private final int ownerId;
    private final int albumId;

    private final IPhotosInteractor interactor;
    private final IOwnersRepository ownersRepository;
    private final IUploadManager uploadManager;

    private final List<SelectablePhotoWrapper> photos;
    private final List<Upload> uploads;

    private final UploadDestination destination;
    private final String action;
    private PhotoAlbum album;
    private Owner owner;
    private boolean requestNow;

    private CompositeDisposable cacheDisposable = new CompositeDisposable();
    private boolean endOfContent;

    public VkPhotosPresenter(int accountId, int ownerId, int albumId, String action,
                             @Nullable Owner owner, @Nullable PhotoAlbum album, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);

        this.ownerId = ownerId;
        this.albumId = albumId;
        this.action = action;

        this.interactor = InteractorFactory.createPhotosInteractor();
        this.ownersRepository = Repository.INSTANCE.getOwners();
        this.uploadManager = Injection.provideUploadManager();

        this.destination = UploadDestination.forPhotoAlbum(albumId, ownerId);

        this.photos = new ArrayList<>();
        this.uploads = new ArrayList<>();

        if (isNull(savedInstanceState)) {
            this.album = album;
            this.owner = owner;
        } else {
            this.album = savedInstanceState.getParcelable(SAVE_ALBUM);
            ParcelableOwnerWrapper ownerWrapper = savedInstanceState.getParcelable(SAVE_OWNER);
            AssertUtils.requireNonNull(ownerWrapper);
            this.owner = ownerWrapper.get();
        }

        loadInitialData();

        appendDisposable(uploadManager.observeAdding()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadQueueAdded));

        appendDisposable(uploadManager.observeDeleting(true)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadsRemoved));

        appendDisposable(uploadManager.observeResults()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadResults));

        appendDisposable(uploadManager.obseveStatus()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadStatusUpdate));

        appendDisposable(uploadManager.observeProgress()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadProgressUpdate));

        refreshOwnerInfoIfNeed();
        refreshAlbumInfoIfNeed();
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putParcelable(SAVE_ALBUM, album);
        outState.putParcelable(SAVE_OWNER, new ParcelableOwnerWrapper(owner));
    }

    private void refreshOwnerInfoIfNeed() {
        final int accountId = super.getAccountId();

        if (!isMy() && isNull(owner)) {
            appendDisposable(ownersRepository.getBaseOwnerInfo(accountId, ownerId, IOwnersRepository.MODE_NET)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(this::onActualOwnerInfoReceived, RxUtils.ignore()));
        }
    }

    private void refreshAlbumInfoIfNeed() {
        final int accountId = super.getAccountId();

        if (isNull(album)) {
            appendDisposable(interactor.getAlbumById(accountId, ownerId, albumId)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(this::onAlbumInfoReceived, RxUtils.ignore()));
        }
    }

    private void onAlbumInfoReceived(PhotoAlbum album) {
        this.album = album;

        resolveToolbarView();

        if (!isSelectionMode()) {
            resolveButtonAddVisibility(true);
        }
    }

    private void onActualOwnerInfoReceived(Owner owner) {
        this.owner = owner;
        resolveButtonAddVisibility(true);
    }

    private static List<SelectablePhotoWrapper> wrappersOf(List<Photo> photos) {
        List<SelectablePhotoWrapper> wrappers = new ArrayList<>(photos.size());
        for (Photo photo : photos) {
            wrappers.add(new SelectablePhotoWrapper(photo));
        }
        return wrappers;
    }

    @OnGuiCreated
    private void resolveToolbarView() {
        if (isGuiReady()) {
            String ownerName = nonNull(owner) ? owner.getFullName() : null;
            String albumTitle = nonNull(album) ? album.getTitle() : null;

            getView().setToolbarSubtitle(albumTitle);

            if (nonEmpty(ownerName)) {
                getView().setToolbarTitle(ownerName);
            } else {
                getView().displayDefaultToolbarTitle();
            }
        }
    }

    private void onUploadQueueAdded(List<Upload> added) {
        int startUploadSize = uploads.size();
        int count = 0;

        for (Upload upload : added) {
            if (destination.compareTo(upload.getDestination())) {
                uploads.add(upload);
                count++;
            }
        }

        if (count > 0) {
            int finalCount = count;
            callView(view -> view.notifyUploadAdded(startUploadSize, finalCount));
        }
    }

    private void onUploadsRemoved(int[] ids) {
        for (int id : ids) {
            int index = findIndexById(uploads, id);

            if (index != -1) {
                uploads.remove(index);
                callView(view -> view.notifyUploadRemoved(index));
            }
        }
    }

    private void onUploadResults(Pair<Upload, UploadResult<?>> pair) {
        if (destination.compareTo(pair.getFirst().getDestination())) {
            Photo photo = (Photo) pair.getSecond().getResult();
            photos.add(0, new SelectablePhotoWrapper(photo));
            callView(view -> view.notifyPhotosAdded(0, 1));
        }
    }

    private void onUploadStatusUpdate(Upload upload) {
        int index = findIndexById(uploads, upload.getId());
        if (index != -1) {
            callView(view -> view.notifyUploadItemChanged(index));
        }
    }

    private void onUploadProgressUpdate(List<IUploadManager.IProgressUpdate> updates) {
        for (IUploadManager.IProgressUpdate update : updates) {
            int index = findIndexById(uploads, update.getId());
            if (index != -1) {
                callView(view -> view.notifyUploadProgressChanged(update.getId(), update.getProgress()));
            }
        }
    }

    @Override
    public void onGuiCreated(@NonNull IVkPhotosView view) {
        super.onGuiCreated(view);
        view.displayData(photos, uploads);
        resolveButtonAddVisibility(false);
    }

    private void setRequestNow(boolean requestNow) {
        this.requestNow = requestNow;
        resolveRefreshingView();
    }

    private void resolveRefreshingView() {
        if (isGuiResumed()) {
            getView().displayRefreshing(requestNow);
        }
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
        getView().setDrawerPhotosSelected(isMy());
    }

    private void requestActualData(int offset) {
        setRequestNow(true);
        appendDisposable(interactor.get(getAccountId(), ownerId, albumId, COUNT, offset, true)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(photos -> onActualPhotosReceived(offset, photos), this::onActualDataGetError));
    }

    private void onActualDataGetError(Throwable t) {
        showError(getView(), getCauseIfRuntime(t));
        setRequestNow(false);
    }

    private void onActualPhotosReceived(int offset, List<Photo> data) {
        this.cacheDisposable.clear();
        this.endOfContent = data.isEmpty();

        setRequestNow(false);

        if (offset == 0) {
            this.photos.clear();
            this.photos.addAll(wrappersOf(data));
            callView(IVkPhotosView::notifyDataSetChanged);
        } else {
            int startSize = this.photos.size();
            this.photos.addAll(wrappersOf(data));
            callView(view -> view.notifyPhotosAdded(startSize, data.size()));
        }
    }

    private void loadInitialData() {
        final int accountId = getAccountId();
        cacheDisposable.add(interactor.getAllCachedData(accountId, ownerId, albumId)
                .zipWith(uploadManager.get(getAccountId(), destination), Pair.Companion::create)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onInitialDataReceived));
    }

    private void onInitialDataReceived(Pair<List<Photo>, List<Upload>> data) {
        photos.clear();
        photos.addAll(wrappersOf(data.getFirst()));

        uploads.clear();
        uploads.addAll(data.getSecond());

        callView(IVkPhotosView::notifyDataSetChanged);

        requestActualData(0);
    }

    @Override
    public void onDestroyed() {
        cacheDisposable.dispose();
        super.onDestroyed();
    }

    public void fireUploadRemoveClick(Upload o) {
        uploadManager.cancel(o.getId());
    }

    public void fireRefresh() {
        if (!requestNow) {
            requestActualData(0);
        }
    }

    public void fireScrollToEnd() {
        if (!requestNow && nonEmpty(photos) && !endOfContent) {
            requestActualData(photos.size());
        }
    }

    private boolean isMy() {
        return getAccountId() == ownerId;
    }

    private boolean isAdmin() {
        return owner instanceof Community && ((Community) owner).getAdminLevel() >= VKApiCommunity.AdminLevel.MODERATOR;
    }

    private boolean canUploadToAlbum() {
        // можно загружать,
        // 1 - альбом не системный ОБЯЗАТЕЛЬНО
        // 2 - если я админ группы
        // 3 - если альбом мой
        // 4 - если альбом принадлежит группе, но разрешено в него грузить
        return albumId >= 0 && (isAdmin() || isMy() || (nonNull(album) && album.isCanUpload()));
    }

    public void firePhotosForUploadSelected(List<LocalPhoto> photos, int size) {
        List<UploadIntent> intents = UploadUtils.createIntents(getAccountId(), destination, photos, size, true);
        uploadManager.enqueue(intents);
    }

    public void firePhotoSelectionChanged(SelectablePhotoWrapper wrapper) {
        wrapper.setSelected(!wrapper.isSelected());
        onPhotoSelected(wrapper);
    }

    private void onPhotoSelected(SelectablePhotoWrapper selectedPhoto) {
        if (selectedPhoto.isSelected()) {
            int targetIndex = 1;
            for (SelectablePhotoWrapper photo : photos) {
                if (photo.getIndex() >= targetIndex) {
                    targetIndex = photo.getIndex() + 1;
                }
            }

            selectedPhoto.setIndex(targetIndex);
        } else {
            for (int i = 0; i < photos.size(); i++) {
                SelectablePhotoWrapper photo = photos.get(i);
                if (photo.getIndex() > selectedPhoto.getIndex()) {
                    photo.setIndex(photo.getIndex() - 1);
                }
            }

            selectedPhoto.setIndex(0);
        }

        if (selectedPhoto.isSelected()) {
            getView().setButtonAddVisible(true, true);
        } else {
            resolveButtonAddVisibility(true);
        }
    }

    private boolean isSelectionMode() {
        return IVkPhotosView.ACTION_SELECT_PHOTOS.equals(action);
    }

    private void resolveButtonAddVisibility(boolean anim) {
        if (isGuiReady()) {
            if (isSelectionMode()) {
                boolean hasSelected = false;
                for (SelectablePhotoWrapper wrapper : photos) {
                    if (wrapper.isSelected()) {
                        hasSelected = true;
                        break;
                    }
                }

                getView().setButtonAddVisible(hasSelected, anim);
            } else {
                getView().setButtonAddVisible(canUploadToAlbum(), anim);
            }
        }
    }

    public void firePhotoClick(SelectablePhotoWrapper wrapper) {
        getView().displayGallery(getAccountId(), albumId, ownerId, wrapper.getPhoto().getId());
    }

    public void fireSelectionCommitClick() {
        List<Photo> selected = getSelected();

        if (nonEmpty(selected)) {
            getView().returnSelectionToParent(selected);
        } else {
            getView().showSelectPhotosToast();
        }
    }

    private List<SelectablePhotoWrapper> getSelectedWrappers() {
        List<SelectablePhotoWrapper> result = Utils.getSelected(photos);
        Collections.sort(result);
        return result;
    }

    private List<Photo> getSelected() {
        List<SelectablePhotoWrapper> wrappers = getSelectedWrappers();
        List<Photo> photos = new ArrayList<>(wrappers.size());
        for (SelectablePhotoWrapper wrapper : wrappers) {
            photos.add(wrapper.getPhoto());
        }

        return photos;
    }

    public void fireAddPhotosClick() {
        if (canUploadToAlbum()) {
            getView().startLocalPhotosSelection();
        }
    }

    public void fireReadStoragePermissionChanged() {
        getView().startLocalPhotosSelectionIfHasPermission();
    }
}