package dev.velaron.fennec.api.impl;

import java.util.Collection;
import java.util.List;

import dev.velaron.fennec.api.IServiceProvider;
import dev.velaron.fennec.api.interfaces.IDocsApi;
import dev.velaron.fennec.api.model.IdPair;
import dev.velaron.fennec.api.model.Items;
import dev.velaron.fennec.api.model.VkApiDoc;
import dev.velaron.fennec.api.model.server.VkApiDocsUploadServer;
import dev.velaron.fennec.api.services.IDocsService;
import io.reactivex.Single;

/**
 * Created by admin on 02.01.2017.
 * phoenix
 */
class DocsApi extends AbsApi implements IDocsApi {

    DocsApi(int accountId, IServiceProvider provider) {
        super(accountId, provider);
    }

    @Override
    public Single<Boolean> delete(Integer ownerId, int docId) {
        return provideService(IDocsService.class)
                .flatMap(service -> service.delete(ownerId, docId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Integer> add(int ownerId, int docId, String accessKey) {
        return provideService(IDocsService.class)
                .flatMap(service -> service.add(ownerId, docId, accessKey)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<List<VkApiDoc>> getById(Collection<IdPair> pairs) {
        String ids = join(pairs, ",", orig -> orig.ownerId + "_" + orig.id);
        return provideService(IDocsService.class)
                .flatMap(service -> service.getById(ids)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VkApiDoc>> search(String query, Integer count, Integer offset) {
        return provideService(IDocsService.class)
                .flatMap(service -> service.search(query, count, offset)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<List<VkApiDoc>> save(String file, String title, String tags) {
        return provideService(IDocsService.class)
                .flatMap(service -> service.save(file, title, tags)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<VkApiDocsUploadServer> getUploadServer(Integer groupId, String type) {
        return provideService(IDocsService.class)
                .flatMap(service -> service.getUploadServer(groupId, type)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VkApiDoc>> get(Integer ownerId, Integer count, Integer offset, Integer type) {
        return provideService(IDocsService.class)
                .flatMap(service -> service.get(ownerId, count, offset, type)
                        .map(extractResponseWithErrorHandling()));
    }
}
