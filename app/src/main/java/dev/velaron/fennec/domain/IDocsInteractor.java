package dev.velaron.fennec.domain;

import java.util.List;

import dev.velaron.fennec.fragment.search.criteria.DocumentSearchCriteria;
import dev.velaron.fennec.model.Document;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by Ruslan Kolbasa on 17.05.2017.
 * phoenix
 */
public interface IDocsInteractor {
    Single<List<Document>> request(int accountId, int ownerId, int filter);

    Single<List<Document>> getCacheData(int accountId, int ownerId, int filter);

    Single<Integer> add(int accountId, int docId, int ownerId, String accessKey);

    Single<Document> findById(int accountId, int ownerId, int docId);

    Single<List<Document>> search(int accountId, DocumentSearchCriteria criteria, int count, int offset);

    Completable delete(int accountId, int docId, int ownerId);
}