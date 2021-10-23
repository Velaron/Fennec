package dev.velaron.fennec.mvp.presenter.search;

import static dev.velaron.fennec.util.Utils.nonEmpty;

import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.List;

import dev.velaron.fennec.domain.ICommunitiesInteractor;
import dev.velaron.fennec.domain.InteractorFactory;
import dev.velaron.fennec.fragment.search.criteria.GroupSearchCriteria;
import dev.velaron.fennec.fragment.search.nextfrom.IntNextFrom;
import dev.velaron.fennec.fragment.search.options.SpinnerOption;
import dev.velaron.fennec.model.Community;
import dev.velaron.fennec.mvp.view.search.ICommunitiesSearchView;
import dev.velaron.fennec.util.Pair;
import io.reactivex.Single;

/**
 * Created by admin on 19.09.2017.
 * phoenix
 */
public class CommunitiesSearchPresenter extends AbsSearchPresenter<ICommunitiesSearchView,
        GroupSearchCriteria, Community, IntNextFrom> {

    private final ICommunitiesInteractor communitiesInteractor;

    public CommunitiesSearchPresenter(int accountId, @Nullable GroupSearchCriteria criteria, @Nullable Bundle savedInstanceState) {
        super(accountId, criteria, savedInstanceState);
        this.communitiesInteractor = InteractorFactory.createCommunitiesInteractor();
    }

    @Override
    IntNextFrom getInitialNextFrom() {
        return new IntNextFrom(0);
    }

    @Override
    boolean isAtLast(IntNextFrom startFrom) {
        return startFrom.getOffset() == 0;
    }

    @Override
    Single<Pair<List<Community>, IntNextFrom>> doSearch(int accountId, GroupSearchCriteria criteria, IntNextFrom startFrom) {
        final String type = extractTypeFromCriteria(criteria);

        final Integer countryId = criteria.extractDatabaseEntryValueId(GroupSearchCriteria.KEY_COUNTRY);
        final Integer cityId = criteria.extractDatabaseEntryValueId(GroupSearchCriteria.KEY_CITY);
        final Boolean future = criteria.extractBoleanValueFromOption(GroupSearchCriteria.KEY_FUTURE_ONLY);

        final SpinnerOption sortOption = criteria.findOptionByKey(GroupSearchCriteria.KEY_SORT);
        final Integer sort = (sortOption == null || sortOption.value == null) ? null : sortOption.value.id;

        final int offset = startFrom.getOffset();
        final IntNextFrom nextFrom = new IntNextFrom(offset + 50);

        return communitiesInteractor.search(accountId, criteria.getQuery(), type, countryId, cityId, future, sort, 50, offset)
                .map(communities -> Pair.Companion.create(communities, nextFrom));
    }

    private static String extractTypeFromCriteria(GroupSearchCriteria criteria) {
        SpinnerOption option = criteria.findOptionByKey(GroupSearchCriteria.KEY_TYPE);
        if (option != null && option.value != null) {
            switch (option.value.id) {
                case GroupSearchCriteria.TYPE_PAGE:
                    return "page";
                case GroupSearchCriteria.TYPE_GROUP:
                    return "group";
                case GroupSearchCriteria.TYPE_EVENT:
                    return "event";
            }
        }

        return null;
    }

    @Override
    GroupSearchCriteria instantiateEmptyCriteria() {
        return new GroupSearchCriteria("");
    }

    @Override
    boolean canSearch(GroupSearchCriteria criteria) {
        return nonEmpty(criteria.getQuery());
    }

    public void fireCommunityClick(Community community) {
        getView().openCommunityWall(getAccountId(), community);
    }
}