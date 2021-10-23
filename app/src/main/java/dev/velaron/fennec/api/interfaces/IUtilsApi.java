package dev.velaron.fennec.api.interfaces;

import androidx.annotation.CheckResult;

import dev.velaron.fennec.api.model.response.ResolveDomailResponse;
import io.reactivex.Single;

/**
 * Created by admin on 09.01.2017.
 * phoenix
 */
public interface IUtilsApi {

    @CheckResult
    Single<ResolveDomailResponse> resolveScreenName(String screenName);

}
