package dev.velaron.fennec.mvp.presenter.search;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import dev.velaron.fennec.Injection;
import dev.velaron.fennec.api.model.VKApiPost;
import dev.velaron.fennec.db.model.PostUpdate;
import dev.velaron.fennec.domain.IFeedInteractor;
import dev.velaron.fennec.domain.IWallsRepository;
import dev.velaron.fennec.domain.InteractorFactory;
import dev.velaron.fennec.domain.Repository;
import dev.velaron.fennec.fragment.search.criteria.NewsFeedCriteria;
import dev.velaron.fennec.fragment.search.nextfrom.StringNextFrom;
import dev.velaron.fennec.model.Commented;
import dev.velaron.fennec.model.Post;
import dev.velaron.fennec.mvp.view.search.INewsFeedSearchView;
import dev.velaron.fennec.util.Pair;
import dev.velaron.fennec.util.RxUtils;
import dev.velaron.fennec.util.Utils;
import io.reactivex.Single;

/**
 * Created by admin on 03.10.2017.
 * phoenix
 */
public class NewsFeedSearchPresenter extends AbsSearchPresenter<INewsFeedSearchView, NewsFeedCriteria, Post, StringNextFrom> {

    private final IFeedInteractor feedInteractor;

    private final IWallsRepository walls;

    public NewsFeedSearchPresenter(int accountId, @Nullable NewsFeedCriteria criteria, @Nullable Bundle savedInstanceState) {
        super(accountId, criteria, savedInstanceState);
        this.feedInteractor = InteractorFactory.createFeedInteractor();
        this.walls = Repository.INSTANCE.getWalls();

        appendDisposable(walls.observeMinorChanges()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onPostUpdate, RxUtils.ignore()));
    }

    private void onPostUpdate(PostUpdate update){
        // TODO: 03.10.2017
    }

    @Override
    StringNextFrom getInitialNextFrom() {
        return new StringNextFrom(null);
    }

    @Override
    boolean isAtLast(StringNextFrom startFrom) {
        return Utils.isEmpty(startFrom.getNextFrom());
    }

    @Override
    Single<Pair<List<Post>, StringNextFrom>> doSearch(int accountId, NewsFeedCriteria criteria, StringNextFrom startFrom) {
        return feedInteractor.search(accountId, criteria, 50, startFrom.getNextFrom())
                .map(pair -> Pair.Companion.create(pair.getFirst(), new StringNextFrom(pair.getSecond())));
    }

    @Override
    NewsFeedCriteria instantiateEmptyCriteria() {
        return new NewsFeedCriteria("");
    }

    @Override
    public void firePostClick(@NonNull Post post) {
        if (post.getPostType() == VKApiPost.Type.REPLY) {
            getView().openComments(getAccountId(), Commented.from(post), post.getVkid());
        } else {
            getView().openPost(getAccountId(), post);
        }
    }

    @Override
    boolean canSearch(NewsFeedCriteria criteria) {
        return Utils.nonEmpty(criteria.getQuery());
    }

    public void fireLikeClick(Post post) {
        final int accountId = super.getAccountId();

        appendDisposable(walls.like(accountId, post.getOwnerId(), post.getVkid(), !post.isUserLikes())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(RxUtils.ignore(), t -> showError(getView(), t)));
    }
}