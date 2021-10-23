package dev.velaron.fennec.db.interfaces;

import java.util.List;

import dev.velaron.fennec.db.model.entity.CountryEntity;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by Ruslan Kolbasa on 20.09.2017.
 * phoenix
 */
public interface IDatabaseStore {
    Completable storeCountries(int accountId, List<CountryEntity> dbos);
    Single<List<CountryEntity>> getCountries(int accountId);
}