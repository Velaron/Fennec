package dev.velaron.fennec.domain;

import dev.velaron.fennec.model.SectionCounters;
import io.reactivex.Observable;

/**
 * Created by Ruslan Kolbasa on 30.06.2017.
 * phoenix
 */
public interface ICountersInteractor {
    Observable<SectionCounters> getCounters(int accountId);
}