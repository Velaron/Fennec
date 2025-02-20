package dev.velaron.fennec.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.velaron.fennec.Extra;
import dev.velaron.fennec.R;
import dev.velaron.fennec.activity.ActivityFeatures;
import dev.velaron.fennec.activity.ActivityUtils;
import dev.velaron.fennec.adapter.CommunitiesAdapter;
import dev.velaron.fennec.fragment.base.BaseMvpFragment;
import dev.velaron.fennec.listener.AppStyleable;
import dev.velaron.fennec.listener.BackPressCallback;
import dev.velaron.fennec.listener.EndlessRecyclerOnScrollListener;
import dev.velaron.fennec.model.Community;
import dev.velaron.fennec.model.DataWrapper;
import dev.velaron.fennec.mvp.presenter.CommunitiesPresenter;
import dev.velaron.fennec.mvp.view.ICommunitiesView;
import dev.velaron.fennec.place.Place;
import dev.velaron.fennec.place.PlaceFactory;
import dev.velaron.fennec.settings.Settings;
import dev.velaron.fennec.util.Utils;
import dev.velaron.fennec.view.MySearchView;
import dev.velaron.fennec.mvp.core.IPresenterFactory;

import static dev.velaron.fennec.util.Objects.nonNull;

/**
 * Created by admin on 19.09.2017.
 * phoenix
 */
public class CommunitiesFragment extends BaseMvpFragment<CommunitiesPresenter, ICommunitiesView>
        implements ICommunitiesView, MySearchView.OnQueryTextListener, CommunitiesAdapter.ActionListener, BackPressCallback, MySearchView.OnBackButtonClickListener {

    public static CommunitiesFragment newInstance(int accountId, int userId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.USER_ID, userId);
        CommunitiesFragment fragment = new CommunitiesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private CommunitiesAdapter mAdapter;

    private MySearchView mSearchView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_communities, container, false);
        //((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> getPresenter().fireRefresh());

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                getPresenter().fireScrollToEnd();
            }
        });

        mAdapter = new CommunitiesAdapter(requireActivity(), Collections.emptyList(), new int[0]);
        mAdapter.setActionListener(this);

        recyclerView.setAdapter(mAdapter);

        mSearchView = root.findViewById(R.id.searchview);
        mSearchView.setOnBackButtonClickListener(this);
        mSearchView.setRightButtonVisibility(false);
        mSearchView.setOnQueryTextListener(this);

        resolveLeftButton();
        return root;
    }

    private void resolveLeftButton() {
        FragmentActivity activity = requireActivity();

        try {
            if (nonNull(mSearchView)) {
                int count = activity.getSupportFragmentManager().getBackStackEntryCount();
                mSearchView.setLeftIcon(count == 1 && activity instanceof AppStyleable ?
                        R.drawable.phoenix_round : R.drawable.arrow_left);
            }
        } catch (Exception ignored) {
        }
    }

    private FragmentManager.OnBackStackChangedListener backStackChangedListener = this::resolveLeftButton;

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        if (context instanceof AppCompatActivity) {
            ((AppCompatActivity) context).getSupportFragmentManager().addOnBackStackChangedListener(backStackChangedListener);
        }
    }

    @Override
    public void onDetach() {
        requireActivity().getSupportFragmentManager().removeOnBackStackChangedListener(backStackChangedListener);
        super.onDetach();
    }

    @Override
    public void displayData(DataWrapper<Community> own, DataWrapper<Community> filtered, DataWrapper<Community> seacrh) {
        if (nonNull(mAdapter)) {
            List<DataWrapper<Community>> wrappers = new ArrayList<>();
            wrappers.add(own);
            wrappers.add(filtered);
            wrappers.add(seacrh);

            int[] titles = {R.string.my_communities_title, R.string.quick_search_title, R.string.other};
            mAdapter.setData(wrappers, titles);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Settings.get().ui().notifyPlaceResumed(Place.COMMUNITIES);

        ActivityUtils.setToolbarTitle(this, R.string.groups);
        ActivityUtils.setToolbarSubtitle(this, null); // TODO: 04.10.2017

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @Override
    public void notifyDataSetChanged() {
        if (nonNull(mAdapter)) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyOwnDataAdded(int position, int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRangeInserted(0, position, count);
        }
    }

    @Override
    public void displayRefreshing(boolean refreshing) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    @Override
    public void showCommunityWall(int accountId, Community community) {
        PlaceFactory.getOwnerWallPlace(accountId, community).tryOpenWith(requireActivity());
    }

    @Override
    public void notifySeacrhDataAdded(int position, int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRangeInserted(2, position, count);
        }
    }

    @Override
    public IPresenterFactory<CommunitiesPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new CommunitiesPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                requireArguments().getInt(Extra.USER_ID),
                saveInstanceState
        );
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        getPresenter().fireSearchQueryChanged(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        getPresenter().fireSearchQueryChanged(newText);
        return true;
    }

    @Override
    public void onCommunityClick(Community community) {
        getPresenter().fireCommunityClick(community);
    }

    @Override
    public boolean onBackPressed() {
        CharSequence query = mSearchView.getText();
        if (Utils.isEmpty(query)) {
            return true;
        }

        mSearchView.setQuery("");
        return false;
    }

    @Override
    public void onBackButtonClick() {
        if (requireActivity().getSupportFragmentManager().getBackStackEntryCount() == 1 && requireActivity() instanceof AppStyleable) {
            ((AppStyleable) requireActivity()).openMenu(true);
        } else {
            requireActivity().onBackPressed();
        }
    }
}