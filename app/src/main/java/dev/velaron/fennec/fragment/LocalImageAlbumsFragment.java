package dev.velaron.fennec.fragment;

import android.Manifest;
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

import dev.velaron.fennec.R;
import dev.velaron.fennec.adapter.LocalPhotoAlbumsAdapter;
import dev.velaron.fennec.fragment.base.BaseMvpFragment;
import dev.velaron.fennec.listener.PicassoPauseOnScrollListener;
import dev.velaron.fennec.model.LocalImageAlbum;
import dev.velaron.fennec.mvp.presenter.LocalPhotoAlbumsPresenter;
import dev.velaron.fennec.mvp.view.ILocalPhotoAlbumsView;
import dev.velaron.fennec.place.PlaceFactory;
import dev.velaron.fennec.util.Objects;
import dev.velaron.fennec.util.ViewUtils;
import biz.dealnote.mvp.core.IPresenterFactory;

public class LocalImageAlbumsFragment extends BaseMvpFragment<LocalPhotoAlbumsPresenter, ILocalPhotoAlbumsView>
        implements LocalPhotoAlbumsAdapter.ClickListener, SwipeRefreshLayout.OnRefreshListener, ILocalPhotoAlbumsView {

    private static final int REQYEST_PERMISSION_READ_EXTERNAL_STORAGE = 89;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mEmptyTextView;

    private LocalPhotoAlbumsAdapter mAlbumsAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_local_albums_gallery, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if(!hasHideToolbarExtra()){
            ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        } else {
            toolbar.setVisibility(View.GONE);
        }

        mSwipeRefreshLayout = view.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout, true);

        int columnCount = getResources().getInteger(R.integer.photos_albums_column_count);
        RecyclerView.LayoutManager manager = new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);

        mRecyclerView = view.findViewById(R.id.list);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(LocalPhotoAlbumsAdapter.PICASSO_TAG));

        mAlbumsAdapter = new LocalPhotoAlbumsAdapter(Collections.emptyList());
        mAlbumsAdapter.setClickListener(this);

        mRecyclerView.setAdapter(mAlbumsAdapter);

        mEmptyTextView = view.findViewById(R.id.empty);
        return view;
    }

    @Override
    public void onClick(LocalImageAlbum album) {
        getPresenter().fireAlbumClick(album);
    }

    @Override
    public void onRefresh() {
        getPresenter().fireRefresh();
    }

    @Override
    public void displayData(@NonNull List<LocalImageAlbum> data) {
        if(Objects.nonNull(mRecyclerView)){
            mAlbumsAdapter.setData(data);
        }
    }

    @Override
    public void setEmptyTextVisible(boolean visible) {
        if(Objects.nonNull(mEmptyTextView)){
            mEmptyTextView.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void displayProgress(boolean loading) {
        if(Objects.nonNull(mSwipeRefreshLayout)){
            mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(loading));
        }
    }

    @Override
    public void openAlbum(@NonNull LocalImageAlbum album) {
        PlaceFactory.getLocalImageAlbumPlace(album).tryOpenWith(requireActivity());
    }

    @Override
    public void notifyDataChanged() {
        if(Objects.nonNull(mAlbumsAdapter)){
            mAlbumsAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void requestReadExternalStoragePermission() {
        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQYEST_PERMISSION_READ_EXTERNAL_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQYEST_PERMISSION_READ_EXTERNAL_STORAGE){
            getPresenter().fireReadExternalStoregePermissionResolved();
        }
    }

    @Override
    public IPresenterFactory<LocalPhotoAlbumsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new LocalPhotoAlbumsPresenter(saveInstanceState);
    }
}