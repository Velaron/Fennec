package dev.velaron.fennec.fragment.fave;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import dev.velaron.fennec.Constants;
import dev.velaron.fennec.Extra;
import dev.velaron.fennec.R;
import dev.velaron.fennec.adapter.WallAdapter;
import dev.velaron.fennec.domain.ILikesInteractor;
import dev.velaron.fennec.fragment.base.PlaceSupportMvpFragment;
import dev.velaron.fennec.listener.EndlessRecyclerOnScrollListener;
import dev.velaron.fennec.listener.PicassoPauseOnScrollListener;
import dev.velaron.fennec.model.Poll;
import dev.velaron.fennec.model.Post;
import dev.velaron.fennec.mvp.presenter.FavePostsPresenter;
import dev.velaron.fennec.mvp.view.IFavePostsView;
import dev.velaron.fennec.util.Utils;
import dev.velaron.fennec.util.ViewUtils;
import biz.dealnote.mvp.core.IPresenterFactory;

import static dev.velaron.fennec.util.Objects.nonNull;

public class FavePostsFragment extends PlaceSupportMvpFragment<FavePostsPresenter, IFavePostsView>
        implements WallAdapter.ClickListener, IFavePostsView {

    public static FavePostsFragment newInstance(int accountId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        FavePostsFragment favePostsFragment = new FavePostsFragment();
        favePostsFragment.setArguments(args);
        return favePostsFragment;
    }

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private WallAdapter mAdapter;
    private TextView mEmpty;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_fave_posts, container, false);

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> getPresenter().fireRefresh());
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        mEmpty = root.findViewById(R.id.empty);

        RecyclerView.LayoutManager manager;
        if (Utils.is600dp(requireActivity())) {
            manager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        } else {
            manager = new LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false);
        }

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(manager);

        recyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                getPresenter().fireScrollToEnd();
            }
        });

        mAdapter = new WallAdapter(requireActivity(), Collections.emptyList(), this, this);
        recyclerView.setAdapter(mAdapter);
        return root;
    }

    private void resolveEmptyText() {
        if (nonNull(mEmpty) && nonNull(mAdapter)) {
            mEmpty.setVisibility(mAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onPollOpen(@NonNull Poll poll) {
        getPresenter().firePollClick(poll);
    }

    @Override
    public void onAvatarClick(int ownerId) {
        this.onOpenOwner(ownerId);
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
        // not supported ?
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
    public void displayData(List<Post> posts) {
        if(nonNull(mAdapter)){
            mAdapter.setItems(posts);
            resolveEmptyText();
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if(nonNull(mAdapter)){
            mAdapter.notifyDataSetChanged();
            resolveEmptyText();
        }
    }

    @Override
    public void notifyDataAdded(int position, int count) {
        if(nonNull(mAdapter)){
            mAdapter.notifyItemRangeInserted(position + mAdapter.getHeadersCount(), count);
            resolveEmptyText();
        }
    }

    @Override
    public void showRefreshing(boolean refreshing) {
        if(nonNull(mSwipeRefreshLayout)){
            mSwipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    @Override
    public void notifyItemChanged(int index) {
        if(nonNull(mAdapter)){
            mAdapter.notifyItemChanged(index + mAdapter.getHeadersCount());
        }
    }

    @Override
    public IPresenterFactory<FavePostsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new FavePostsPresenter(getArguments().getInt(Extra.ACCOUNT_ID), saveInstanceState);
    }
}