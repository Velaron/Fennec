package dev.velaron.fennec.domain;

import java.util.List;

import dev.velaron.fennec.model.NewsfeedComment;
import dev.velaron.fennec.util.Pair;
import io.reactivex.Single;

/**
 * Created by admin on 08.05.2017.
 * phoenix
 */
public interface INewsfeedInteractor {

    Single<Pair<List<NewsfeedComment>, String>> getNewsfeedComments(int accountId, int count, String startFrom, String filter);

}
