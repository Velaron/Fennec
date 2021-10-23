package dev.velaron.fennec.fragment.fave;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.velaron.fennec.Constants;
import dev.velaron.fennec.Extra;
import dev.velaron.fennec.R;
import dev.velaron.fennec.adapter.fave.FavePhotosAdapter;
import dev.velaron.fennec.fragment.base.BaseMvpFragment;
import dev.velaron.fennec.listener.EndlessRecyclerOnScrollListener;
import dev.velaron.fennec.listener.PicassoPauseOnScrollListener;
import dev.velaron.fennec.model.Photo;
import dev.velaron.fennec.mvp.presenter.FavePhotosPresenter;
import dev.velaron.fennec.mvp.view.IFavePhotosView;
import dev.velaron.fennec.place.PlaceFactory;
import dev.velaron.fennec.util.ViewUtils;
import biz.dealnote.mvp.core.IPresenterFactory;

import static dev.velaron.fennec.util.Objects.nonNull;

public class FavePhotosFragment extends BaseMvpFragment<FavePhotosPresenter, IFavePhotosView>
        implements SwipeRefreshLayout.OnRefreshListener, IFavePhotosView, FavePhotosAdapter.PhotoSelectionListener {

    public static FavePhotosFragment newInstance(int accountId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        FavePhotosFragment favePhotosFragment = new FavePhotosFragment();
        favePhotosFragment.setArguments(args);
        return favePhotosFragment;
    }

    private TextView mEmpty;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FavePhotosAdapter mAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_fave_photos, container, false);
        RecyclerView recyclerView = root.findViewById(android.R.id.list);
        mEmpty = root.findViewById(R.id.empty);

        int columns = getContext().getResources().getInteger(R.integer.photos_column_count);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireActivity(), columns);
        recyclerView.setLayoutManager(gridLayoutManager);

        recyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                getPresenter().fireScrollToEnd();
            }
        });

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        mAdapter = new FavePhotosAdapter(requireActivity(), Collections.emptyList());
        mAdapter.setPhotoSelectionListener(this);
        recyclerView.setAdapter(mAdapter);

        resolveEmptyTextVisibility();
        return root;
    }

    @Override
    public void onRefresh() {
        getPresenter().fireRefresh();
    }

    @Override
    public void onPhotoClicked(int position, Photo photo) {
        getPresenter().firePhotoClick(position, photo);
    }

    @Override
    public void displayData(List<Photo> photos) {
        if(nonNull(mAdapter)){
            mAdapter.setData(photos);
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if(nonNull(mAdapter)){
            mAdapter.notifyDataSetChanged();
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void notifyDataAdded(int position, int count) {
        if(nonNull(mAdapter)){
            mAdapter.notifyItemRangeInserted(position, count);
            resolveEmptyTextVisibility();
        }
    }

    private void resolveEmptyTextVisibility() {
        if(nonNull(mEmpty) && nonNull(mAdapter)){
            mEmpty.setVisibility(mAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void showRefreshing(boolean refreshing) {
        if(nonNull(mSwipeRefreshLayout)){
            mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(refreshing));
        }
    }

    @Override
    public void goToGallery(int accountId, ArrayList<Photo> photos, int position) {
        PlaceFactory.getFavePhotosGallery(accountId, photos, position)
                .tryOpenWith(requireActivity());
    }

    @Override
    public IPresenterFactory<FavePhotosPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new FavePhotosPresenter(getArguments().getInt(Extra.ACCOUNT_ID), saveInstanceState);
    }
}