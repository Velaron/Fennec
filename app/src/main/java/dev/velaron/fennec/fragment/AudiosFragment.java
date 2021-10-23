package dev.velaron.fennec.fragment;

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

import java.util.Collections;
import java.util.List;

import dev.velaron.fennec.Extra;
import dev.velaron.fennec.R;
import dev.velaron.fennec.activity.ActivityFeatures;
import dev.velaron.fennec.activity.ActivityUtils;
import dev.velaron.fennec.adapter.AudioRecyclerAdapter;
import dev.velaron.fennec.adapter.horizontal.HorizontalOptionsAdapter;
import dev.velaron.fennec.fragment.base.BaseMvpFragment;
import dev.velaron.fennec.listener.EndlessRecyclerOnScrollListener;
import dev.velaron.fennec.listener.OnSectionResumeCallback;
import dev.velaron.fennec.model.Audio;
import dev.velaron.fennec.model.AudioFilter;
import dev.velaron.fennec.mvp.presenter.AudiosPresenter;
import dev.velaron.fennec.mvp.view.IAudiosView;
import dev.velaron.fennec.place.Place;
import dev.velaron.fennec.place.PlaceFactory;
import dev.velaron.fennec.settings.Settings;
import biz.dealnote.mvp.core.IPresenterFactory;

import static dev.velaron.fennec.util.Objects.nonNull;

/**
 * Audio is not supported :-(
 */
public class AudiosFragment extends BaseMvpFragment<AudiosPresenter, IAudiosView>
        implements IAudiosView, HorizontalOptionsAdapter.Listener<AudioFilter> {

    public static AudiosFragment newInstance(int accountId, int ownerId) {
        Bundle args = new Bundle();
        args.putInt(Extra.OWNER_ID, ownerId);
        args.putInt(Extra.ACCOUNT_ID, accountId);
        AudiosFragment fragment = new AudiosFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private View mBlockedRoot;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private AudioRecyclerAdapter mAudioRecyclerAdapter;
    private HorizontalOptionsAdapter<AudioFilter> mAudioFilterAdapter;

    private RecyclerView mFilterRecycler;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_music, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        mBlockedRoot = root.findViewById(R.id.blocked_root);
        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> getPresenter().fireRefresh());

        root.findViewById(R.id.button_details).setOnClickListener(v -> openPost());

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                getPresenter().fireScrollToEnd();
            }
        });

        mFilterRecycler = root.findViewById(R.id.recycler_filter);
        mFilterRecycler.setLayoutManager(new LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false));
        mAudioFilterAdapter = new HorizontalOptionsAdapter<>(Collections.emptyList());
        mAudioFilterAdapter.setListener(this);
        mFilterRecycler.setAdapter(mAudioFilterAdapter);

        mAudioRecyclerAdapter = new AudioRecyclerAdapter(requireActivity(), Collections.emptyList());
        mAudioRecyclerAdapter.setClickListener((position, audio) -> getPresenter().playAudio(requireActivity(), position));
        recyclerView.setAdapter(mAudioRecyclerAdapter);
        return root;
    }

    private void openPost() {
        PlaceFactory.getPostPreviewPlace(requireArguments().getInt(Extra.ACCOUNT_ID), 7927, -72124992).tryOpenWith(requireActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        Settings.get().ui().notifyPlaceResumed(Place.AUDIOS);

        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle(R.string.music);
            actionBar.setSubtitle(null);
        }

        if (requireActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) requireActivity()).onSectionResume(AdditionalNavigationFragment.SECTION_ITEM_AUDIOS);
        }

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @Override
    public IPresenterFactory<AudiosPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new AudiosPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                requireArguments().getInt(Extra.OWNER_ID),
                saveInstanceState
        );
    }

    @Override
    public void fillFilters(List<AudioFilter> sources) {
        if (nonNull(mAudioFilterAdapter)) {
            mAudioFilterAdapter.setItems(sources);
        }
    }

    @Override
    public void displayList(List<Audio> audios) {
        if (nonNull(mAudioRecyclerAdapter)) {
            mAudioRecyclerAdapter.setData(audios);
        }
    }

    @Override
    public void notifyListChanged() {
        if (nonNull(mAudioRecyclerAdapter)) {
            mAudioRecyclerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void displayRefreshing(boolean refresing) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.setRefreshing(refresing);
        }
    }

    @Override
    public void setBlockedScreen(boolean visible) {
        if (nonNull(mBlockedRoot)) {
            mBlockedRoot.setVisibility(visible ? View.VISIBLE : View.GONE);
        }

        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.setVisibility(visible ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void showFilters(boolean canFilter) {
        mFilterRecycler.setVisibility(canFilter ? View.VISIBLE : View.GONE);
    }

    public void notifyFilterListChanged() {
        if (nonNull(mAudioFilterAdapter)) {
            mAudioFilterAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onOptionClick(AudioFilter source) {
        getPresenter().fireFilterItemClick(source);
    }
}