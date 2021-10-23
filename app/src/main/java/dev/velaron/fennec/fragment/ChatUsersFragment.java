package dev.velaron.fennec.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.velaron.fennec.Extra;
import dev.velaron.fennec.R;
import dev.velaron.fennec.activity.ActivityFeatures;
import dev.velaron.fennec.activity.ActivityUtils;
import dev.velaron.fennec.activity.SelectProfilesActivity;
import dev.velaron.fennec.adapter.ChatMembersListAdapter;
import dev.velaron.fennec.fragment.base.BaseMvpFragment;
import dev.velaron.fennec.fragment.friends.FriendsTabsFragment;
import dev.velaron.fennec.model.AppChatUser;
import dev.velaron.fennec.model.Owner;
import dev.velaron.fennec.model.SelectProfileCriteria;
import dev.velaron.fennec.model.User;
import dev.velaron.fennec.mvp.presenter.ChatMembersPresenter;
import dev.velaron.fennec.mvp.view.IChatMembersView;
import dev.velaron.fennec.place.Place;
import dev.velaron.fennec.place.PlaceFactory;
import dev.velaron.fennec.util.AssertUtils;
import dev.velaron.fennec.util.ViewUtils;
import biz.dealnote.mvp.core.IPresenterFactory;

import static dev.velaron.fennec.util.Objects.nonNull;

public class ChatUsersFragment extends BaseMvpFragment<ChatMembersPresenter, IChatMembersView>
        implements IChatMembersView, ChatMembersListAdapter.ActionListener {

    public static Bundle buildArgs(int accountId, int chatId) {
        Bundle args = new Bundle();
        args.putInt(Extra.CHAT_ID, chatId);
        args.putInt(Extra.ACCOUNT_ID, accountId);
        return args;
    }

    public static ChatUsersFragment newInstance(Bundle args) {
        ChatUsersFragment fragment = new ChatUsersFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ChatMembersListAdapter mAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_chat_users, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        mAdapter = new ChatMembersListAdapter(requireActivity(), Collections.emptyList());
        mAdapter.setActionListener(this);
        recyclerView.setAdapter(mAdapter);

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> getPresenter().fireRefresh());
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout, true);

        FloatingActionButton fabAdd = root.findViewById(R.id.fragment_chat_users_add);
        fabAdd.setOnClickListener(v -> getPresenter().fireAddUserClick());
        return root;
    }

    private static final int REQUEST_CODE_ADD_USER = 110;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_USER && resultCode == Activity.RESULT_OK && data != null) {
            ArrayList<User> users = data.getParcelableArrayListExtra(Extra.USERS);
            AssertUtils.requireNonNull(users);

            postPrenseterReceive(presenter -> presenter.fireUserSelected(users));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);

        if (actionBar != null) {
            actionBar.setTitle(R.string.chat_users);
            actionBar.setSubtitle(null);
        }

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @Override
    public void onRemoveClick(final AppChatUser user) {
        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.confirmation)
                .setMessage(getString(R.string.remove_chat_user_commit, user.getMember().getFullName()))
                .setPositiveButton(R.string.button_ok, (dialog, which) -> getPresenter().fireUserDeteleConfirmed(user))
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    @Override
    public void displayData(List<AppChatUser> users) {
        if(nonNull(mAdapter)){
            mAdapter.setData(users);
        }
    }

    @Override
    public void notifyItemRemoved(int position) {
        if(nonNull(mAdapter)){
            mAdapter.notifyItemRemoved(position);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if(nonNull(mAdapter)){
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyDataAdded(int position, int count) {
        if(nonNull(mAdapter)){
            mAdapter.notifyItemRangeInserted(position, count);
        }
    }

    @Override
    public void openUserWall(int accountId, Owner user) {
        PlaceFactory.getOwnerWallPlace(accountId, user).tryOpenWith(requireActivity());
    }

    @Override
    public void displayRefreshing(boolean refreshing) {
        if(nonNull(mSwipeRefreshLayout)){
            mSwipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    @Override
    public void startSelectUsersActivity(int accountId) {
        final Place place = PlaceFactory.getFriendsFollowersPlace(accountId, accountId, FriendsTabsFragment.TAB_ALL_FRIENDS, null);
        final SelectProfileCriteria criteria = new SelectProfileCriteria().setFriendsOnly(true);

        Intent intent = SelectProfilesActivity.createIntent(requireActivity(), place, criteria);

        startActivityForResult(intent, REQUEST_CODE_ADD_USER);
    }

    @Override
    public IPresenterFactory<ChatMembersPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new ChatMembersPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                requireArguments().getInt(Extra.CHAT_ID),
                saveInstanceState
        );
    }

    @Override
    public void onUserClick(AppChatUser user) {
        getPresenter().fireUserClick(user);
    }
}