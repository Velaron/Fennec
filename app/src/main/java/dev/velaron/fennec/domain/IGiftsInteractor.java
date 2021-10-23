package dev.velaron.fennec.domain;

import java.util.List;

import dev.velaron.fennec.fragment.search.nextfrom.IntNextFrom;
import dev.velaron.fennec.model.Gift;
import io.reactivex.Single;

public interface IGiftsInteractor {
    Single<List<Gift>> get(int userId, IntNextFrom start, int count);
}
