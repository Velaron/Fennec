package dev.velaron.fennec.fragment.search;

import android.os.Bundle;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dev.velaron.fennec.Extra;
import dev.velaron.fennec.adapter.DocsAdapter;
import dev.velaron.fennec.fragment.search.criteria.DocumentSearchCriteria;
import dev.velaron.fennec.model.Document;
import dev.velaron.fennec.mvp.presenter.search.DocsSearchPresenter;
import dev.velaron.fennec.mvp.view.search.IDocSearchView;
import biz.dealnote.mvp.core.IPresenterFactory;

public class DocsSearchFragment extends AbsSearchFragment<DocsSearchPresenter, IDocSearchView, Document>
        implements DocsAdapter.ActionListener, IDocSearchView {

    public static DocsSearchFragment newInstance(int accountId, @Nullable DocumentSearchCriteria initialCriteria){
        Bundle args = new Bundle();
        args.putParcelable(Extra.CRITERIA, initialCriteria);
        args.putInt(Extra.ACCOUNT_ID, accountId);
        DocsSearchFragment fragment = new DocsSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    void setAdapterData(RecyclerView.Adapter adapter, List<Document> data) {
        ((DocsAdapter) adapter).setItems(data);
    }

    @Override
    RecyclerView.Adapter createAdapter(List<Document> data) {
        DocsAdapter adapter = new DocsAdapter(data);
        adapter.setActionListner(this);
        return adapter;
    }

    @Override
    protected RecyclerView.LayoutManager createLayoutManager() {
        return new LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false);
    }

    @Override
    public void onDocClick(int index, @NonNull Document doc) {
        getPresenter().fireDocClick(doc);
    }

    @Override
    public boolean onDocLongClick(int index, @NonNull Document doc) {
        return false;
    }

    @Override
    public IPresenterFactory<DocsSearchPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new DocsSearchPresenter(
                getArguments().getInt(Extra.ACCOUNT_ID),
                getArguments().getParcelable(Extra.CRITERIA),
                saveInstanceState
        );
    }
}