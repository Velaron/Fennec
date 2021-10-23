package dev.velaron.fennec.mvp.presenter;

import static dev.velaron.fennec.util.Utils.nonEmpty;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.velaron.fennec.domain.IVideosInteractor;
import dev.velaron.fennec.domain.InteractorFactory;
import dev.velaron.fennec.fragment.search.nextfrom.IntNextFrom;
import dev.velaron.fennec.model.Video;
import dev.velaron.fennec.mvp.presenter.base.AccountDependencyPresenter;
import dev.velaron.fennec.mvp.view.IVideosListView;
import dev.velaron.fennec.util.Analytics;
import dev.velaron.fennec.util.RxUtils;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by admin on 21.11.2016.
 * phoenix
 */
public class VideosListPresenter extends AccountDependencyPresenter<IVideosListView> {

    private static final int COUNT = 50;

    private final int ownerId;
    private final int albumId;

    private final String action;

    private String albumTitle;
    private final List<Video> data;

    private boolean endOfContent;

    private IntNextFrom intNextFrom;

    private final IVideosInteractor interactor;

    private boolean hasActualNetData;

    public VideosListPresenter(int accountId, int ownerId, int albumId, String action,
                               @Nullable String albumTitle, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.interactor = InteractorFactory.createVideosInteractor();

        this.ownerId = ownerId;
        this.albumId = albumId;
        this.action = action;
        this.albumTitle = albumTitle;

        this.intNextFrom = new IntNextFrom(0);

        this.data = new ArrayList<>();

        loadAllFromCache();
        request(false);
    }

    private boolean requestNow;

    private void resolveRefreshingView(){
        if(isGuiResumed()){
            getView().displayLoading(requestNow);
        }
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
    }

    private void setRequestNow(boolean requestNow) {
        this.requestNow = requestNow;
        resolveRefreshingView();
    }

    private CompositeDisposable netDisposable = new CompositeDisposable();

    private void request(boolean more){
        if(requestNow) return;

        setRequestNow(true);

        int accountId = super.getAccountId();

        final IntNextFrom startFrom = more ? this.intNextFrom : new IntNextFrom(0);

        netDisposable.add(interactor.get(accountId, ownerId, albumId, COUNT, startFrom.getOffset())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(videos -> {
                    IntNextFrom nextFrom = new IntNextFrom(startFrom.getOffset() + COUNT);
                    onRequestResposnse(videos, startFrom, nextFrom);
                }, this::onListGetError));
    }

    private void onListGetError(Throwable throwable){
        setRequestNow(false);
        showError(getView(), throwable);
    }

    private void onRequestResposnse(List<Video> videos, IntNextFrom startFrom, IntNextFrom nextFrom){
        this.cacheDisposable.clear();
        this.cacheNowLoading = false;

        this.hasActualNetData = true;
        this.intNextFrom = nextFrom;
        this.endOfContent = videos.isEmpty();

        if(startFrom.getOffset() == 0){
            data.clear();
            data.addAll(videos);

            callView(IVideosListView::notifyDataSetChanged);
        } else {
            if(nonEmpty(videos)){
                int startSize = data.size();
                data.addAll(videos);
                callView(view -> view.notifyDataAdded(startSize, videos.size()));
            }
        }

        setRequestNow(false);
    }

    @Override
    public void onGuiCreated(@NonNull IVideosListView view) {
        super.onGuiCreated(view);
        view.displayData(data);
        view.setToolbarSubtitle(albumTitle);
    }

    private CompositeDisposable cacheDisposable = new CompositeDisposable();
    private boolean cacheNowLoading;

    private void loadAllFromCache() {
        this.cacheNowLoading = true;
        final int accountId = super.getAccountId();

        cacheDisposable.add(interactor.getCachedVideos(accountId, ownerId, albumId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onCachedDataReceived, Analytics::logUnexpectedError));
    }

    private void onCachedDataReceived(List<Video> videos){
        this.data.clear();
        this.data.addAll(videos);

        callView(IVideosListView::notifyDataSetChanged);
    }

    @Override
    public void onDestroyed() {
        cacheDisposable.dispose();
        netDisposable.dispose();
        super.onDestroyed();
    }

    public void fireRefresh() {
        this.cacheDisposable.clear();
        this.cacheNowLoading = false;

        this.netDisposable.clear();

        request(false);
    }

    private boolean canLoadMore(){
        return !endOfContent && !requestNow && hasActualNetData && !cacheNowLoading && nonEmpty(data);
    }

    public void fireScrollToEnd() {
        if(canLoadMore()){
            request(true);
        }
    }

    public void fireVideoClick(Video video) {
        if(IVideosListView.ACTION_SELECT.equalsIgnoreCase(action)){
            getView().returnSelectionToParent(video);
        } else {
            getView().showVideoPreview(getAccountId(), video);
        }
    }
}