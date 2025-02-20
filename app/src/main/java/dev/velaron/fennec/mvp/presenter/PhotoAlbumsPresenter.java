package dev.velaron.fennec.mvp.presenter;

import static dev.velaron.fennec.util.Utils.findIndexById;
import static dev.velaron.fennec.util.Utils.getCauseIfRuntime;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.velaron.fennec.mvp.reflect.OnGuiCreated;
import dev.velaron.fennec.domain.IOwnersRepository;
import dev.velaron.fennec.domain.IPhotosInteractor;
import dev.velaron.fennec.domain.IUtilsInteractor;
import dev.velaron.fennec.domain.InteractorFactory;
import dev.velaron.fennec.domain.Repository;
import dev.velaron.fennec.fragment.VKPhotoAlbumsFragment;
import dev.velaron.fennec.model.Community;
import dev.velaron.fennec.model.Owner;
import dev.velaron.fennec.model.PhotoAlbum;
import dev.velaron.fennec.model.PhotoAlbumEditor;
import dev.velaron.fennec.model.SimplePrivacy;
import dev.velaron.fennec.mvp.presenter.base.AccountDependencyPresenter;
import dev.velaron.fennec.mvp.view.IPhotoAlbumsView;
import dev.velaron.fennec.util.Analytics;
import dev.velaron.fennec.util.Objects;
import dev.velaron.fennec.util.RxUtils;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by ruslan.kolbasa on 29.11.2016.
 * phoenix
 */
public class PhotoAlbumsPresenter extends AccountDependencyPresenter<IPhotoAlbumsView> {

    private int mOwnerId;
    private Owner mOwner;
    private String mAction;
    private ArrayList<PhotoAlbum> mData;

    private final IPhotosInteractor photosInteractor;
    private final IOwnersRepository ownersRepository;
    private final IUtilsInteractor utilsInteractor;

    public PhotoAlbumsPresenter(int accountId, int ownerId, @Nullable AdditionalParams params, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);

        this.ownersRepository = Repository.INSTANCE.getOwners();
        this.photosInteractor = InteractorFactory.createPhotosInteractor();
        this.utilsInteractor = InteractorFactory.createUtilsInteractor();

        mOwnerId = ownerId;

        //do restore this

        if (Objects.nonNull(params)) {
            mAction = params.getAction();
        }

        if (Objects.isNull(mOwner) && Objects.nonNull(params)) {
            mOwner = params.getOwner();
        }

        if (Objects.isNull(mData)) {
            mData = new ArrayList<>();

            loadAllFromDb();
            refreshFromNet(0);
        }

        if (Objects.isNull(mOwner) && !isMy()) {
            loadOwnerInfo();
        }
    }

    @Override
    public void onGuiCreated(@NonNull IPhotoAlbumsView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayData(mData);
    }

    @OnGuiCreated
    private void resolveDrawerPhotoSection() {
        if (isGuiReady()) {
            getView().seDrawertPhotoSectionActive(isMy());
        }
    }

    private boolean isMy() {
        return mOwnerId == getAccountId();
    }

    private void loadOwnerInfo() {
        if (isMy()) {
            return;
        }

        final int accountId = super.getAccountId();
        appendDisposable(ownersRepository.getBaseOwnerInfo(accountId, mOwnerId, IOwnersRepository.MODE_ANY)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onOwnerInfoReceived, this::onOwnerGetError));
    }

    private void onOwnerGetError(Throwable t) {
        showError(getView(), getCauseIfRuntime(t));
    }

    private void onOwnerInfoReceived(Owner owner) {
        this.mOwner = owner;
        resolveSubtitleView();
        resolveCreateAlbumButtonVisibility();
    }

    private CompositeDisposable netDisposable = new CompositeDisposable();
    private boolean netLoadingNow;

    private void refreshFromNet(int offset) {
        this.netLoadingNow = true;
        resolveProgressView();

        final int accountId = super.getAccountId();
        netDisposable.add(photosInteractor.getActualAlbums(accountId, mOwnerId, 50, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(albums -> onActualAlbumsReceived(offset, albums), this::onActualAlbumsGetError));
    }

    private void onActualAlbumsGetError(Throwable t) {
        this.netLoadingNow = false;
        showError(getView(), getCauseIfRuntime(t));

        resolveProgressView();
    }

    private void onActualAlbumsReceived(int offset, List<PhotoAlbum> albums) {
        // reset cache loading
        this.cacheDisposable.clear();
        this.cacheLoadingNow = false;

        this.netLoadingNow = false;

        if (offset == 0) {
            this.mData.clear();
            this.mData.addAll(albums);
            callView(IPhotoAlbumsView::notifyDataSetChanged);
        } else {
            int startSize = this.mData.size();
            this.mData.addAll(albums);
            callView(view -> view.notifyDataAdded(startSize, albums.size()));
        }

        resolveProgressView();
    }

    private CompositeDisposable cacheDisposable = new CompositeDisposable();
    private boolean cacheLoadingNow;

    private void loadAllFromDb() {
        this.cacheLoadingNow = true;

        final int accountId = super.getAccountId();
        cacheDisposable.add(photosInteractor.getCachedAlbums(accountId, mOwnerId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onCachedDataReceived, t -> {/*ignored*/}));
    }

    private void onCachedDataReceived(List<PhotoAlbum> albums) {
        this.cacheLoadingNow = false;

        this.mData.clear();
        this.mData.addAll(albums);

        safeNotifyDatasetChanged();
    }

    @Override
    public void onDestroyed() {
        cacheDisposable.dispose();
        netDisposable.dispose();
        super.onDestroyed();
    }

    @OnGuiCreated
    private void resolveProgressView() {
        if (isGuiReady()) {
            getView().displayLoading(netLoadingNow);
        }
    }


    private void safeNotifyDatasetChanged() {
        if (isGuiReady()) {
            getView().notifyDataSetChanged();
        }
    }

    @OnGuiCreated
    private void resolveSubtitleView() {
        if (isGuiReady()) {
            getView().setToolbarSubtitle(Objects.isNull(mOwner) || isMy() ? null : mOwner.getFullName());
        }
    }

    private void doAlbumRemove(@NonNull PhotoAlbum album) {
        final int accountId = super.getAccountId();
        final int albumId = album.getId();
        final int ownerId = album.getOwnerId();

        appendDisposable(photosInteractor.removedAlbum(accountId, album.getOwnerId(), album.getId())
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> onAlbumRemoved(albumId, ownerId), t -> showError(getView(), getCauseIfRuntime(t))));
    }

    private void onAlbumRemoved(int albumId, int ownerId) {
        int index = findIndexById(this.mData, albumId, ownerId);
        if (index != -1) {
            callView(view -> view.notifyItemRemoved(index));
        }
    }

    public void fireCreateAlbumClick() {
        getView().goToAlbumCreation(getAccountId(), mOwnerId);
    }

    public void fireAlbumClick(PhotoAlbum album) {
        if (VKPhotoAlbumsFragment.ACTION_SELECT_ALBUM.equals(mAction)) {
            getView().doSelection(album);
        } else {
            getView().openAlbum(getAccountId(), album, mOwner, mAction);
        }
    }

    public boolean fireAlbumLongClick(PhotoAlbum album) {
        if (canDeleteOrEdit(album)) {
            getView().showAlbumContextMenu(album);
            return true;
        }

        return false;
    }

    private boolean isAdmin() {
        return mOwner instanceof Community && ((Community) mOwner).isAdmin();
    }

    private boolean canDeleteOrEdit(@NonNull PhotoAlbum album) {
        return !album.isSystem() && (isMy() || isAdmin());
    }

    @OnGuiCreated
    private void resolveCreateAlbumButtonVisibility() {
        if (isGuiReady()) {
            boolean mustBeVisible = isMy() || isAdmin();
            getView().setCreateAlbumFabVisible(mustBeVisible);
        }
    }

    public void fireRefresh() {
        this.cacheDisposable.clear();
        this.cacheLoadingNow = false;

        this.netDisposable.clear();
        this.netLoadingNow = false;

        refreshFromNet(0);
    }

    public void fireAlbumDeletingConfirmed(PhotoAlbum album) {
        doAlbumRemove(album);
    }

    public void fireAlbumDeleteClick(PhotoAlbum album) {
        getView().showDeleteConfirmDialog(album);
    }

    public void fireAlbumEditClick(PhotoAlbum album) {
        @SuppressLint("UseSparseArrays")
        Map<Integer, SimplePrivacy> privacies = new HashMap<>();

        privacies.put(0, album.getPrivacyView());
        privacies.put(1, album.getPrivacyComment());

        final int accountId = super.getAccountId();

        appendDisposable(utilsInteractor
                .createFullPrivacies(accountId, privacies)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(full -> {
                    PhotoAlbumEditor editor = PhotoAlbumEditor.create()
                            .setPrivacyView(full.get(0))
                            .setPrivacyComment(full.get(1))
                            .setTitle(album.getTitle())
                            .setDescription(album.getDescription())
                            .setCommentsDisabled(album.isCommentsDisabled())
                            .setUploadByAdminsOnly(album.isUploadByAdminsOnly());
                    if (isGuiReady()) {
                        getView().goToAlbumEditing(getAccountId(), album, editor);
                    }
                }, Analytics::logUnexpectedError));
    }

    public static class AdditionalParams {

        private Owner owner;
        private String action;

        public AdditionalParams setOwner(Owner owner) {
            this.owner = owner;
            return this;
        }

        private Owner getOwner() {
            return owner;
        }

        public AdditionalParams setAction(String action) {
            this.action = action;
            return this;
        }

        private String getAction() {
            return action;
        }
    }
}