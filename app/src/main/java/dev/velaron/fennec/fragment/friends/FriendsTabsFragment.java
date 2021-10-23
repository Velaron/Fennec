package dev.velaron.fennec.fragment.friends;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import dev.velaron.fennec.Extra;
import dev.velaron.fennec.R;
import dev.velaron.fennec.activity.ActivityFeatures;
import dev.velaron.fennec.activity.ActivityUtils;
import dev.velaron.fennec.fragment.AdditionalNavigationFragment;
import dev.velaron.fennec.fragment.base.BaseMvpFragment;
import dev.velaron.fennec.listener.OnSectionResumeCallback;
import dev.velaron.fennec.model.FriendsCounters;
import dev.velaron.fennec.mvp.presenter.FriendsTabsPresenter;
import dev.velaron.fennec.mvp.view.IFriendsTabsView;
import dev.velaron.fennec.place.Place;
import dev.velaron.fennec.settings.Settings;
import dev.velaron.fennec.mvp.core.IPresenterFactory;

public class FriendsTabsFragment extends BaseMvpFragment<FriendsTabsPresenter, IFriendsTabsView> implements IFriendsTabsView {

    public static final int TAB_ALL_FRIENDS = 0;
    public static final int TAB_ONLINE = 1;
    public static final int TAB_FOLLOWERS = 2;
    public static final int TAB_MUTUAL = 3;

    public static Bundle buildArgs(int accountId, int userId, int tab, FriendsCounters counters) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.USER_ID, userId);
        args.putInt(Extra.TAB, tab);
        args.putParcelable(Extra.COUNTERS, counters);
        return args;
    }

    public static FriendsTabsFragment newInstance(Bundle args) {
        FriendsTabsFragment friendsFragment = new FriendsTabsFragment();
        friendsFragment.setArguments(args);
        return friendsFragment;
    }

    public static FriendsTabsFragment newInstance(int accountId, int userId, int tab, FriendsCounters counters) {
        return newInstance(buildArgs(accountId, userId, tab, counters));
    }

    private CharSequence[] titles;
    private Adapter adapter;
    private TabLayout tabLayout;

    private ViewPager viewPager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        titles = new CharSequence[4];
        titles[TAB_ALL_FRIENDS] = getString(R.string.all_friends);
        titles[TAB_ONLINE] = getString(R.string.online);
        titles[TAB_FOLLOWERS] = getString(R.string.counter_followers);
        titles[TAB_MUTUAL] = getString(R.string.mutual_friends);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        View root = inflater.inflate(R.layout.fragment_friends_tabs, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        viewPager = root.findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(1);

        tabLayout = root.findViewById(R.id.tablayout);
        return root;
    }

    private void setupTabCounterView(int position, int count) {
        try {
            String targetTitle = titles[position] + (count > 0 ? " " + count : "");
            adapter.setPageTitle(position, targetTitle);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Settings.get().ui().notifyPlaceResumed(Place.FRIENDS_AND_FOLLOWERS);

        ActivityUtils.setToolbarTitle(this, R.string.friends);

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @Override
    public IPresenterFactory<FriendsTabsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new FriendsTabsPresenter(
                getArguments().getInt(Extra.ACCOUNT_ID),
                getArguments().getInt(Extra.USER_ID),
                getArguments().getParcelable(Extra.COUNTERS),
                saveInstanceState
        );
    }

    @Override
    public void displayConters(FriendsCounters counters) {
        setupTabCounterView(TAB_ALL_FRIENDS, counters.getAll());
        setupTabCounterView(TAB_ONLINE, counters.getOnline());
        setupTabCounterView(TAB_FOLLOWERS, counters.getFollowers());
        setupTabCounterView(TAB_MUTUAL, counters.getMutual());

        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void configTabs(int accountId, int userId, boolean showMutualTab) {
        adapter = new Adapter(requireActivity(), getChildFragmentManager(), accountId, userId, showMutualTab);

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        if (getArguments().containsKey(Extra.TAB)) {
            int tab = getArguments().getInt(Extra.TAB);
            getArguments().remove(Extra.TAB);
            viewPager.setCurrentItem(tab);
        }
    }

    @Override
    public void displayUserNameAtToolbar(String userName) {
        ActivityUtils.setToolbarSubtitle(this, userName);
    }

    @Override
    public void setDrawerFriendsSectionSelected(boolean selected) {
        if (requireActivity() instanceof OnSectionResumeCallback) {
            if (selected) {
                ((OnSectionResumeCallback) requireActivity()).onSectionResume(AdditionalNavigationFragment.SECTION_ITEM_FRIENDS);
            } else {
                ((OnSectionResumeCallback) requireActivity()).onClearSelection();
            }
        }
    }

    private static class Adapter extends FragmentPagerAdapter {

        private final int accountId;
        private final int userId;
        private final boolean showMutualTab;

        private final List<String> mFragmentTitles = new ArrayList<>(4);

        public Adapter(Context context, FragmentManager fm, int accountId, int userId, boolean showMutualTab) {
            super(fm);
            this.accountId = accountId;
            this.userId = userId;
            this.showMutualTab = showMutualTab;

            mFragmentTitles.add(TAB_ALL_FRIENDS, context.getString(R.string.all_friends));
            mFragmentTitles.add(TAB_ONLINE, context.getString(R.string.online));
            mFragmentTitles.add(TAB_FOLLOWERS, context.getString(R.string.counter_followers));

            if (showMutualTab) {
                mFragmentTitles.add(TAB_MUTUAL, context.getString(R.string.mutual_friends));
            }
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case TAB_ALL_FRIENDS:
                    return AllFriendsFragment.newInstance(accountId, userId);
                case TAB_ONLINE:
                    return OnlineFriendsFragment.newInstance(accountId, userId);
                case TAB_FOLLOWERS:
                    return FollowersFragment.newInstance(accountId, userId);
                case TAB_MUTUAL:
                    return MutualFriendsFragment.newInstance(accountId, userId);
            }

            throw new UnsupportedOperationException();
        }

        @Override
        public int getCount() {
            return showMutualTab ? 4 : 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }

        void setPageTitle(int position, String title) {
            mFragmentTitles.set(position, title);
        }
    }
}