package dev.velaron.fennec.fragment.search;

import android.os.Bundle;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dev.velaron.fennec.Extra;
import dev.velaron.fennec.adapter.DialogPreviewAdapter;
import dev.velaron.fennec.fragment.search.criteria.DialogsSearchCriteria;
import dev.velaron.fennec.mvp.presenter.search.DialogsSearchPresenter;
import dev.velaron.fennec.mvp.view.search.IDialogsSearchView;
import dev.velaron.fennec.mvp.core.IPresenterFactory;

/**
 * Created by admin on 02.05.2017.
 * phoenix
 */
public class DialogsSearchFragment extends AbsSearchFragment<DialogsSearchPresenter, IDialogsSearchView, Object>
        implements IDialogsSearchView, DialogPreviewAdapter.ActionListener {

    public static DialogsSearchFragment newInstance(int accountId, DialogsSearchCriteria criteria) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putParcelable(Extra.CRITERIA, criteria);
        DialogsSearchFragment fragment = new DialogsSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public IPresenterFactory<DialogsSearchPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int accountId = getArguments().getInt(Extra.ACCOUNT_ID);
            DialogsSearchCriteria criteria = getArguments().getParcelable(Extra.CRITERIA);
            return new DialogsSearchPresenter(accountId, criteria, saveInstanceState);
        };
    }

    @Override
    void setAdapterData(RecyclerView.Adapter adapter, List<Object> data) {
        ((DialogPreviewAdapter) adapter).setData(data);
    }

    @Override
    RecyclerView.Adapter createAdapter(List<Object> data) {
        return new DialogPreviewAdapter(requireActivity(), data, this);
    }

    @Override
    RecyclerView.LayoutManager createLayoutManager() {
        return new LinearLayoutManager(requireActivity());
    }

    @Override
    public void onEntryClick(Object o) {
        getPresenter().fireEntryClick(o);
    }
}