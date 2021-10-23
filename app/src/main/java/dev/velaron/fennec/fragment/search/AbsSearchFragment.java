package dev.velaron.fennec.fragment.search;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import dev.velaron.fennec.Extra;
import dev.velaron.fennec.R;
import dev.velaron.fennec.fragment.base.PlaceSupportMvpFragment;
import dev.velaron.fennec.fragment.search.options.BaseOption;
import dev.velaron.fennec.listener.EndlessRecyclerOnScrollListener;
import dev.velaron.fennec.mvp.presenter.search.AbsSearchPresenter;
import dev.velaron.fennec.mvp.view.search.IBaseSearchView;
import dev.velaron.fennec.util.ViewUtils;

import static dev.velaron.fennec.util.Objects.nonNull;

/**
 * Created by admin on 02.05.2017.
 * phoenix
 */
public abstract class AbsSearchFragment<P extends AbsSearchPresenter<V, ?, T, ?>, V extends IBaseSearchView<T>, T>
        extends PlaceSupportMvpFragment<P, V> implements IBaseSearchView<T> {

    private static final int REQUEST_FILTER_EDIT = 19;
    private RecyclerView.Adapter mAdapter;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private TextView mEmptyText;

    private void onSeachOptionsChanged(){
        getPresenter().fireOptionsChanged();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_search, container, false);

        RecyclerView recyclerView = root.findViewById(R.id.list);
        RecyclerView.LayoutManager manager = createLayoutManager();
        recyclerView.setLayoutManager(manager);
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                getPresenter().fireScrollToEnd();
            }
        });

        mAdapter = createAdapter(Collections.emptyList());
        recyclerView.setAdapter(mAdapter);

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> getPresenter().fireRefresh());
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        mEmptyText = root.findViewById(R.id.empty);
        mEmptyText.setText(getEmptyText());
        return root;
    }

    @StringRes
    int getEmptyText() {
        return R.string.list_is_empty;
    }

    public void fireTextQueryEdit(String q) {
        getPresenter().fireTextQueryEdit(q);
    }

    @Override
    public void displayData(List<T> data) {
        if (nonNull(mAdapter)) {
            setAdapterData(mAdapter, data);
        }
    }

    @Override
    public void notifyItemChanged(int index) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemChanged(index);
        }
    }

    @Override
    public void setEmptyTextVisible(boolean visible) {
        if (nonNull(mEmptyText)) {
            mEmptyText.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (nonNull(mAdapter)) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyDataAdded(int position, int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRangeInserted(position, count);
        }
    }

    @Override
    public void showLoading(boolean loading) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.setRefreshing(loading);
        }
    }

    public static final String ACTION_QUERY = "action_query";

    /**
     * Метод будет вызван, когда внутри viewpager this фрагмент будет выбран
     */
    public void syncYourCriteriaWithParent() {
        getPresenter().fireSyncCriteriaRequest();
    }

    public void openSearchFilter(){
        getPresenter().fireOpenFilterClick();
    }

    @Override
    public void displayFilter(int accountId, ArrayList<BaseOption> options) {
        FilterEditFragment fragment = FilterEditFragment.newInstance(accountId, options);
        fragment.setTargetFragment(this, REQUEST_FILTER_EDIT);
        fragment.show(getFragmentManager(), "filter-edit");
    }

    @Override
    public void displaySearchQuery(String query) {
        Intent data = new Intent(ACTION_QUERY);
        data.putExtra(Extra.Q, query);

        if (nonNull(getTargetFragment())) {
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, data);
        }

        if (nonNull(getParentFragment())) {
            getParentFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, data);
        }
    }

    abstract void setAdapterData(RecyclerView.Adapter adapter, List<T> data);

    abstract RecyclerView.Adapter createAdapter(List<T> data);

    abstract RecyclerView.LayoutManager createLayoutManager();
}
