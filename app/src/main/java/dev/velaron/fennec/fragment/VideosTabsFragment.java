package dev.velaron.fennec.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
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
import dev.velaron.fennec.db.OwnerHelper;
import dev.velaron.fennec.fragment.base.BaseFragment;
import dev.velaron.fennec.fragment.search.SearchContentType;
import dev.velaron.fennec.fragment.search.criteria.VideoSearchCriteria;
import dev.velaron.fennec.listener.OnSectionResumeCallback;
import dev.velaron.fennec.place.Place;
import dev.velaron.fennec.place.PlaceFactory;
import dev.velaron.fennec.settings.Settings;

public class VideosTabsFragment extends BaseFragment {

    private int accountId;
    private int ownerId;
    private String action;

    public static Bundle buildArgs(int accountId, int ownerId, String action) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.OWNER_ID, ownerId);
        args.putString(Extra.ACTION, action);
        return args;
    }

    public static VideosTabsFragment newInstance(int accountId, int ownerId, String action) {
        return newInstance(buildArgs(accountId, ownerId, action));
    }

    public static VideosTabsFragment newInstance(Bundle args) {
        VideosTabsFragment fragment = new VideosTabsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        accountId = requireArguments().getInt(Extra.ACCOUNT_ID);
        ownerId = requireArguments().getInt(Extra.OWNER_ID);
        action = requireArguments().getString(Extra.ACTION);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_videos_tabs, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        ViewPager viewPager = view.findViewById(R.id.fragment_videos_pager);
        setupViewPager(viewPager);

        TabLayout tabLayout = view.findViewById(R.id.fragment_videos_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    public int getAccountId() {
        return accountId;
    }

    private void setupViewPager(ViewPager viewPager) {
        VideosFragment fragment = VideosFragment.newInstance(getAccountId(), ownerId, 0, action, null);
        fragment.requireArguments().putBoolean(VideosFragment.EXTRA_IN_TABS_CONTAINER, true);

        Adapter adapter = new Adapter(getChildFragmentManager());
        adapter.addFragment(fragment, getString(R.string.videos_my));
        adapter.addFragment(VideoAlbumsFragment.newInstance(getAccountId(), ownerId, action), getString(R.string.videos_albums));
        viewPager.setAdapter(adapter);
    }

    private boolean isMy() {
        return getAccountId() == ownerId;
    }

    @Override
    public void onResume() {
        super.onResume();
        Settings.get().ui().notifyPlaceResumed(Place.VIDEOS);

        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle(R.string.videos);
            actionBar.setSubtitle(isMy() ? null : OwnerHelper.loadOwnerFullName(requireActivity(), getAccountId(), ownerId));
        }

        if (requireActivity() instanceof OnSectionResumeCallback) {
            if (isMy()) {
                ((OnSectionResumeCallback) requireActivity()).onSectionResume(AdditionalNavigationFragment.SECTION_ITEM_VIDEOS);
            } else {
                ((OnSectionResumeCallback) requireActivity()).onClearSelection();
            }
        }

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    static class Adapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                VideoSearchCriteria criteria = new VideoSearchCriteria("");
                PlaceFactory.getSingleTabSearchPlace(getAccountId(), SearchContentType.VIDEOS, criteria).tryOpenWith(requireActivity());
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_video_main, menu);
    }
}