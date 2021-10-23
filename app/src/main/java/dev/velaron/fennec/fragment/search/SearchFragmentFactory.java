package dev.velaron.fennec.fragment.search;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import dev.velaron.fennec.fragment.search.criteria.BaseSearchCriteria;
import dev.velaron.fennec.fragment.search.criteria.DialogsSearchCriteria;
import dev.velaron.fennec.fragment.search.criteria.DocumentSearchCriteria;
import dev.velaron.fennec.fragment.search.criteria.GroupSearchCriteria;
import dev.velaron.fennec.fragment.search.criteria.MessageSeachCriteria;
import dev.velaron.fennec.fragment.search.criteria.NewsFeedCriteria;
import dev.velaron.fennec.fragment.search.criteria.PeopleSearchCriteria;
import dev.velaron.fennec.fragment.search.criteria.VideoSearchCriteria;
import dev.velaron.fennec.fragment.search.criteria.WallSearchCriteria;

/**
 * Created by admin on 01.05.2017.
 * phoenix
 */
public class SearchFragmentFactory {

    public static Fragment create(@SearchContentType int type, int accountId, @Nullable BaseSearchCriteria criteria) {
        switch (type) {
            case SearchContentType.PEOPLE:
                return PeopleSearchFragment.newInstance(accountId,
                        criteria instanceof PeopleSearchCriteria ? (PeopleSearchCriteria) criteria : null);

            case SearchContentType.COMMUNITIES:
                return GroupsSearchFragment.newInstance(accountId,
                        criteria instanceof GroupSearchCriteria ? (GroupSearchCriteria) criteria : null);

            case SearchContentType.VIDEOS:
                return VideoSearchFragment.newInstance(accountId,
                        criteria instanceof VideoSearchCriteria ? (VideoSearchCriteria) criteria : null);

            case SearchContentType.DOCUMENTS:
                return DocsSearchFragment.newInstance(accountId,
                        criteria instanceof DocumentSearchCriteria ? (DocumentSearchCriteria) criteria : null);

            case SearchContentType.NEWS:
                return NewsFeedSearchFragment.newInstance(accountId,
                        criteria instanceof NewsFeedCriteria ? (NewsFeedCriteria) criteria : null);

            case SearchContentType.MESSAGES:
                return MessagesSearchFragment.newInstance(accountId,
                        criteria instanceof MessageSeachCriteria ? (MessageSeachCriteria) criteria : null);

            case SearchContentType.WALL:
                return WallSearchFragment.newInstance(accountId,
                        criteria instanceof WallSearchCriteria ? (WallSearchCriteria) criteria : null);

                case SearchContentType.DIALOGS:
                    return DialogsSearchFragment.newInstance(accountId,
                            criteria instanceof DialogsSearchCriteria ? (DialogsSearchCriteria) criteria : null);

            default:
                throw new UnsupportedOperationException();
        }
    }

}
