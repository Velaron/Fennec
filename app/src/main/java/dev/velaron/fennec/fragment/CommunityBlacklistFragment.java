package dev.velaron.fennec.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import dev.velaron.fennec.Extra;
import dev.velaron.fennec.R;
import dev.velaron.fennec.activity.SelectProfilesActivity;
import dev.velaron.fennec.adapter.CommunityBannedAdapter;
import dev.velaron.fennec.fragment.base.BaseMvpFragment;
import dev.velaron.fennec.fragment.search.SearchContentType;
import dev.velaron.fennec.fragment.search.criteria.PeopleSearchCriteria;
import dev.velaron.fennec.listener.EndlessRecyclerOnScrollListener;
import dev.velaron.fennec.model.Banned;
import dev.velaron.fennec.model.SelectProfileCriteria;
import dev.velaron.fennec.model.User;
import dev.velaron.fennec.mvp.presenter.CommunityBlacklistPresenter;
import dev.velaron.fennec.mvp.view.ICommunityBlacklistView;
import dev.velaron.fennec.place.Place;
import dev.velaron.fennec.place.PlaceFactory;
import dev.velaron.fennec.util.AssertUtils;
import dev.velaron.fennec.util.Objects;
import biz.dealnote.mvp.core.IPresenterFactory;

/**
 * Created by admin on 13.06.2017.
 * phoenix
 */
public class CommunityBlacklistFragment extends BaseMvpFragment<CommunityBlacklistPresenter, ICommunityBlacklistView>
        implements ICommunityBlacklistView, CommunityBannedAdapter.ActionListener {

    private static final int REQUEST_SELECT_PROFILES = 17;

    public static CommunityBlacklistFragment newInstance(int accountId, int groupdId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.GROUP_ID, groupdId);
        CommunityBlacklistFragment fragment = new CommunityBlacklistFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CommunityBannedAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_community_blacklist, container, false);

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> getPresenter().fireRefresh());

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                getPresenter().fireScrollToBottom();
            }
        });

        mAdapter = new CommunityBannedAdapter(requireActivity(), Collections.emptyList());
        mAdapter.setActionListener(this);

        recyclerView.setAdapter(mAdapter);

        root.findViewById(R.id.button_add).setOnClickListener(v -> getPresenter().fireAddClick());
        return root;
    }

    @Override
    public IPresenterFactory<CommunityBlacklistPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new CommunityBlacklistPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                requireArguments().getInt(Extra.GROUP_ID),
                saveInstanceState
        );
    }

    @Override
    public void displayRefreshing(boolean loadingNow) {
        if(Objects.nonNull(mSwipeRefreshLayout)){
            mSwipeRefreshLayout.setRefreshing(loadingNow);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if(Objects.nonNull(mAdapter)){
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void diplayData(List<Banned> data) {
        if(Objects.nonNull(mAdapter)){
            mAdapter.setData(data);
        }
    }

    @Override
    public void notifyItemRemoved(int index) {
        if(Objects.nonNull(mAdapter)){
            mAdapter.notifyItemRemoved(index);
        }
    }

    @Override
    public void openBanEditor(int accountId, int groupId, Banned banned) {
        PlaceFactory.getCommunityBanEditPlace(accountId, groupId, banned).tryOpenWith(requireActivity());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_SELECT_PROFILES && resultCode == Activity.RESULT_OK){
            ArrayList<User> users = data.getParcelableArrayListExtra(Extra.USERS);
            AssertUtils.requireNonNull(users);
            postPrenseterReceive(presenter -> presenter.fireAddToBanUsersSelected(users));
        }
    }

    @Override
    public void startSelectProfilesActivity(int accountId, int groupId) {
        PeopleSearchCriteria criteria = new PeopleSearchCriteria("")
                .setGroupId(groupId);

        SelectProfileCriteria c = new SelectProfileCriteria();

        Place place = PlaceFactory.getSingleTabSearchPlace(accountId, SearchContentType.PEOPLE, criteria);
        Intent intent = SelectProfilesActivity.createIntent(requireActivity(), place, c);
        startActivityForResult(intent, REQUEST_SELECT_PROFILES);
    }

    @Override
    public void addUsersToBan(int accountId, int groupId, ArrayList<User> users) {
        PlaceFactory.getCommunityAddBanPlace(accountId, groupId, users).tryOpenWith(requireActivity());
    }

    @Override
    public void notifyItemsAdded(int position, int size) {
        if(Objects.nonNull(mAdapter)){
            mAdapter.notifyItemRangeInserted(position, size);
        }
    }

    @Override
    public void onBannedClick(Banned banned) {
        getPresenter().fireBannedClick(banned);
    }

    @Override
    public void onBannedLongClick(Banned banned) {
        String[] items = {getString(R.string.delete)};
        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(banned.getBanned().getFullName())
                .setItems(items, (dialog, which) -> getPresenter().fireBannedRemoveClick(banned))
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }
}