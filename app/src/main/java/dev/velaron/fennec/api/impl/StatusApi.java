package dev.velaron.fennec.api.impl;

import dev.velaron.fennec.api.IServiceProvider;
import dev.velaron.fennec.api.TokenType;
import dev.velaron.fennec.api.interfaces.IStatusApi;
import dev.velaron.fennec.api.services.IStatusService;
import io.reactivex.Single;

/**
 * Created by admin on 08.01.2017.
 * phoenix
 */
class StatusApi extends AbsApi implements IStatusApi {

    StatusApi(int accountId, IServiceProvider provider) {
        super(accountId, provider);
    }

    @Override
    public Single<Boolean> set(String text, Integer groupId) {
        return provideService(IStatusService.class, TokenType.USER)
                .flatMap(service -> service.set(text, groupId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }
}
