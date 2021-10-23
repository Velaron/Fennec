package dev.velaron.fennec.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.velaron.fennec.db.Stores;
import dev.velaron.fennec.model.LocalImageAlbum;
import dev.velaron.fennec.mvp.presenter.base.RxSupportPresenter;
import dev.velaron.fennec.mvp.view.ILocalPhotoAlbumsView;
import dev.velaron.fennec.util.Analytics;
import dev.velaron.fennec.util.AppPerms;
import dev.velaron.fennec.util.RxUtils;
import dev.velaron.fennec.util.Utils;

/**
 * Created by admin on 03.10.2016.
 * phoenix
 */
public class LocalPhotoAlbumsPresenter extends RxSupportPresenter<ILocalPhotoAlbumsView> {

    private boolean permissionRequestedOnce;
    private List<LocalImageAlbum> mLocalImageAlbums;

    public LocalPhotoAlbumsPresenter(@Nullable Bundle savedInstanceState) {
        super(savedInstanceState);
        mLocalImageAlbums = new ArrayList<>();
    }

    private boolean mLoadingNow;

    @Override
    public void onGuiCreated(@NonNull ILocalPhotoAlbumsView viewHost) {
        super.onGuiCreated(viewHost);

        if(!AppPerms.hasReadStoragePermision(getApplicationContext())){
            if(!permissionRequestedOnce){
                permissionRequestedOnce = true;
                getView().requestReadExternalStoragePermission();
            }
        } else {
            loadData();
        }

        getView().displayData(mLocalImageAlbums);
        resolveProgressView();
        resolveEmptyTextView();
    }

    private void changeLoadingNowState(boolean loading) {
        mLoadingNow = loading;
        resolveProgressView();
    }

    private void resolveProgressView() {
        if (isGuiReady()) getView().displayProgress(mLoadingNow);
    }

    private void loadData() {
        if (mLoadingNow) return;

        changeLoadingNowState(true);
        appendDisposable(Stores.getInstance()
                .localPhotos()
                .getImageAlbums()
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onDataLoaded, this::onLoadError));
    }

    private void onLoadError(Throwable throwable) {
        Analytics.logUnexpectedError(throwable);
        changeLoadingNowState(false);
    }

    private void onDataLoaded(List<LocalImageAlbum> data) {
        changeLoadingNowState(false);
        mLocalImageAlbums.clear();
        mLocalImageAlbums.addAll(data);

        if (isGuiReady()) {
            getView().notifyDataChanged();
        }

        resolveEmptyTextView();
    }

    private void resolveEmptyTextView() {
        if (isGuiReady()) getView().setEmptyTextVisible(Utils.safeIsEmpty(mLocalImageAlbums));
    }

    public void fireRefresh() {
        loadData();
    }

    public void fireAlbumClick(@NonNull LocalImageAlbum album) {
        getView().openAlbum(album);
    }

    public void fireReadExternalStoregePermissionResolved() {
        if(AppPerms.hasReadStoragePermision(getApplicationContext())){
            loadData();
        }
    }
}
