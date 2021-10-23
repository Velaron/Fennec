package dev.velaron.fennec.api.interfaces;

import java.util.Map;

import dev.velaron.fennec.util.Optional;
import io.reactivex.Single;

/**
 * Created by Ruslan Kolbasa on 31.07.2017.
 * phoenix
 */
public interface IOtherApi {
    Single<Optional<String>> rawRequest(String method, Map<String, String> postParams);
}