package dev.velaron.fennec.domain.impl;

import java.util.Collections;
import java.util.List;

import dev.velaron.fennec.api.interfaces.INetworker;
import dev.velaron.fennec.api.model.VKApiUser;
import dev.velaron.fennec.db.column.UserColumns;
import dev.velaron.fennec.db.interfaces.IStorages;
import dev.velaron.fennec.db.model.entity.UserEntity;
import dev.velaron.fennec.domain.IRelationshipInteractor;
import dev.velaron.fennec.domain.mappers.Dto2Entity;
import dev.velaron.fennec.domain.mappers.Dto2Model;
import dev.velaron.fennec.domain.mappers.Entity2Model;
import dev.velaron.fennec.exception.NotFoundException;
import dev.velaron.fennec.exception.UnepectedResultException;
import dev.velaron.fennec.model.FriendsCounters;
import dev.velaron.fennec.model.User;
import dev.velaron.fennec.util.Objects;
import dev.velaron.fennec.util.Pair;
import dev.velaron.fennec.util.Utils;
import io.reactivex.Single;

/**
 * Created by Ruslan Kolbasa on 08.09.2017.
 * phoenix
 */
public class RelationshipInteractor implements IRelationshipInteractor {

    private final IStorages repositories;
    private final INetworker networker;

    public RelationshipInteractor(IStorages repositories, INetworker networker) {
        this.repositories = repositories;
        this.networker = networker;
    }

    @Override
    public Single<List<User>> getCachedFriends(int accountId, int objectId) {
        return repositories.relativeship()
                .getFriends(accountId, objectId)
                .map(Entity2Model::buildUsersFromDbo);
    }

    @Override
    public Single<List<User>> getCachedFollowers(int accountId, int objectId) {
        return repositories.relativeship()
                .getFollowers(accountId, objectId)
                .map(Entity2Model::buildUsersFromDbo);
    }

    @Override
    public Single<List<User>> getActualFriendsList(int accountId, int userId, int count, int offset) {
        String order = accountId == userId ? "hints" : null;

        return networker.vkDefault(accountId)
                .friends()
                .get(userId, order, null, count, offset, UserColumns.API_FIELDS, null)
                .map(items -> Utils.listEmptyIfNull(items.getItems()))
                .flatMap(dtos -> {
                    List<UserEntity> dbos = Dto2Entity.mapUsers(dtos);
                    List<User> users = Dto2Model.transformUsers(dtos);

                    return repositories.relativeship()
                            .storeFriends(accountId, dbos, userId, offset == 0)
                            .andThen(Single.just(users));
                });
    }

    @Override
    public Single<List<User>> getOnlineFriends(int accountId, int userId, int count, int offset) {
        String order = accountId == userId ? "hints" : null; // hints (сортировка по популярности) доступна только для своих друзей

        return networker.vkDefault(accountId)
                .friends()
                .getOnline(userId, order, count, offset, UserColumns.API_FIELDS)
                .map(response -> Utils.listEmptyIfNull(response.profiles))
                .map(Dto2Model::transformUsers);

    }

    @Override
    public Single<List<User>> getFollowers(int accountId, int userId, int count, int offset) {
        return networker.vkDefault(accountId)
                .users()
                .getFollowers(userId, offset, count, UserColumns.API_FIELDS, null)
                .map(items -> Utils.listEmptyIfNull(items.getItems()))
                .flatMap(dtos -> {
                    List<UserEntity> dbos = Dto2Entity.mapUsers(dtos);
                    List<User> users = Dto2Model.transformUsers(dtos);

                    return repositories.relativeship()
                            .storeFollowers(accountId, dbos, userId, offset == 0)
                            .andThen(Single.just(users));
                });
    }

    @Override
    public Single<List<User>> getMutualFriends(int accountId, int objectId, int count, int offset) {
        return networker.vkDefault(accountId)
                .friends()
                .getMutual(accountId, objectId, count, offset, UserColumns.API_FIELDS)
                .map(Dto2Model::transformUsers);
    }

    @Override
    public Single<Pair<List<User>, Integer>> seacrhFriends(int accountId, int userId, int count, int offset, String q) {
        return networker.vkDefault(accountId)
                .friends()
                .search(userId, q, UserColumns.API_FIELDS, null, offset, count)
                .map(items -> {
                    List<User> users = Dto2Model.transformUsers(Utils.listEmptyIfNull(items.getItems()));
                    return Pair.Companion.create(users, items.getCount());
                });
    }

    @Override
    public Single<FriendsCounters> getFriendsCounters(int accountId, int userId) {
        return networker.vkDefault(accountId)
                .users()
                .get(Collections.singletonList(userId), null, "counters", null)
                .map(users -> {
                    if(users.isEmpty()){
                        throw new NotFoundException();
                    }

                    VKApiUser user = users.get(0);
                    FriendsCounters counters;
                    if(Objects.nonNull(user.counters)){
                        counters = new FriendsCounters(user.counters.friends, user.counters.online_friends, user.counters.followers, user.counters.mutual_friends);
                    } else {
                        counters = new FriendsCounters(0, 0, 0, 0);
                    }

                    return counters;
                });
    }

    @Override
    public Single<Integer> addFriend(int accountId, int userId, String optionalText, boolean keepFollow) {
        return networker.vkDefault(accountId)
                .friends()
                .add(userId, optionalText, keepFollow);
    }

    @Override
    public Single<Integer> deleteFriends(int accountId, int userId) {
        return networker.vkDefault(accountId)
                .friends()
                .delete(userId)
                .map(response -> {
                    if(response.friend_deleted){
                        return DeletedCodes.FRIEND_DELETED;
                    }

                    if(response.in_request_deleted){
                        return DeletedCodes.IN_REQUEST_DELETED;
                    }

                    if(response.out_request_deleted){
                        return DeletedCodes.OUT_REQUEST_DELETED;
                    }

                    if(response.suggestion_deleted){
                        return DeletedCodes.SUGGESTION_DELETED;
                    }

                    throw new UnepectedResultException();
                });
    }
}