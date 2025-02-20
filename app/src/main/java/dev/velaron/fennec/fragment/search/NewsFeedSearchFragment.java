package dev.velaron.fennec.fragment.search;

import android.os.Bundle;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import dev.velaron.fennec.Extra;
import dev.velaron.fennec.adapter.WallAdapter;
import dev.velaron.fennec.domain.ILikesInteractor;
import dev.velaron.fennec.fragment.search.criteria.NewsFeedCriteria;
import dev.velaron.fennec.model.Post;
import dev.velaron.fennec.mvp.presenter.search.NewsFeedSearchPresenter;
import dev.velaron.fennec.mvp.view.search.INewsFeedSearchView;
import dev.velaron.fennec.util.Utils;
import dev.velaron.fennec.mvp.core.IPresenterFactory;

public class NewsFeedSearchFragment extends AbsSearchFragment<NewsFeedSearchPresenter, INewsFeedSearchView, Post>
        implements WallAdapter.ClickListener, INewsFeedSearchView {

    public static NewsFeedSearchFragment newInstance(int accountId, @Nullable NewsFeedCriteria initialCriteria){
        Bundle args = new Bundle();
        args.putParcelable(Extra.CRITERIA, initialCriteria);
        args.putInt(Extra.ACCOUNT_ID, accountId);
        NewsFeedSearchFragment fragment = new NewsFeedSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    void setAdapterData(RecyclerView.Adapter adapter, List<Post> data) {
        ((WallAdapter)adapter).setItems(data);
    }

    @Override
    RecyclerView.Adapter createAdapter(List<Post> data) {
        return new WallAdapter(requireActivity(), data, this, this);
    }

    @Override
    protected RecyclerView.LayoutManager createLayoutManager() {
        if (Utils.is600dp(requireActivity())) {
            boolean land = Utils.isLandscape(getContext());
            return new StaggeredGridLayoutManager(land ? 2 : 1, StaggeredGridLayoutManager.VERTICAL);
        } else {
            return new LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false);
        }
    }

    @Override
    public void onAvatarClick(int ownerId) {
        getPresenter().fireOwnerClick(ownerId);
    }

    @Override
    public void onShareClick(Post post) {
        getPresenter().fireShareClick(post);
    }

    @Override
    public void onPostClick(Post post) {
        getPresenter().firePostClick(post);
    }

    @Override
    public void onRestoreClick(Post post) {
        // not supported
    }

    @Override
    public void onCommentsClick(Post post) {
        getPresenter().fireCommentsClick(post);
    }

    @Override
    public void onLikeLongClick(Post post) {
        getPresenter().fireCopiesLikesClick("post", post.getOwnerId(), post.getVkid(), ILikesInteractor.FILTER_LIKES);
    }

    @Override
    public void onShareLongClick(Post post) {
        getPresenter().fireCopiesLikesClick("post", post.getOwnerId(), post.getVkid(), ILikesInteractor.FILTER_COPIES);
    }

    @Override
    public void onLikeClick(Post post) {
        getPresenter().fireLikeClick(post);
    }

    @Override
    public IPresenterFactory<NewsFeedSearchPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new NewsFeedSearchPresenter(
                getArguments().getInt(Extra.ACCOUNT_ID),
                getArguments().getParcelable(Extra.CRITERIA),
                saveInstanceState
        );
    }
}