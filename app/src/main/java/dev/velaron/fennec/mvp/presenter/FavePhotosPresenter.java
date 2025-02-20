package dev.velaron.fennec.mvp.presenter;

import static dev.velaron.fennec.util.Utils.getCauseIfRuntime;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.velaron.fennec.mvp.reflect.OnGuiCreated;
import dev.velaron.fennec.domain.IFaveInteractor;
import dev.velaron.fennec.domain.InteractorFactory;
import dev.velaron.fennec.model.Photo;
import dev.velaron.fennec.mvp.presenter.base.AccountDependencyPresenter;
import dev.velaron.fennec.mvp.view.IFavePhotosView;
import dev.velaron.fennec.util.RxUtils;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by admin on 09.01.2017.
 * phoenix
 */
public class FavePhotosPresenter extends AccountDependencyPresenter<IFavePhotosView> {

    private static final String TAG = FavePhotosPresenter.class.getSimpleName();
    private static final int COUNT_PER_REQUEST = 50;

    private ArrayList<Photo> mPhotos;
    private boolean mEndOfContent;

    private final IFaveInteractor faveInteractor;

    public FavePhotosPresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);

        this.mPhotos = new ArrayList<>();
        this.faveInteractor = InteractorFactory.createFaveInteractor();

        loadAllCachedData();
        requestAtLast();
    }

    @OnGuiCreated
    private void resolveRefreshingView() {
        if (isGuiReady()) {
            getView().showRefreshing(requestNow);
        }
    }

    private CompositeDisposable cacheDisposable = new CompositeDisposable();
    private CompositeDisposable netDisposable = new CompositeDisposable();

    private boolean cacheLoadingNow;

    private void loadAllCachedData() {
        final int accountId = super.getAccountId();

        this.cacheLoadingNow = true;
        cacheDisposable.add(faveInteractor.getCachedPhotos(accountId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onCachedDataReceived, this::onCacheGetError));
    }

    private void onCacheGetError(Throwable t) {
        this.cacheLoadingNow = false;
        showError(getView(), t);
    }

    private void onCachedDataReceived(List<Photo> photos) {
        this.cacheLoadingNow = false;
        this.mPhotos.clear();
        this.mPhotos.addAll(photos);

        callView(IFavePhotosView::notifyDataSetChanged);
    }

    @Override
    public void onDestroyed() {
        cacheDisposable.dispose();
        netDisposable.dispose();
        super.onDestroyed();
    }

    private boolean requestNow;

    private void setRequestNow(boolean requestNow) {
        this.requestNow = requestNow;
        resolveRefreshingView();
    }

    private void request(int offset) {
        setRequestNow(true);

        final int accountId = super.getAccountId();

        netDisposable.add(faveInteractor.getPhotos(accountId, COUNT_PER_REQUEST, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(photos -> onActualDataReceived(offset, photos), this::onActualDataGetError));
    }

    private void onActualDataGetError(Throwable t){
        setRequestNow(false);
        showError(getView(), getCauseIfRuntime(t));
    }

    private void onActualDataReceived(int offset, List<Photo> photos) {
        this.mEndOfContent = photos.isEmpty();
        this.cacheDisposable.clear();

        setRequestNow(false);

        if (offset == 0) {
            mPhotos.clear();
            mPhotos.addAll(photos);
            callView(IFavePhotosView::notifyDataSetChanged);
        } else {
            int startSize = mPhotos.size();
            mPhotos.addAll(photos);
            callView(view -> view.notifyDataAdded(startSize, photos.size()));
        }
    }

    private void requestAtLast() {
        request(0);
    }

    private void requestNext() {
        request(mPhotos.size());
    }

    @Override
    public void onGuiCreated(@NonNull IFavePhotosView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayData(mPhotos);
    }

    private boolean canLoadMore() {
        return !mPhotos.isEmpty() && !requestNow && !mEndOfContent && !cacheLoadingNow;
    }

    public void fireRefresh() {
        this.netDisposable.clear();
        this.cacheDisposable.clear();
        this.cacheLoadingNow = false;
        this.requestNow = false;

        requestAtLast();
    }

    @SuppressWarnings("unused")
    public void firePhotoClick(int position, Photo photo) {
        getView().goToGallery(getAccountId(), mPhotos, position);
    }

    public void fireScrollToEnd() {
        if (canLoadMore()) {
            requestNext();
        }
    }
}