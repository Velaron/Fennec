package dev.velaron.fennec.mvp.presenter;

import static dev.velaron.fennec.util.Objects.isNull;
import static dev.velaron.fennec.util.Objects.nonNull;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.velaron.fennec.Injection;
import dev.velaron.fennec.db.model.PostUpdate;
import dev.velaron.fennec.domain.IFaveInteractor;
import dev.velaron.fennec.domain.IWallsRepository;
import dev.velaron.fennec.domain.InteractorFactory;
import dev.velaron.fennec.domain.Repository;
import dev.velaron.fennec.model.Post;
import dev.velaron.fennec.mvp.presenter.base.PlaceSupportPresenter;
import dev.velaron.fennec.mvp.view.IFavePostsView;
import dev.velaron.fennec.util.Analytics;
import dev.velaron.fennec.util.Pair;
import dev.velaron.fennec.util.RxUtils;
import dev.velaron.fennec.util.Utils;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by Ruslan Kolbasa on 14.07.2017.
 * phoenix
 */
public class FavePostsPresenter extends PlaceSupportPresenter<IFavePostsView> {

    private final List<Post> posts;
    private final IFaveInteractor faveInteractor;

    private final IWallsRepository wallInteractor;

    public FavePostsPresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);

        this.posts = new ArrayList<>();
        this.faveInteractor = InteractorFactory.createFaveInteractor();
        this.wallInteractor = Repository.INSTANCE.getWalls();

        appendDisposable(wallInteractor.observeMinorChanges()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onPostUpdate));

        loadCachedData();
        requestActual(0);
    }

    private void onPostUpdate(PostUpdate update) {
        // likes only
        if(isNull(update.getLikeUpdate())){
            return;
        }

        Pair<Integer, Post> info = Utils.findInfoByPredicate(posts, post -> post.getVkid() == update.getPostId() && post.getOwnerId() == update.getOwnerId());

        if(nonNull(info)){
            Post post = info.getSecond();

            if(getAccountId() == update.getAccountId()){
                post.setUserLikes(update.getLikeUpdate().isLiked());
            }

            post.setLikesCount(update.getLikeUpdate().getCount());
            callView(view -> view.notifyItemChanged(info.getFirst()));
        }
    }

    private boolean requestNow;

    private boolean actualInfoReceived;

    private void setRequestNow(boolean requestNow) {
        this.requestNow = requestNow;
        resolveRefreshingView();
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
    }

    private void resolveRefreshingView() {
        if (isGuiResumed()) {
            getView().showRefreshing(requestNow);
        }
    }

    @Override
    public void onGuiCreated(@NonNull IFavePostsView view) {
        super.onGuiCreated(view);
        view.displayData(posts);
    }

    private final static int COUNT = 50;

    private void requestActual(int offset) {
        setRequestNow(true);
        final int accountId = super.getAccountId();
        final int newOffset = offset + COUNT;
        appendDisposable(faveInteractor.getPosts(accountId, COUNT, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(posts -> onActualDataReceived(offset, newOffset, posts), this::onActualDataGetError));
    }

    private void onActualDataGetError(Throwable throwable) {
        setRequestNow(false);
        showError(getView(), throwable);
    }

    private int nextOffset;

    private void onActualDataReceived(int offset, int newOffset, List<Post> data) {
        setRequestNow(false);

        this.nextOffset = newOffset;
        this.endOfContent = data.isEmpty();
        this.actualInfoReceived = true;

        if (offset == 0) {
            this.posts.clear();
            this.posts.addAll(data);
            callView(IFavePostsView::notifyDataSetChanged);
        } else {
            int sizeBefore = this.posts.size();
            this.posts.addAll(data);
            callView(view -> view.notifyDataAdded(sizeBefore, data.size()));
        }
    }

    private void loadCachedData() {
        final int accountId = super.getAccountId();

        cacheCompositeDisposable.add(faveInteractor.getCachedPosts(accountId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onCachedDataReceived, Analytics::logUnexpectedError));
    }

    private void onCachedDataReceived(List<Post> posts) {
        this.posts.clear();
        this.posts.addAll(posts);
        callView(IFavePostsView::notifyDataSetChanged);
    }

    private CompositeDisposable cacheCompositeDisposable = new CompositeDisposable();

    @Override
    public void onDestroyed() {
        cacheCompositeDisposable.dispose();
        super.onDestroyed();
    }

    public void fireRefresh() {
        if (!requestNow) {
            requestActual(0);
        }
    }

    private boolean endOfContent;

    public void fireScrollToEnd() {
        if (!posts.isEmpty() && actualInfoReceived && !requestNow && !endOfContent) {
            requestActual(nextOffset);
        }
    }

    public void fireLikeClick(Post post) {
        final int accountId = super.getAccountId();
        appendDisposable(wallInteractor.like(accountId, post.getOwnerId(), post.getVkid(), !post.isUserLikes())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(RxUtils.ignore(), this::onLikeError));
    }

    private void onLikeError(Throwable t) {
        showError(getView(), t);
    }
}