package dev.velaron.fennec.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.velaron.fennec.mvp.reflect.OnGuiCreated;
import dev.velaron.fennec.R;
import dev.velaron.fennec.domain.IBoardInteractor;
import dev.velaron.fennec.domain.InteractorFactory;
import dev.velaron.fennec.model.LoadMoreState;
import dev.velaron.fennec.model.Topic;
import dev.velaron.fennec.mvp.presenter.base.AccountDependencyPresenter;
import dev.velaron.fennec.mvp.view.ITopicsView;
import dev.velaron.fennec.util.RxUtils;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by admin on 13.12.2016.
 * phoenix
 */
public class TopicsPresenter extends AccountDependencyPresenter<ITopicsView> {

    private static final int COUNT_PER_REQUEST = 20;

    private final int ownerId;
    private final List<Topic> topics;
    private boolean endOfContent;

    private final IBoardInteractor boardInteractor;
    private boolean actualDataReceived;

    public TopicsPresenter(int accountId, int ownerId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);

        this.ownerId = ownerId;
        this.topics = new ArrayList<>();
        this.boardInteractor = InteractorFactory.createBoardInteractor();

        loadCachedData();
        requestActualData(0);
    }

    private CompositeDisposable cacheDisposable = new CompositeDisposable();
    private boolean cacheLoadingNow;

    private void loadCachedData() {
        final int accountId = super.getAccountId();

        cacheDisposable.add(boardInteractor.getCachedTopics(accountId, ownerId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onCachedDataReceived, RxUtils.ignore()));
    }

    private void onCachedDataReceived(List<Topic> topics) {
        this.cacheLoadingNow = false;

        this.topics.clear();
        this.topics.addAll(topics);

        callView(ITopicsView::notifyDataSetChanged);
    }

    private CompositeDisposable netDisposable = new CompositeDisposable();
    private boolean netLoadingNow;
    private int netLoadingNowOffset;

    private void requestActualData(final int offset) {
        final int accountId = super.getAccountId();

        this.netLoadingNow = true;
        this.netLoadingNowOffset = offset;

        resolveRefreshingView();
        resolveLoadMoreFooter();

        netDisposable.add(boardInteractor.getActualTopics(accountId, ownerId, COUNT_PER_REQUEST, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(topics -> onActualDataReceived(offset, topics), this::onActualDataGetError));
    }

    private void onActualDataGetError(Throwable t) {
        this.netLoadingNow = false;
        resolveRefreshingView();
        resolveLoadMoreFooter();

        showError(getView(), t);
    }

    private void onActualDataReceived(int offset, List<Topic> topics) {
        this.cacheDisposable.clear();
        this.cacheLoadingNow = false;

        this.netLoadingNow = false;
        resolveRefreshingView();
        resolveLoadMoreFooter();

        this.actualDataReceived = true;
        this.endOfContent = topics.isEmpty();

        if (offset == 0) {
            this.topics.clear();
            this.topics.addAll(topics);
            callView(ITopicsView::notifyDataSetChanged);
        } else {
            int startCount = this.topics.size();
            this.topics.addAll(topics);
            callView(view -> view.notifyDataAdd(startCount, topics.size()));
        }
    }

    @Override
    public void onGuiCreated(@NonNull ITopicsView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayData(topics);
    }

    @Override
    public void onDestroyed() {
        this.cacheDisposable.dispose();
        this.netDisposable.dispose();
        super.onDestroyed();
    }

    @OnGuiCreated
    private void resolveRefreshingView() {
        if (isGuiReady()) {
            getView().showRefreshing(netLoadingNow);
        }
    }

    @OnGuiCreated
    private void resolveLoadMoreFooter() {
        if (isGuiReady()) {
            if(netLoadingNow && netLoadingNowOffset > 0){
                getView().setupLoadMore(LoadMoreState.LOADING);
                return;
            }

            if(actualDataReceived && !netLoadingNow){
                getView().setupLoadMore(LoadMoreState.CAN_LOAD_MORE);
            }

            getView().setupLoadMore(LoadMoreState.END_OF_LIST);
        }
    }

    public void fireLoadMoreClick() {
        if (canLoadMore()) {
            requestActualData(this.topics.size());
        }
    }

    private boolean canLoadMore() {
        return actualDataReceived && !cacheLoadingNow && !endOfContent && !topics.isEmpty();
    }

    public void fireButtonCreateClick() {
        safeShowError(getView(), R.string.not_yet_implemented_message);
    }

    public void fireRefresh() {
        this.netDisposable.clear();
        this.netLoadingNow = false;

        this.cacheDisposable.clear();
        this.cacheLoadingNow = false;

        requestActualData(0);
    }

    public void fireTopicClick(Topic topic) {
        getView().goToComments(getAccountId(), topic);
    }

    public void fireScrollToEnd() {
        if (canLoadMore()) {
            requestActualData(this.topics.size());
        }
    }
}