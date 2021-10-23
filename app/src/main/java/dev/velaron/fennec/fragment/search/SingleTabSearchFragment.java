package dev.velaron.fennec.fragment.search;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import dev.velaron.fennec.Extra;
import dev.velaron.fennec.R;
import dev.velaron.fennec.activity.ActivityFeatures;
import dev.velaron.fennec.fragment.search.criteria.BaseSearchCriteria;
import dev.velaron.fennec.listener.OnSectionResumeCallback;
import dev.velaron.fennec.settings.CurrentTheme;
import dev.velaron.fennec.util.Objects;
import dev.velaron.fennec.view.MySearchView;

/**
 * Created by admin on 01.05.2017.
 * phoenix
 */
public class SingleTabSearchFragment extends Fragment implements MySearchView.OnQueryTextListener, MySearchView.OnAdditionalButtonClickListener {

    public static Bundle buildArgs(int accountId, @SearchContentType int contentType, @Nullable BaseSearchCriteria criteria) {
        Bundle args = new Bundle();
        args.putInt(Extra.TYPE, contentType);
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putParcelable(Extra.CRITERIA, criteria);
        return args;
    }

    public static SingleTabSearchFragment newInstance(Bundle args) {
        SingleTabSearchFragment fragment = new SingleTabSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @SearchContentType
    private int mContentType;

    private int mAccountId;

    private BaseSearchCriteria mInitialCriteria;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContentType = getArguments().getInt(Extra.TYPE);
        this.mAccountId = getArguments().getInt(Extra.ACCOUNT_ID);
        this.mInitialCriteria = getArguments().getParcelable(Extra.CRITERIA);

        getChildFragmentManager().registerFragmentLifecycleCallbacks(mFragmentLifecycleCallbacks, false);

        if(Objects.nonNull(savedInstanceState)){
            this.attachedChild = savedInstanceState.getBoolean("attachedChild");
        }
    }

    @Override
    public void onDestroy() {
        getChildFragmentManager().unregisterFragmentLifecycleCallbacks(mFragmentLifecycleCallbacks);
        super.onDestroy();
    }

    private boolean attachedChild;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_search_single, container, false);

        MySearchView searchView = root.findViewById(R.id.searchview);
        searchView.setOnQueryTextListener(this);
        searchView.setOnBackButtonClickListener(() -> requireActivity().onBackPressed());
        searchView.setOnAdditionalButtonClickListener(this);
        searchView.setQuery(getInitialCriteriaText(), true);

        if(!attachedChild){
            attachChildFragment();
            this.attachedChild = true;
        }

        root.setBackgroundColor(getRequiredBackgroundColor(mContentType));
        return root;
    }

    private FragmentManager.FragmentLifecycleCallbacks mFragmentLifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
        @Override
        public void onFragmentViewCreated(FragmentManager fm, Fragment f, View v, Bundle savedInstanceState) {
            syncChildFragment();
        }
    };

    private void syncChildFragment(){
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.child_container);

        if(fragment instanceof AbsSearchFragment){
            ((AbsSearchFragment) fragment).syncYourCriteriaWithParent();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("attachedChild", attachedChild);
    }

    private void fireNewQuery(String query) {
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.child_container);

        // MVP
        if(fragment instanceof AbsSearchFragment){
            ((AbsSearchFragment) fragment).fireTextQueryEdit(query);
        }
    }

    private void attachChildFragment(){
        Fragment fragment = SearchFragmentFactory.create(mContentType, mAccountId, mInitialCriteria);
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.child_container, fragment)
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());

        if (requireActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) requireActivity()).onClearSelection();
        }
    }

    private String getInitialCriteriaText() {
        return Objects.isNull(mInitialCriteria) ? "" : mInitialCriteria.getQuery();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        fireNewQuery(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        fireNewQuery(newText);
        return false;
    }

    private int getRequiredBackgroundColor(int type) {
        switch (type) {
            case SearchContentType.NEWS:
            case SearchContentType.WALL:
            case SearchContentType.VIDEOS:
            case SearchContentType.MESSAGES:
                return CurrentTheme.getColorFromAttrs(requireActivity(),
                        R.attr.messages_background_color, Color.WHITE);
            default:
                return CurrentTheme.getColorFromAttrs(requireActivity(),
                        android.R.attr.colorBackground, Color.WHITE);
        }
    }

    @Override
    public void onAdditionalButtonClick() {
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.child_container);
        if(fragment instanceof AbsSearchFragment){
            ((AbsSearchFragment) fragment).openSearchFilter();
        }
    }
}