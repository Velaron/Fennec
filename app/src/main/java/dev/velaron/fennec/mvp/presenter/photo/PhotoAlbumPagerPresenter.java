package dev.velaron.fennec.mvp.presenter.photo;

import static dev.velaron.fennec.util.Objects.nonNull;
import static dev.velaron.fennec.util.Utils.getCauseIfRuntime;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.velaron.fennec.domain.IPhotosInteractor;
import dev.velaron.fennec.domain.InteractorFactory;
import dev.velaron.fennec.model.Photo;
import dev.velaron.fennec.util.RxUtils;

/**
 * Created by admin on 25.09.2016.
 * phoenix
 */
public class PhotoAlbumPagerPresenter extends PhotoPagerPresenter {

    private static final int COUNT_PER_LOAD = 50;

    private int mOwnerId;
    private int mAlbumId;
    private Integer mFocusPhotoId;
    private final IPhotosInteractor photosInteractor;

    public PhotoAlbumPagerPresenter(int accountId, int ownerId, int albumId, Integer focusPhotoId,
                                    @Nullable Bundle savedInstanceState) {
        super(new ArrayList<>(0), accountId, savedInstanceState);

        this.photosInteractor = InteractorFactory.createPhotosInteractor();
        this.mOwnerId = ownerId;
        this.mAlbumId = albumId;

        if (nonNull(savedInstanceState)) {
            this.mFocusPhotoId = null; // because has saved last view index
        } else {
            this.mFocusPhotoId = focusPhotoId;
        }

        loadDataFromDatabase();
    }

    @Override
    void initPhotosData(@NonNull ArrayList<Photo> initialData, @Nullable Bundle savedInstanceState) {
        super.mPhotos = initialData;
    }

    @Override
    void savePhotosState(@NonNull Bundle outState) {
        //no saving state
    }

    private void loadDataFromDatabase() {
        changeLoadingNowState(true);

        final int accountId = super.getAccountId();

        appendDisposable(photosInteractor.getAllCachedData(accountId, mOwnerId, mAlbumId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onInitialLoadingFinished, this::onInitialDataGetError));
    }

    private void onInitialDataGetError(Throwable t) {
        showError(getView(), getCauseIfRuntime(t));
        changeLoadingNowState(true);
    }

    private void onInitialLoadingFinished(List<Photo> photos) {
        changeLoadingNowState(false);

        getData().addAll(photos);

        if (mFocusPhotoId != null) {
            for (int i = 0; i < photos.size(); i++) {
                if (mFocusPhotoId == photos.get(i).getId()) {
                    setCurrentIndex(i);
                    break;
                }
            }
        }

        refreshPagerView();
        resolveButtonsBarVisible();
        resolveToolbarVisibility();
        refreshInfoViews();
    }

    @Override
    protected void afterPageChangedFromUi(int oldPage, int newPage) {
        super.afterPageChangedFromUi(oldPage, newPage);

        if (newPage == count() - 1) {
            final int accountId = super.getAccountId();

            appendDisposable(photosInteractor.get(accountId, mOwnerId, mAlbumId, COUNT_PER_LOAD, count(), true)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(this::onLoadingAtRangeFinished, t -> showError(getView(), getCauseIfRuntime(t))));
        }
    }

    private void onLoadingAtRangeFinished(List<Photo> photos) {
        getData().addAll(photos);
        refreshPagerView();
        resolveToolbarTitleSubtitleView();
    }
}