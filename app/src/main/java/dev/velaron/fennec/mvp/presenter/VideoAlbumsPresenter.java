package dev.velaron.fennec.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.velaron.fennec.mvp.reflect.OnGuiCreated;
import dev.velaron.fennec.domain.IVideosInteractor;
import dev.velaron.fennec.domain.InteractorFactory;
import dev.velaron.fennec.model.VideoAlbum;
import dev.velaron.fennec.mvp.presenter.base.AccountDependencyPresenter;
import dev.velaron.fennec.mvp.view.IVideoAlbumsView;
import dev.velaron.fennec.util.RxUtils;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by admin on 21.11.2016.
 * phoenix
 */
public class VideoAlbumsPresenter extends AccountDependencyPresenter<IVideoAlbumsView> {

    private static final int COUNT_PER_LOAD = 40;

    private final int ownerId;
    private final String action;
    private final List<VideoAlbum> data;
    private boolean endOfContent;
    private boolean actualDataReceived;

    private final IVideosInteractor videosInteractor;

    public VideoAlbumsPresenter(int accountId, int ownerId, String action, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);

        this.videosInteractor = InteractorFactory.createVideosInteractor();
        this.ownerId = ownerId;
        this.action = action;
        this.data = new ArrayList<>();

        loadAllDataFromDb();
        requestActualData(0);
    }

    @OnGuiCreated
    private void resolveRefreshingView() {
        if (isGuiReady()) {
            getView().displayLoading(netLoadingNow);
        }
    }

    private CompositeDisposable netDisposable = new CompositeDisposable();
    private boolean netLoadingNow;
    private int netLoadingOffset;

    private void requestActualData(int offset) {
        this.netLoadingNow = true;
        this.netLoadingOffset = offset;

        resolveRefreshingView();

        final int accountId = super.getAccountId();
        netDisposable.add(videosInteractor.getActualAlbums(accountId, ownerId, COUNT_PER_LOAD, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(albums -> onActualDataReceived(offset, albums), this::onActualDataGetError));
    }

    private void onActualDataGetError(Throwable t) {
        this.netLoadingNow = false;
        resolveRefreshingView();

        showError(getView(), t);
    }

    private void onActualDataReceived(int offset, List<VideoAlbum> albums) {
        this.cacheDisposable.clear();
        this.cacheNowLoading = false;

        this.netLoadingNow = false;
        this.actualDataReceived = true;
        this.endOfContent = albums.isEmpty();

        resolveRefreshingView();

        if (offset == 0) {
            this.data.clear();
            this.data.addAll(albums);
            callView(IVideoAlbumsView::notifyDataSetChanged);
        } else {
            int startSize = this.data.size();
            this.data.addAll(albums);
            callView(view -> view.notifyDataAdded(startSize, albums.size()));
        }
    }

    private CompositeDisposable cacheDisposable = new CompositeDisposable();
    private boolean cacheNowLoading;

    private void loadAllDataFromDb() {
        final int accountId = super.getAccountId();

        cacheDisposable.add(videosInteractor.getCachedAlbums(accountId, ownerId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onCachedDataReceived, RxUtils.ignore()));
    }

    private void onCachedDataReceived(List<VideoAlbum> albums) {
        this.cacheNowLoading = false;
        this.data.clear();
        this.data.addAll(albums);

        callView(IVideoAlbumsView::notifyDataSetChanged);
    }

    @Override
    public void onDestroyed() {
        this.cacheDisposable.dispose();
        this.netDisposable.dispose();
        super.onDestroyed();
    }

    @Override
    public void onGuiCreated(@NonNull IVideoAlbumsView view) {
        super.onGuiCreated(view);
        view.displayData(this.data);
    }

    private boolean canLoadMore() {
        return !endOfContent && actualDataReceived && !netLoadingNow && !cacheNowLoading && !data.isEmpty();
    }

    public void fireItemClick(VideoAlbum album) {
        getView().openAlbum(getAccountId(), ownerId, album.getId(), action, album.getTitle());
    }

    public void fireRefresh() {
        this.cacheDisposable.clear();
        this.cacheNowLoading = false;

        this.netDisposable.clear();

        requestActualData(0);
    }

    public void fireScrollToLast() {
        if (canLoadMore()) {
            requestActualData(this.data.size());
        }
    }
}
