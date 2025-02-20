package dev.velaron.fennec.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.Collections;
import java.util.List;

import dev.velaron.fennec.Extra;
import dev.velaron.fennec.R;
import dev.velaron.fennec.adapter.VideoAlbumsNewAdapter;
import dev.velaron.fennec.fragment.base.BaseMvpFragment;
import dev.velaron.fennec.listener.EndlessRecyclerOnScrollListener;
import dev.velaron.fennec.listener.PicassoPauseOnScrollListener;
import dev.velaron.fennec.model.VideoAlbum;
import dev.velaron.fennec.mvp.presenter.VideoAlbumsPresenter;
import dev.velaron.fennec.mvp.view.IVideoAlbumsView;
import dev.velaron.fennec.place.PlaceFactory;
import dev.velaron.fennec.util.Objects;
import dev.velaron.fennec.util.ViewUtils;
import dev.velaron.fennec.mvp.core.IPresenterFactory;

public class VideoAlbumsFragment extends BaseMvpFragment<VideoAlbumsPresenter, IVideoAlbumsView>
        implements VideoAlbumsNewAdapter.Listener, IVideoAlbumsView {

    public static VideoAlbumsFragment newInstance(Bundle args){
        VideoAlbumsFragment fragment = new VideoAlbumsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static VideoAlbumsFragment newInstance(int accountId, int ownerId, String action){
        return newInstance(buildArgs(accountId, ownerId, action));
    }

    public static Bundle buildArgs(int aid, int ownerId, String action){
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, aid);
        args.putInt(Extra.OWNER_ID, ownerId);
        args.putString(Extra.ACTION, action);
        return args;
    }

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private VideoAlbumsNewAdapter mAdapter;
    private TextView mEmpty;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_videos, container, false);
        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> getPresenter().fireRefresh());

        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout, true);

        mEmpty = root.findViewById(R.id.empty);

        int columns = requireActivity().getResources().getInteger(R.integer.videos_column_count);
        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(columns, StaggeredGridLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(manager);
        recyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(VideoAlbumsNewAdapter.PICASSO_TAG));
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                getPresenter().fireScrollToLast();
            }
        });

        mAdapter = new VideoAlbumsNewAdapter(requireActivity(), Collections.emptyList());
        mAdapter.setListener(this);
        recyclerView.setAdapter(mAdapter);

        resolveEmptyTextVisibility();
        return root;
    }

    @Override
    public void onClick(VideoAlbum album) {
        getPresenter().fireItemClick(album);
    }

    @Override
    public void displayData(@NonNull List<VideoAlbum> data) {
        if(Objects.nonNull(mAdapter)){
            mAdapter.setData(data);
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void notifyDataAdded(int position, int count) {
        if(Objects.nonNull(mAdapter)){
            mAdapter.notifyItemRangeInserted(position, count);
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void displayLoading(boolean loading) {
        if(Objects.nonNull(mSwipeRefreshLayout)){
            mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(loading));
        }
    }

    private void resolveEmptyTextVisibility() {
        if(Objects.nonNull(mEmpty) && Objects.nonNull(mAdapter)){
            mEmpty.setVisibility(mAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void openAlbum(int accountId, int ownerId, int albumId, String action, String title) {
        PlaceFactory.getVideoAlbumPlace(accountId, ownerId, albumId, action, title).tryOpenWith(requireActivity());
    }

    @Override
    public void notifyDataSetChanged() {
        if(Objects.nonNull(mAdapter)){
            mAdapter.notifyDataSetChanged();
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public IPresenterFactory<VideoAlbumsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int ownerId1 = requireArguments().getInt(Extra.OWNER_ID);
            int accountId = requireArguments().getInt(Extra.ACCOUNT_ID);
            String action = requireArguments().getString(Extra.ACTION);
            return new VideoAlbumsPresenter(accountId, ownerId1, action, saveInstanceState);
        };
    }
}