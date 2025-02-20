package dev.velaron.fennec.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.Collections;
import java.util.List;

import dev.velaron.fennec.Extra;
import dev.velaron.fennec.R;
import dev.velaron.fennec.activity.ActivityFeatures;
import dev.velaron.fennec.activity.ActivityUtils;
import dev.velaron.fennec.adapter.NewsfeedCommentsAdapter;
import dev.velaron.fennec.fragment.base.PlaceSupportMvpFragment;
import dev.velaron.fennec.listener.EndlessRecyclerOnScrollListener;
import dev.velaron.fennec.listener.OnSectionResumeCallback;
import dev.velaron.fennec.model.NewsfeedComment;
import dev.velaron.fennec.model.Post;
import dev.velaron.fennec.mvp.presenter.NewsfeedCommentsPresenter;
import dev.velaron.fennec.mvp.view.INewsfeedCommentsView;
import dev.velaron.fennec.place.Place;
import dev.velaron.fennec.settings.Settings;
import dev.velaron.fennec.util.Utils;
import dev.velaron.fennec.mvp.core.IPresenterFactory;

import static dev.velaron.fennec.util.Objects.nonNull;
import static dev.velaron.fennec.util.Utils.isLandscape;

/**
 * Created by admin on 08.05.2017.
 * phoenix
 */
public class NewsfeedCommentsFragment extends PlaceSupportMvpFragment<NewsfeedCommentsPresenter, INewsfeedCommentsView>
        implements INewsfeedCommentsView, NewsfeedCommentsAdapter.ActionListener {

    public static NewsfeedCommentsFragment newInstance(int accountId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        NewsfeedCommentsFragment fragment = new NewsfeedCommentsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private NewsfeedCommentsAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_newsfeed_comments, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> getPresenter().fireRefresh());

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);

        RecyclerView.LayoutManager manager;
        if (Utils.is600dp(requireActivity())) {
            manager = new StaggeredGridLayoutManager(isLandscape(requireActivity()) ? 2 : 1, StaggeredGridLayoutManager.VERTICAL);
        } else {
            manager = new LinearLayoutManager(requireActivity());
        }

        recyclerView.setLayoutManager(manager);
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                getPresenter().fireScrollToEnd();
            }
        });

        mAdapter = new NewsfeedCommentsAdapter(requireActivity(), Collections.emptyList(), this);
        mAdapter.setActionListener(this);
        mAdapter.setOwnerClickListener(this);

        recyclerView.setAdapter(mAdapter);
        return root;
    }

    @Override
    public IPresenterFactory<NewsfeedCommentsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int accountId = requireArguments().getInt(Extra.ACCOUNT_ID);
            return new NewsfeedCommentsPresenter(accountId, saveInstanceState);
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        Settings.get().ui().notifyPlaceResumed(Place.NEWSFEED_COMMENTS);

        ActivityUtils.setToolbarTitle(this, R.string.drawer_newsfeed_comments);
        ActivityUtils.setToolbarSubtitle(this, null);

        if (requireActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) requireActivity()).onSectionResume(AdditionalNavigationFragment.SECTION_ITEM_NEWSFEED_COMMENTS);
        }

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @Override
    public void displayData(List<NewsfeedComment> data) {
        if (nonNull(mAdapter)) {
            mAdapter.setData(data);
        }
    }

    @Override
    public void notifyDataAdded(int position, int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRangeInserted(position, count);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (nonNull(mAdapter)) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void showLoading(boolean loading) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.setRefreshing(loading);
        }
    }

    @Override
    public void onPostBodyClick(NewsfeedComment comment) {
        getPresenter().firePostClick((Post) comment.getModel());
    }

    @Override
    public void onCommentBodyClick(NewsfeedComment comment) {
        getPresenter().fireCommentBodyClick(comment);
    }
}