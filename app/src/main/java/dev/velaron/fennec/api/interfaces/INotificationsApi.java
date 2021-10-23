package dev.velaron.fennec.api.interfaces;

import androidx.annotation.CheckResult;

import dev.velaron.fennec.api.model.response.NotificationsResponse;
import io.reactivex.Single;

/**
 * Created by admin on 03.01.2017.
 * phoenix
 */
public interface INotificationsApi {

    @CheckResult
    Single<Integer> markAsViewed();

    @CheckResult
    Single<NotificationsResponse> get(Integer count, String startFrom, String filters,
                                                    Long startTime, Long endTime);

}
