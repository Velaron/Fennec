package dev.velaron.fennec.api.interfaces;

import dev.velaron.fennec.api.model.LoginResponse;
import io.reactivex.Single;

/**
 * Created by admin on 16.07.2017.
 * phoenix
 */
public interface IAuthApi {
    Single<LoginResponse> directLogin(String grantType, int clientId, String clientSecret,
                                      String username, String pass, String v, boolean twoFaSupported,
                                      String scope, String code, String captchaSid, String captchaKey, boolean forceSms);
}