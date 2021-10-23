package dev.velaron.fennec.db.interfaces;

import androidx.annotation.NonNull;

import java.util.Collection;
import java.util.List;

import dev.velaron.fennec.db.model.entity.CommunityEntity;
import dev.velaron.fennec.db.model.entity.FriendListEntity;
import dev.velaron.fennec.db.model.entity.UserEntity;
import io.reactivex.Completable;
import io.reactivex.Single;

public interface IRelativeshipStorage extends IStorage {

    Completable storeFriendsList(int accountId, int userId, @NonNull Collection<FriendListEntity> data);

    Completable storeFriends(int accountId, @NonNull List<UserEntity> users, int objectId, boolean clearBeforeStore);
    Completable storeFollowers(int accountId, @NonNull List<UserEntity> users, int objectId, boolean clearBeforeStore);

    Single<List<UserEntity>> getFriends(int accountId, int objectId);
    Single<List<UserEntity>> getFollowers(int accountId, int objectId);

    Single<List<CommunityEntity>> getCommunities(int accountId, int ownerId);

    Completable storeComminities(int accountId, List<CommunityEntity> communities, int userId, boolean invalidateBefore);
}