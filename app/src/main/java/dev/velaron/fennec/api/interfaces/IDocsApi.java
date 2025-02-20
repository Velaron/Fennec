package dev.velaron.fennec.api.interfaces;

import androidx.annotation.CheckResult;

import java.util.Collection;
import java.util.List;

import dev.velaron.fennec.api.model.IdPair;
import dev.velaron.fennec.api.model.Items;
import dev.velaron.fennec.api.model.VkApiDoc;
import dev.velaron.fennec.api.model.server.VkApiDocsUploadServer;
import io.reactivex.Single;

/**
 * Created by admin on 02.01.2017.
 * phoenix
 */
public interface IDocsApi {

    @CheckResult
    Single<Boolean> delete(Integer ownerId, int docId);

    @CheckResult
    Single<Integer> add(int ownerId, int docId, String accessKey);

    @CheckResult
    Single<List<VkApiDoc>> getById(Collection<IdPair> pairs);

    @CheckResult
    Single<Items<VkApiDoc>> search(String query, Integer count, Integer offset);

    @CheckResult
    Single<List<VkApiDoc>> save(String file, String title, String tags);

    @CheckResult
    Single<VkApiDocsUploadServer> getUploadServer(Integer groupId, String type);

    @CheckResult
    Single<Items<VkApiDoc>> get(Integer ownerId, Integer count, Integer offset, Integer type);
}
