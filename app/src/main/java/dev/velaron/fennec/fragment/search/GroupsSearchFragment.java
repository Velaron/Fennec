package dev.velaron.fennec.fragment.search;

import android.os.Bundle;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dev.velaron.fennec.Extra;
import dev.velaron.fennec.adapter.PeopleAdapter;
import dev.velaron.fennec.fragment.search.criteria.GroupSearchCriteria;
import dev.velaron.fennec.model.Community;
import dev.velaron.fennec.model.Owner;
import dev.velaron.fennec.mvp.presenter.search.CommunitiesSearchPresenter;
import dev.velaron.fennec.mvp.view.search.ICommunitiesSearchView;
import dev.velaron.fennec.place.PlaceFactory;
import dev.velaron.fennec.mvp.core.IPresenterFactory;

public class GroupsSearchFragment extends AbsSearchFragment<CommunitiesSearchPresenter, ICommunitiesSearchView, Community>
        implements ICommunitiesSearchView, PeopleAdapter.ClickListener {

    public static GroupsSearchFragment newInstance(int accountId, @Nullable GroupSearchCriteria initialCriteria) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putParcelable(Extra.CRITERIA, initialCriteria);
        GroupsSearchFragment fragment = new GroupsSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    void setAdapterData(RecyclerView.Adapter adapter, List<Community> data) {
        ((PeopleAdapter) adapter).setItems(data);
    }

    @Override
    RecyclerView.Adapter createAdapter(List<Community> data) {
        PeopleAdapter adapter = new PeopleAdapter(requireActivity(), data);
        adapter.setClickListener(this);
        return adapter;
    }

    @Override
    protected RecyclerView.LayoutManager createLayoutManager() {
        return new LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false);
    }

    @Override
    public IPresenterFactory<CommunitiesSearchPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new CommunitiesSearchPresenter(
                getArguments().getInt(Extra.ACCOUNT_ID),
                getArguments().getParcelable(Extra.CRITERIA),
                saveInstanceState
        );
    }

    @Override
    public void onOwnerClick(Owner owner) {
        getPresenter().fireCommunityClick((Community) owner);
    }

    @Override
    public void openCommunityWall(int accountId, Community community) {
        PlaceFactory.getOwnerWallPlace(accountId, community).tryOpenWith(requireActivity());
    }
}