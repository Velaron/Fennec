package dev.velaron.fennec.api;

import dev.velaron.fennec.api.services.IAuthService;
import io.reactivex.Single;

/**
 * Created by Ruslan Kolbasa on 28.07.2017.
 * phoenix
 */
public interface IDirectLoginSeviceProvider {
    Single<IAuthService> provideAuthService();
}