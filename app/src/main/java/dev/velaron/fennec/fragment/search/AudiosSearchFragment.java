package dev.velaron.fennec.fragment.search;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import dev.velaron.fennec.Extra;
import dev.velaron.fennec.adapter.AudioRecyclerAdapter;
import dev.velaron.fennec.fragment.search.criteria.AudioSearchCriteria;
import dev.velaron.fennec.model.Audio;
import dev.velaron.fennec.mvp.presenter.search.AudiosSearchPresenter;
import dev.velaron.fennec.mvp.view.search.IAudioSearchView;
import dev.velaron.fennec.place.PlaceFactory;
import biz.dealnote.mvp.core.IPresenterFactory;


public class AudiosSearchFragment extends AbsSearchFragment<AudiosSearchPresenter, IAudioSearchView, Audio>
        implements IAudioSearchView {

    public static AudiosSearchFragment newInstance(int accountId, AudioSearchCriteria criteria) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putParcelable(Extra.CRITERIA, criteria);
        AudiosSearchFragment fragment = new AudiosSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    void setAdapterData(RecyclerView.Adapter adapter, List<Audio> data) {
        ((AudioRecyclerAdapter) adapter).setData(data);
    }

    @Override
    RecyclerView.Adapter createAdapter(List<Audio> data) {
        AudioRecyclerAdapter adapter = new AudioRecyclerAdapter(requireActivity(), Collections.emptyList());
        adapter.setClickListener((position, audio) -> getPresenter().playAudio(requireActivity(), position));
        return adapter;
    }

    @Override
    RecyclerView.LayoutManager createLayoutManager() {
        return new LinearLayoutManager(requireActivity());
    }

    private void openPost() {
        PlaceFactory.getPostPreviewPlace(requireArguments().getInt(Extra.ACCOUNT_ID), 7927, -72124992).tryOpenWith(requireActivity());
    }

    @Override
    public IPresenterFactory<AudiosSearchPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new AudiosSearchPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                requireArguments().getParcelable(Extra.CRITERIA),
                saveInstanceState
        );
    }
}