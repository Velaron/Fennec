package dev.velaron.fennec.domain;

import java.util.Map;

import androidx.annotation.NonNull;
import dev.velaron.fennec.model.Owner;
import dev.velaron.fennec.model.Privacy;
import dev.velaron.fennec.model.SimplePrivacy;
import dev.velaron.fennec.util.Optional;
import io.reactivex.Single;

/**
 * Created by Ruslan Kolbasa on 18.09.2017.
 * phoenix
 */
public interface IUtilsInteractor {
    Single<Map<Integer, Privacy>> createFullPrivacies(int accountId, @NonNull Map<Integer, SimplePrivacy> orig);
    Single<Optional<Owner>> resolveDomain(final int accountId, String domain);
}