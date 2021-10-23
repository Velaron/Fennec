package dev.velaron.fennec.domain;

import java.util.List;

import dev.velaron.fennec.model.Owner;
import io.reactivex.Single;

/**
 * Created by admin on 03.10.2017.
 * phoenix
 */
public interface ILikesInteractor {
    String FILTER_LIKES = "likes";
    String FILTER_COPIES = "copies";

    Single<List<Owner>> getLikes(int accountId, String type, int ownerId, int itemId, String filter, int count, int offset);
}