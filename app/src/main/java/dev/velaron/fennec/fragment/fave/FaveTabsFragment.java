package dev.velaron.fennec.fragment.fave;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
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
import dev.velaron.fennec.fragment.AdditionalNavigationFragment;
import dev.velaron.fennec.fragment.base.BaseFragment;
import dev.velaron.fennec.link.types.FaveLink;
import dev.velaron.fennec.listener.OnSectionResumeCallback;
import dev.velaron.fennec.place.Place;
import dev.velaron.fennec.settings.Settings;

public class FaveTabsFragment extends BaseFragment {

    public static final int TAB_UNKNOWN = -1;
    public static final int TAB_PHOTOS = 0;
    public static final int TAB_VIDEOS = 1;
    public static final int TAB_POSTS = 2;
    public static final int TAB_PAGES = 3;
    public static final int TAB_LINKS = 4;

    public static Bundle buildArgs(int accountId, int tab){
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.TAB, tab);
        return args;
    }

    private int mAccountId;

    public static FaveTabsFragment newInstance(int accountId, int tab){
        return newInstance(buildArgs(accountId, tab));
    }

    public static FaveTabsFragment newInstance(Bundle args){
        FaveTabsFragment faveTabsFragment = new FaveTabsFragment();
        faveTabsFragment.setArguments(args);
        return faveTabsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mAccountId = getArguments().getInt(Extra.ACCOUNT_ID);
    }

    public int getAccountId() {
        return mAccountId;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_fave_tabs, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        ViewPager viewPager = view.findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(1);
        setupViewPager(viewPager);

        TabLayout tabLayout = view.findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);

        if (getArguments().containsKey(Extra.TAB)) {
            int tab = getArguments().getInt(Extra.TAB);
            getArguments().remove(Extra.TAB);
            viewPager.setCurrentItem(tab);
        }
    }

    public static int getTabByLinkSection(String linkSection) {
        if (TextUtils.isEmpty(linkSection)) {
            return TAB_PHOTOS;
        }

        switch (linkSection) {
            case FaveLink.SECTION_PHOTOS:
                return TAB_PHOTOS;
            case FaveLink.SECTION_VIDEOS:
                return TAB_VIDEOS;
            case FaveLink.SECTION_POSTS:
                return TAB_POSTS;
            case FaveLink.SECTION_PAGES:
                return TAB_PAGES;
            case FaveLink.SECTION_LINKS:
                return TAB_LINKS;
            default:
                return TAB_UNKNOWN;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Settings.get().ui().notifyPlaceResumed(Place.BOOKMARKS);

        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);

        if (actionBar != null) {
            actionBar.setTitle(R.string.bookmarks);
            actionBar.setSubtitle(null);
        }

        if (requireActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) requireActivity()).onSectionResume(AdditionalNavigationFragment.SECTION_ITEM_BOOKMARKS);
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

        public void addFragment(Fragment fragment, String title) {
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

    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getChildFragmentManager());
        adapter.addFragment(FavePhotosFragment.newInstance(getAccountId()), getString(R.string.photos));
        adapter.addFragment(FaveVideosFragment.newInstance(getAccountId()), getString(R.string.videos));
        adapter.addFragment(FavePostsFragment.newInstance(getAccountId()), getString(R.string.posts));
        adapter.addFragment(FavePagesFragment.newInstance(getAccountId()), getString(R.string.page));
        adapter.addFragment(FaveLinksFragment.newInstance(getAccountId()), getString(R.string.links));
        viewPager.setAdapter(adapter);
    }
}