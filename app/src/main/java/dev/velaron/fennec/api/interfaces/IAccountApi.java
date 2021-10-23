package dev.velaron.fennec.api.interfaces;

import androidx.annotation.CheckResult;

import dev.velaron.fennec.api.model.CountersDto;
import dev.velaron.fennec.api.model.Items;
import dev.velaron.fennec.api.model.VKApiUser;
import io.reactivex.Single;

/**
 * Created by admin on 04.01.2017.
 * phoenix
 */
public interface IAccountApi {

    @CheckResult
    Single<Integer> banUser(int userId);

    @CheckResult
    Single<Integer> unbanUser(int userId);

    Single<Items<VKApiUser>> getBanned(Integer count, Integer offset, String fields);

    @CheckResult
    Single<Boolean> unregisterDevice(String deviceId);

    @CheckResult
    Single<Boolean> registerDevice(String token, String deviceModel, Integer deviceYear, String deviceId,
                                   String systemVersion, String settings);

    @CheckResult
    Single<Boolean> setOffline();

    @CheckResult
    Single<Boolean> setOnline(Boolean voip);

    @CheckResult
    Single<CountersDto> getCounters(String filter);
}