package dev.velaron.fennec.api.impl;

import dev.velaron.fennec.api.IServiceProvider;
import dev.velaron.fennec.api.TokenType;
import dev.velaron.fennec.api.interfaces.IAccountApi;
import dev.velaron.fennec.api.model.CountersDto;
import dev.velaron.fennec.api.model.Items;
import dev.velaron.fennec.api.model.VKApiUser;
import dev.velaron.fennec.api.services.IAccountService;
import io.reactivex.Single;

/**
 * Created by admin on 04.01.2017.
 * phoenix
 */
class AccountApi extends AbsApi implements IAccountApi {

    AccountApi(int accountId, IServiceProvider provider) {
        super(accountId, provider);
    }

    @Override
    public Single<Integer> banUser(int userId) {
        return provideService(IAccountService.class, TokenType.USER)
                .flatMap(service -> service
                        .banUser(userId)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Integer> unbanUser(int userId) {
        return provideService(IAccountService.class, TokenType.USER)
                .flatMap(service -> service
                        .unbanUser(userId)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiUser>> getBanned(Integer count, Integer offset, String fields) {
        return provideService(IAccountService.class, TokenType.USER)
                .flatMap(service -> service
                        .getBanned(count, offset, fields)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Boolean> unregisterDevice(String deviceId) {
        return provideService(IAccountService.class, TokenType.USER)
                .flatMap(service -> service.unregisterDevice(deviceId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Boolean> registerDevice(String token, String deviceModel, Integer deviceYear,
                                          String deviceId, String systemVersion, String settings) {
        return provideService(IAccountService.class, TokenType.USER)
                .flatMap(service -> service
                        .registerDevice(token, deviceModel, deviceYear, deviceId, systemVersion, settings)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Boolean> setOffline() {
        return provideService(IAccountService.class, TokenType.USER)
                .flatMap(service -> service
                        .setOffline()
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Boolean> setOnline(Boolean voip) {
        return provideService(IAccountService.class, TokenType.USER)
                .flatMap(service -> service.setOnline(integerFromBoolean(voip))
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<CountersDto> getCounters(String filter) {
        return provideService(IAccountService.class, TokenType.USER)
                .flatMap(service -> service
                        .getCounters(filter)
                        .map(extractResponseWithErrorHandling()));
    }
}