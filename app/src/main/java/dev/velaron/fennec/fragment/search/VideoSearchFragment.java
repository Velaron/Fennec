package dev.velaron.fennec.fragment.search;

import android.os.Bundle;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import dev.velaron.fennec.Extra;
import dev.velaron.fennec.R;
import dev.velaron.fennec.adapter.VideosAdapter;
import dev.velaron.fennec.fragment.search.criteria.VideoSearchCriteria;
import dev.velaron.fennec.model.Video;
import dev.velaron.fennec.mvp.presenter.search.VideosSearchPresenter;
import dev.velaron.fennec.mvp.view.search.IVideosSearchView;
import biz.dealnote.mvp.core.IPresenterFactory;

public class VideoSearchFragment extends AbsSearchFragment<VideosSearchPresenter, IVideosSearchView, Video>
        implements VideosAdapter.VideoOnClickListener {

    public static VideoSearchFragment newInstance(int accountId, @Nullable VideoSearchCriteria initialCriteria){
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putParcelable(Extra.CRITERIA, initialCriteria);
        VideoSearchFragment fragment = new VideoSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    void setAdapterData(RecyclerView.Adapter adapter, List<Video> data) {
        ((VideosAdapter)adapter).setData(data);
    }

    @Override
    RecyclerView.Adapter createAdapter(List<Video> data) {
        VideosAdapter adapter = new VideosAdapter(requireActivity(), data);
        adapter.setVideoOnClickListener(this);
        return adapter;
    }

    @Override
    protected RecyclerView.LayoutManager createLayoutManager() {
        int columns = getContext().getResources().getInteger(R.integer.videos_column_count);
        return new StaggeredGridLayoutManager(columns, StaggeredGridLayoutManager.VERTICAL);
    }

    @Override
    public void onVideoClick(int position, Video video) {
        getPresenter().fireVideoClick(video);
    }

    @Override
    public IPresenterFactory<VideosSearchPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new VideosSearchPresenter(
                getArguments().getInt(Extra.ACCOUNT_ID),
                getArguments().getParcelable(Extra.CRITERIA),
                saveInstanceState
        );
    }
}