package dev.velaron.fennec.fragment.search;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import dev.velaron.fennec.Extra;
import dev.velaron.fennec.R;
import dev.velaron.fennec.activity.ActivityFeatures;
import dev.velaron.fennec.adapter.MyFragmentStatePagerAdapter;
import dev.velaron.fennec.fragment.AdditionalNavigationFragment;
import dev.velaron.fennec.fragment.search.criteria.AudioSearchCriteria;
import dev.velaron.fennec.fragment.search.criteria.BaseSearchCriteria;
import dev.velaron.fennec.fragment.search.criteria.DocumentSearchCriteria;
import dev.velaron.fennec.fragment.search.criteria.GroupSearchCriteria;
import dev.velaron.fennec.fragment.search.criteria.MessageSeachCriteria;
import dev.velaron.fennec.fragment.search.criteria.NewsFeedCriteria;
import dev.velaron.fennec.fragment.search.criteria.PeopleSearchCriteria;
import dev.velaron.fennec.fragment.search.criteria.VideoSearchCriteria;
import dev.velaron.fennec.listener.AppStyleable;
import dev.velaron.fennec.listener.OnSectionResumeCallback;
import dev.velaron.fennec.place.Place;
import dev.velaron.fennec.settings.CurrentTheme;
import dev.velaron.fennec.settings.Settings;
import dev.velaron.fennec.util.Accounts;
import dev.velaron.fennec.util.Exestime;
import dev.velaron.fennec.util.Logger;
import dev.velaron.fennec.util.Utils;
import dev.velaron.fennec.view.MySearchView;

import static dev.velaron.fennec.util.Objects.nonNull;

public class SearchTabsFragment extends Fragment implements MySearchView.OnQueryTextListener,
        MySearchView.OnBackButtonClickListener, MySearchView.OnAdditionalButtonClickListener {

    public static final int TAB_PEOPLE = 0;
    public static final int TAB_COMMUNITIES = 1;
    public static final int TAB_NEWS = 2;
    public static final int TAB_MUSIC = 3;
    public static final int TAB_VIDEOS = 4;
    public static final int TAB_MESSAGES = 5;
    public static final int TAB_DOCUMENTS = 6;
    private static final String TAG = SearchTabsFragment.class.getSimpleName();

    public static Bundle buildArgs(int accountId, int tab, @Nullable BaseSearchCriteria criteria) {
        Bundle args = new Bundle();
        args.putInt(Extra.TAB, tab);
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putParcelable(Extra.CRITERIA, criteria);
        return args;
    }

    public static SearchTabsFragment newInstance(Bundle args) {
        SearchTabsFragment fragment = new SearchTabsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private ViewPager mViewPager;
    private Adapter mAdapter;
    private MySearchView mSearchView;
    private int mCurrentViewPagerBackgroundColor;
    private int mCurrentTab;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getChildFragmentManager().registerFragmentLifecycleCallbacks(mLifecycleCallbacks, false);

        if (savedInstanceState != null) {
            mCurrentTab = savedInstanceState.getInt(SAVE_CURRENT_TAB);
        }
    }

    @Override
    public void onDestroy() {
        getChildFragmentManager().unregisterFragmentLifecycleCallbacks(mLifecycleCallbacks);
        super.onDestroy();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_search_tabs, container, false);
        mViewPager = root.findViewById(R.id.viewpager);

        TabLayout tabLayout = root.findViewById(R.id.tablayout);

        mSearchView = root.findViewById(R.id.searchview);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setOnBackButtonClickListener(this);
        mSearchView.setOnAdditionalButtonClickListener(this);
        mSearchView.setQuery(getInitialCriteriaText(), true);

        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mCurrentTab = position;

                syncChildFragment();
                resolveViewPagerBackgroundColor();
            }
        });

        if (mViewPager.getBackground() != null && mViewPager.getBackground() instanceof ColorDrawable) {
            mCurrentViewPagerBackgroundColor = ((ColorDrawable) mViewPager.getBackground()).getColor();
        }

        resolveLeftButton();

        mAdapter = new Adapter(getChildFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(1);

        tabLayout.setupWithViewPager(mViewPager);

        if (getArguments().containsKey(Extra.TAB)) {
            mCurrentTab = getArguments().getInt(Extra.TAB);

            getArguments().remove(Extra.TAB);
            mViewPager.setCurrentItem(mCurrentTab);
        }

        resolveViewPagerBackgroundColor();
        return root;
    }

    private FragmentManager.FragmentLifecycleCallbacks mLifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
        @Override
        public void onFragmentViewCreated(FragmentManager fm, Fragment f, View v, Bundle savedInstanceState) {
            syncChildFragment(f);
        }
    };

    private void syncChildFragment() {
        Fragment fragment = mAdapter.findFragmentByPosition(mCurrentTab);
        if (nonNull(fragment)) {
            syncChildFragment(fragment);
        }
    }

    private void syncChildFragment(Fragment fragment) {
        int fragmentPosition = fragment.getArguments().getInt(Extra.POSITION);

        Logger.d(TAG, "syncChildFragment, current: " + mCurrentTab + ", fp: " + fragmentPosition + ", f: " + fragment.getClass());

        if (fragmentPosition != mCurrentTab) {
            return;
        }

        if (fragment instanceof AbsSearchFragment) {
            ((AbsSearchFragment) fragment).syncYourCriteriaWithParent();
        }
    }

    private static final String SAVE_CURRENT_TAB = "save_current_tab";

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVE_CURRENT_TAB, mCurrentTab);
    }

    private void resolveViewPagerBackgroundColor() {
        if (!isAdded() || mViewPager == null) return;

        int tagretColor = mAdapter.getRequiredBackgroundColor(mCurrentTab);
        if (mCurrentViewPagerBackgroundColor == tagretColor) {
            return;
        }

        startAnimation(tagretColor);
    }

    private void startAnimation(int targetColor) {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(),
                mCurrentViewPagerBackgroundColor, targetColor);
        colorAnimation.setDuration(250);

        colorAnimation.addUpdateListener(animator -> {
            int newValue = (int) animator.getAnimatedValue();
            mViewPager.setBackgroundColor(newValue);
            mCurrentViewPagerBackgroundColor = newValue;
        });

        colorAnimation.start();
    }

    @Nullable
    private String getInitialCriteriaText() {
        BaseSearchCriteria criteria = getArguments().getParcelable(Extra.CRITERIA);
        return criteria == null ? null : criteria.getQuery();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Logger.d(TAG, "onQueryTextSubmit, query: " + query);
        fireNewQuery(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        fireNewQuery(newText);
        return true;
    }

    private void fireNewQuery(String query) {
        long start = System.currentTimeMillis();

        Fragment fragment = mAdapter.findFragmentByPosition(mCurrentTab);

        if (fragment instanceof AbsSearchFragment) {
            ((AbsSearchFragment) fragment).fireTextQueryEdit(query);
        }

        Exestime.log("fireNewQuery", start);
    }

    @Override
    public void onBackButtonClick() {
        if (requireActivity().getSupportFragmentManager().getBackStackEntryCount() == 1
                && requireActivity() instanceof AppStyleable) {
            ((AppStyleable) requireActivity()).openMenu(true);
        } else {
            requireActivity().onBackPressed();
        }
    }

    @Override
    public void onAdditionalButtonClick() {
        Fragment fragment = mAdapter.findFragmentByPosition(mCurrentTab);

        if (fragment instanceof AbsSearchFragment) {
            AbsSearchFragment searchFragment = (AbsSearchFragment) fragment;
            searchFragment.openSearchFilter();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String action = nonNull(data) ? data.getAction() : null;

        if (AbsSearchFragment.ACTION_QUERY.equals(action)) {
            String q = data.getStringExtra(Extra.Q);
            mSearchView.setQuery(q);
            mSearchView.setSelection(Utils.safeLenghtOf(q));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Settings.get().ui().notifyPlaceResumed(Place.SEARCH);

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());

        if (requireActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) requireActivity()).onSectionResume(AdditionalNavigationFragment.SECTION_ITEM_SEARCH);
        }
    }

    private void resolveLeftButton() {
        int count = requireActivity().getSupportFragmentManager().getBackStackEntryCount();
        if (mSearchView != null) {
            mSearchView.setLeftIcon(count == 1 && requireActivity() instanceof AppStyleable ?
                    R.drawable.phoenix_round : R.drawable.arrow_left);
        }
    }

    @Override
    public void onDetach() {
        requireActivity().getSupportFragmentManager().removeOnBackStackChangedListener(backStackChangedListener);
        super.onDetach();
    }

    private FragmentManager.OnBackStackChangedListener backStackChangedListener = this::resolveLeftButton;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AppCompatActivity) {
            ((AppCompatActivity) context).getSupportFragmentManager()
                    .addOnBackStackChangedListener(backStackChangedListener);
        }
    }

    private class Adapter extends MyFragmentStatePagerAdapter {

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            int accountId = Accounts.fromArgs(getArguments());
            BaseSearchCriteria criteria = getArguments().getParcelable(Extra.CRITERIA);

            Fragment fragment;

            switch (position) {
                case TAB_PEOPLE:
                    fragment = PeopleSearchFragment.newInstance(accountId,
                            criteria instanceof PeopleSearchCriteria ? (PeopleSearchCriteria) criteria : null);
                    break;

                case TAB_COMMUNITIES:
                    fragment = GroupsSearchFragment.newInstance(accountId,
                            criteria instanceof GroupSearchCriteria ? (GroupSearchCriteria) criteria : null);
                    break;

                case TAB_MUSIC:
                    fragment = AudiosSearchFragment.newInstance(accountId,
                            criteria instanceof AudioSearchCriteria ? (AudioSearchCriteria) criteria : null);
                    break;

                case TAB_VIDEOS:
                    fragment = VideoSearchFragment.newInstance(accountId,
                            criteria instanceof VideoSearchCriteria ? (VideoSearchCriteria) criteria : null);
                    break;

                case TAB_DOCUMENTS:
                    fragment = DocsSearchFragment.newInstance(accountId,
                            criteria instanceof DocumentSearchCriteria ? (DocumentSearchCriteria) criteria : null);
                    break;

                case TAB_NEWS:
                    fragment = NewsFeedSearchFragment.newInstance(accountId,
                            criteria instanceof NewsFeedCriteria ? (NewsFeedCriteria) criteria : null);
                    break;

                case TAB_MESSAGES:
                    fragment = MessagesSearchFragment.newInstance(accountId,
                            criteria instanceof MessageSeachCriteria ? (MessageSeachCriteria) criteria : null);
                    break;

                default:
                    throw new IllegalArgumentException();
            }

            fragment.getArguments().putInt(Extra.POSITION, position);
            return fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case TAB_PEOPLE:
                    return getString(R.string.people);
                case TAB_COMMUNITIES:
                    return getString(R.string.communities);
                case TAB_MUSIC:
                    return getString(R.string.music);
                case TAB_VIDEOS:
                    return getString(R.string.videos);
                case TAB_DOCUMENTS:
                    return getString(R.string.documents);
                case TAB_NEWS:
                    return getString(R.string.feed);
                case TAB_MESSAGES:
                    return getString(R.string.messages);
            }

            throw new IllegalArgumentException();
        }

        @Override
        public int getCount() {
            return 7;
        }

        int getRequiredBackgroundColor(int position) {
            switch (position) {
                case TAB_NEWS:
                case TAB_VIDEOS:
                case TAB_MESSAGES:
                    return CurrentTheme.getColorFromAttrs(requireActivity(),
                            R.attr.messages_background_color, Color.WHITE);
                default:
                    return CurrentTheme.getColorFromAttrs(requireActivity(),
                            android.R.attr.colorBackground, Color.WHITE);
            }
        }
    }
}