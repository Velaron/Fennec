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

import java.util.Collections;
import java.util.List;

import dev.velaron.fennec.Constants;
import dev.velaron.fennec.Extra;
import dev.velaron.fennec.R;
import dev.velaron.fennec.adapter.fave.FavePagesAdapter;
import dev.velaron.fennec.fragment.base.BaseMvpFragment;
import dev.velaron.fennec.listener.EndlessRecyclerOnScrollListener;
import dev.velaron.fennec.listener.PicassoPauseOnScrollListener;
import dev.velaron.fennec.model.FavePage;
import dev.velaron.fennec.model.Owner;
import dev.velaron.fennec.mvp.presenter.FavePagesPresenter;
import dev.velaron.fennec.mvp.view.IFaveUsersView;
import dev.velaron.fennec.place.PlaceFactory;
import dev.velaron.fennec.util.ViewUtils;
import dev.velaron.fennec.mvp.core.IPresenterFactory;

import static dev.velaron.fennec.util.Objects.nonNull;

public class FavePagesFragment extends BaseMvpFragment<FavePagesPresenter, IFaveUsersView> implements IFaveUsersView, FavePagesAdapter.ClickListener {

    public static FavePagesFragment newInstance(int accountId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        FavePagesFragment fragment = new FavePagesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private TextView mEmpty;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FavePagesAdapter mAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_fave_users, container, false);
        mEmpty = root.findViewById(R.id.empty);

        RecyclerView recyclerView = root.findViewById(android.R.id.list);
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
        mSwipeRefreshLayout.setOnRefreshListener(() -> getPresenter().fireRefresh());
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        mAdapter = new FavePagesAdapter(Collections.emptyList(), requireActivity());
        mAdapter.setClickListener(this);

        recyclerView.setAdapter(mAdapter);

        resolveEmptyText();
        return root;
    }

    private void resolveEmptyText() {
        if (nonNull(mEmpty) && nonNull(mAdapter)) {
            mEmpty.setVisibility(mAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void displayData(List<FavePage> users) {
        if (nonNull(mAdapter)) {
            mAdapter.setData(users);
            resolveEmptyText();
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (nonNull(mAdapter)) {
            mAdapter.notifyDataSetChanged();
            resolveEmptyText();
        }
    }

    @Override
    public void notifyDataAdded(int position, int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRangeInserted(position, count);
            resolveEmptyText();
        }
    }

    @Override
    public void showRefreshing(boolean refreshing) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    @Override
    public void openOwnerWall(int accountId, Owner owner) {
        PlaceFactory.getOwnerWallPlace(accountId, owner).tryOpenWith(requireActivity());
    }

    @Override
    public void notifyItemRemoved(int index) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRemoved(index);
            resolveEmptyText();
        }
    }

    @Override
    public IPresenterFactory<FavePagesPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new FavePagesPresenter(
                getArguments().getInt(Extra.ACCOUNT_ID),
                saveInstanceState
        );
    }

    @Override
    public void onPageClick(int index, Owner owner) {
        getPresenter().fireOwnerClick(owner);
    }

    @Override
    public void onDelete(int index, Owner owner) {
        getPresenter().fireOwnerDelete(owner);
    }
}