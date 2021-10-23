package dev.velaron.fennec.domain;

import java.util.List;

import dev.velaron.fennec.fragment.search.criteria.NewsFeedCriteria;
import dev.velaron.fennec.model.FeedList;
import dev.velaron.fennec.model.News;
import dev.velaron.fennec.model.Post;
import dev.velaron.fennec.util.Pair;
import io.reactivex.Single;

/**
 * Created by Ruslan Kolbasa on 06.09.2017.
 * phoenix
 */
public interface IFeedInteractor {
    Single<List<News>> getCachedFeed(int accountId);
    Single<Pair<List<News>, String>> getActualFeed(int accountId, int count, String nextFrom, String filters, Integer maxPhotos, String sourceIds);
    Single<Pair<List<Post>, String>> search(int accountId, NewsFeedCriteria criteria, int count, String startFrom);

    Single<List<FeedList>> getCachedFeedLists(int accountId);
    Single<List<FeedList>> getActualFeedLists(int accountId);
}