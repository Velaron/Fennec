package dev.velaron.fennec.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dev.velaron.fennec.Extra;
import dev.velaron.fennec.R;
import dev.velaron.fennec.adapter.SelectedProfilesAdapter;
import dev.velaron.fennec.fragment.friends.FriendsTabsFragment;
import dev.velaron.fennec.model.SelectProfileCriteria;
import dev.velaron.fennec.model.User;
import dev.velaron.fennec.place.Place;
import dev.velaron.fennec.place.PlaceFactory;
import dev.velaron.fennec.settings.Settings;
import dev.velaron.fennec.util.Logger;
import dev.velaron.fennec.util.Utils;

public class SelectProfilesActivity extends MainActivity implements SelectedProfilesAdapter.ActionListener, ProfileSelectable {

    private static final String TAG = SelectProfilesActivity.class.getSimpleName();

    private SelectProfileCriteria mSelectableCriteria;

    private ArrayList<User> mSelectedUsers;
    private RecyclerView mRecyclerView;
    private SelectedProfilesAdapter mProfilesAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        this.mLayoutRes = R.layout.activity_main_with_profiles_selection;
        super.onCreate(savedInstanceState);
        super.mLastBackPressedTime = Long.MAX_VALUE - DOUBLE_BACK_PRESSED_TIMEOUT;

        this.mSelectableCriteria = getIntent().getParcelableExtra(Extra.CRITERIA);

        if (savedInstanceState != null) {
            mSelectedUsers = savedInstanceState.getParcelableArrayList(SAVE_SELECTED_USERS);
        }

        if (mSelectedUsers == null) {
            mSelectedUsers = new ArrayList<>();
        }

        RecyclerView.LayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mProfilesAdapter = new SelectedProfilesAdapter(this, mSelectedUsers);
        mProfilesAdapter.setActionListener(this);

        mRecyclerView = findViewById(R.id.recycleView);
        if (mRecyclerView == null) {
            throw new IllegalStateException("Invalid view");
        }

        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mProfilesAdapter);
    }

    private static final String SAVE_SELECTED_USERS = "save_selected_users";

    /*public static void start(Activity activity, @Nullable ArrayList<VKApiUser> users, int requestCode){
        int aid = Accounts.getCurrentUid(activity);
        Place place = PlaceFactory.getFriendsFollowersPlace(aid, aid, FriendsTabsFragment.TAB_ALL_FRIENDS, null);

        Intent intent = new Intent(activity, SelectProfilesActivity.class);
        intent.setAction(SelectProfilesActivity.ACTION_OPEN_PLACE);
        intent.putExtra(Extra.PLACE, place);
        intent.putParcelableArrayListExtra(Extra.USERS, users);
        activity.startActivityForResult(intent, requestCode);
    }*/

    public static Intent createIntent(Context context, @NonNull Place initialPlace, @NonNull SelectProfileCriteria criteria) {
        return new Intent(context, SelectProfilesActivity.class)
                .setAction(SelectProfilesActivity.ACTION_OPEN_PLACE)
                .putExtra(Extra.PLACE, initialPlace)
                .putExtra(Extra.CRITERIA, criteria);
    }

    public static void startFriendsSelection(@NonNull Fragment fragment, int requestCode) {
        int aid = Settings.get()
                .accounts()
                .getCurrent();

        Place place = PlaceFactory.getFriendsFollowersPlace(aid, aid, FriendsTabsFragment.TAB_ALL_FRIENDS, null);

        SelectProfileCriteria criteria = new SelectProfileCriteria().setFriendsOnly(true);

        Intent intent = new Intent(fragment.requireActivity(), SelectProfilesActivity.class);
        intent.setAction(SelectProfilesActivity.ACTION_OPEN_PLACE);
        intent.putExtra(Extra.PLACE, place);
        intent.putExtra(Extra.CRITERIA, criteria);
        fragment.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(SAVE_SELECTED_USERS, mSelectedUsers);
    }

    @Override
    public void onClick(int adapterPosition, User user) {
        mSelectedUsers.remove(mProfilesAdapter.toDataPosition(adapterPosition));
        mProfilesAdapter.notifyItemRemoved(adapterPosition);
        mProfilesAdapter.notifyHeaderChange();
    }

    @Override
    public void onCheckClick() {
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(Extra.USERS, mSelectedUsers);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void select(User user) {
        Logger.d(TAG, "Select, user: " + user);

        int index = Utils.indexOf(mSelectedUsers, user.getId());

        if (index != -1) {
            mSelectedUsers.remove(index);
            mProfilesAdapter.notifyItemRemoved(mProfilesAdapter.toAdapterPosition(index));
        }

        mSelectedUsers.add(0, user);
        mProfilesAdapter.notifyItemInserted(mProfilesAdapter.toAdapterPosition(0));
        mProfilesAdapter.notifyHeaderChange();
        mRecyclerView.smoothScrollToPosition(0);
    }

    @Override
    public SelectProfileCriteria getAcceptableCriteria() {
        return mSelectableCriteria;
    }
}
