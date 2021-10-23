package dev.velaron.fennec.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.Collections;
import java.util.List;

import dev.velaron.fennec.Constants;
import dev.velaron.fennec.Extra;
import dev.velaron.fennec.R;
import dev.velaron.fennec.activity.ActivityFeatures;
import dev.velaron.fennec.adapter.VideosAdapter;
import dev.velaron.fennec.fragment.base.BaseMvpFragment;
import dev.velaron.fennec.listener.EndlessRecyclerOnScrollListener;
import dev.velaron.fennec.listener.OnSectionResumeCallback;
import dev.velaron.fennec.listener.PicassoPauseOnScrollListener;
import dev.velaron.fennec.model.Video;
import dev.velaron.fennec.mvp.presenter.VideosListPresenter;
import dev.velaron.fennec.mvp.view.IVideosListView;
import dev.velaron.fennec.place.PlaceFactory;
import dev.velaron.fennec.util.Utils;
import dev.velaron.fennec.util.ViewUtils;
import biz.dealnote.mvp.core.IPresenterFactory;

import static dev.velaron.fennec.util.Objects.nonNull;

/**
 * Created by admin on 21.11.2016.
 * phoenix
 */
public class VideosFragment extends BaseMvpFragment<VideosListPresenter, IVideosListView>
        implements IVideosListView, VideosAdapter.VideoOnClickListener {

    public static final String EXTRA_IN_TABS_CONTAINER = "in_tabs_container";
    public static final String EXTRA_ALBUM_TITLE = "album_title";

    public static Bundle buildArgs(int accoutnId, int ownerId, int albumId, String action, @Nullable String albumTitle) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accoutnId);
        args.putInt(Extra.ALBUM_ID, albumId);
        args.putInt(Extra.OWNER_ID, ownerId);
        if (albumTitle != null) {
            args.putString(EXTRA_ALBUM_TITLE, albumTitle);
        }

        args.putString(Extra.ACTION, action);
        return args;
    }

    @Override
    public IPresenterFactory<VideosListPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int accountId = requireArguments().getInt(Extra.ACCOUNT_ID);
            int albumId = requireArguments().getInt(Extra.ALBUM_ID);
            int ownerId = requireArguments().getInt(Extra.OWNER_ID);

            String optAlbumTitle = requireArguments().getString(EXTRA_ALBUM_TITLE);
            String action = requireArguments().getString(Extra.ACTION);
            return new VideosListPresenter(accountId, ownerId, albumId, action, optAlbumTitle, saveInstanceState);
        };
    }

    public static VideosFragment newInstance(int accoutnId, int ownerId, int albumId, String action, @Nullable String albumTitle) {
        return newInstance(buildArgs(accoutnId, ownerId, albumId, action, albumTitle));
    }

    public static VideosFragment newInstance(Bundle args) {
        VideosFragment fragment = new VideosFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * True - если фрагмент находится внутри TabLayout
     */
    private boolean inTabsContainer;

    private VideosAdapter mAdapter;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mEmpty;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inTabsContainer = requireArguments().getBoolean(EXTRA_IN_TABS_CONTAINER);
    }

    @Override
    public void setToolbarTitle(String title) {
        if (!inTabsContainer) {
            super.setToolbarTitle(title);
        }
    }

    @Override
    public void setToolbarSubtitle(String subtitle) {
        if (!inTabsContainer) {
            super.setToolbarSubtitle(subtitle);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setToolbarTitle(getString(R.string.videos));

        if (!inTabsContainer) {
            if (requireActivity() instanceof OnSectionResumeCallback) {
                ((OnSectionResumeCallback) requireActivity()).onClearSelection();
            }

            new ActivityFeatures.Builder()
                    .begin()
                    .setHideNavigationMenu(false)
                    .setBarsColored(requireActivity(), true)
                    .build()
                    .apply(requireActivity());
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_videos, container, false);

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);

        Toolbar toolbar = root.findViewById(R.id.toolbar);

        if (!inTabsContainer) {
            toolbar.setVisibility(View.VISIBLE);
            ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        } else {
            toolbar.setVisibility(View.GONE);
        }

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> getPresenter().fireRefresh());

        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout, true);

        mEmpty = root.findViewById(R.id.empty);

        int columns = requireActivity().getResources().getInteger(R.integer.videos_column_count);
        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(columns, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                getPresenter().fireScrollToEnd();
            }
        });

        mAdapter = new VideosAdapter(requireActivity(), Collections.emptyList());
        mAdapter.setVideoOnClickListener(this);
        recyclerView.setAdapter(mAdapter);

        resolveEmptyTextVisibility();
        return root;
    }

    @Override
    public void onVideoClick(int position, Video video) {
        getPresenter().fireVideoClick(video);
    }

    @Override
    public void displayData(@NonNull List<Video> data) {
        if (nonNull(mAdapter)) {
            mAdapter.setData(data);
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void notifyDataAdded(int position, int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRangeInserted(position, count);
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void displayLoading(boolean loading) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.setRefreshing(loading);
        }
    }

    private void resolveEmptyTextVisibility() {
        if (nonNull(mEmpty) && nonNull(mAdapter)) {
            mEmpty.setVisibility(mAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (nonNull(mAdapter)) {
            mAdapter.notifyDataSetChanged();
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void returnSelectionToParent(Video video) {
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(Extra.ATTACHMENTS, Utils.singletonArrayList(video));
        requireActivity().setResult(Activity.RESULT_OK, intent);
        requireActivity().finish();
    }

    @Override
    public void showVideoPreview(int accountId, Video video) {
        PlaceFactory.getVideoPreviewPlace(accountId, video).tryOpenWith(requireActivity());
    }
}
