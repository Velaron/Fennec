package dev.velaron.fennec.fragment.search;

import android.os.Bundle;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import dev.velaron.fennec.Extra;
import dev.velaron.fennec.adapter.WallAdapter;
import dev.velaron.fennec.fragment.search.criteria.WallSearchCriteria;
import dev.velaron.fennec.model.Post;
import dev.velaron.fennec.mvp.presenter.search.WallSearchPresenter;
import dev.velaron.fennec.mvp.view.search.IWallSearchView;
import dev.velaron.fennec.util.Utils;
import dev.velaron.fennec.mvp.core.IPresenterFactory;

/**
 * Created by admin on 02.05.2017.
 * phoenix
 */
public class WallSearchFragment extends AbsSearchFragment<WallSearchPresenter, IWallSearchView, Post>
        implements IWallSearchView, WallAdapter.ClickListener {

    public static WallSearchFragment newInstance(int accountId, WallSearchCriteria criteria) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putParcelable(Extra.CRITERIA, criteria);
        WallSearchFragment fragment = new WallSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public IPresenterFactory<WallSearchPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int accountId = getArguments().getInt(Extra.ACCOUNT_ID);
            WallSearchCriteria c = getArguments().getParcelable(Extra.CRITERIA);
            return new WallSearchPresenter(accountId, c, saveInstanceState);
        };
    }

    @Override
    void setAdapterData(RecyclerView.Adapter adapter, List<Post> data) {
        ((WallAdapter) adapter).setItems(data);
    }

    @Override
    public void onAvatarClick(int ownerId) {
        super.onOwnerClick(ownerId);
    }

    @Override
    RecyclerView.Adapter createAdapter(List<Post> data) {
        WallAdapter adapter = new WallAdapter(requireActivity(), data, this, this);
        return adapter;
    }

    @Override
    RecyclerView.LayoutManager createLayoutManager() {
        RecyclerView.LayoutManager manager;

        if (Utils.is600dp(requireActivity())) {
            boolean land = Utils.isLandscape(requireActivity());
            manager = new StaggeredGridLayoutManager(land ? 2 : 1, StaggeredGridLayoutManager.VERTICAL);
        } else {
            manager = new LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false);
        }

        return manager;
    }

    @Override
    public void onOwnerClick(int ownerId) {
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

    }

    @Override
    public void onCommentsClick(Post post) {
        getPresenter().fireCommentsClick(post);
    }

    @Override
    public void onLikeLongClick(Post post) {
        getPresenter().fireShowLikesClick(post);
    }

    @Override
    public void onShareLongClick(Post post) {
        getPresenter().fireShowCopiesClick(post);
    }

    @Override
    public void onLikeClick(Post post) {
        getPresenter().fireLikeClick(post);
    }
}
