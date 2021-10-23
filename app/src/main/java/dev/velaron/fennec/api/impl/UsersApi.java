package dev.velaron.fennec.api.impl;

import static dev.velaron.fennec.util.Objects.isNull;
import static dev.velaron.fennec.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import dev.velaron.fennec.api.IServiceProvider;
import dev.velaron.fennec.api.TokenType;
import dev.velaron.fennec.api.interfaces.IUsersApi;
import dev.velaron.fennec.api.model.Items;
import dev.velaron.fennec.api.model.VKApiUser;
import dev.velaron.fennec.api.model.response.UserWallInfoResponse;
import dev.velaron.fennec.api.services.IUsersService;
import dev.velaron.fennec.exception.NotFoundException;
import dev.velaron.fennec.util.Utils;
import io.reactivex.Single;

/**
 * Created by admin on 04.01.2017.
 * phoenix
 */
class UsersApi extends AbsApi implements IUsersApi {

    UsersApi(int accountId, IServiceProvider provider) {
        super(accountId, provider);
    }

    @Override
    public Single<VKApiUser> getUserWallInfo(int userId, String fields, String nameCase) {
        return provideService(IUsersService.class, TokenType.USER)
                .flatMap(service -> service
                        .getUserWallInfo(userId, fields, nameCase)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> {
                            if (Utils.safeCountOf(response.users) != 1) {
                                throw new NotFoundException();
                            }

                            return createFrom(response);
                        }));
    }

    private static VKApiUser createFrom(UserWallInfoResponse response) {
        VKApiUser user = response.users.get(0);

        if (isNull(user.counters)) {
            user.counters = new VKApiUser.Counters();
        }

        if (nonNull(response.allWallCount)) {
            user.counters.all_wall = response.allWallCount;
        }

        if (nonNull(response.ownerWallCount)) {
            user.counters.owner_wall = response.ownerWallCount;
        }

        if (nonNull(response.postponedWallCount)) {
            user.counters.postponed_wall = response.postponedWallCount;
        }

        return user;
    }

    @Override
    public Single<Items<VKApiUser>> getFollowers(Integer userId, Integer offset, Integer count, String fields, String nameCase) {
        return provideService(IUsersService.class, TokenType.USER)
                .flatMap(service -> service.getFollowers(userId, offset, count, fields, nameCase)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiUser>> search(String query, Integer sort, Integer offset, Integer count, String fields, Integer city, Integer country, String hometown, Integer universityCountry, Integer university, Integer universityYear, Integer universityFaculty, Integer universityChair, Integer sex, Integer status, Integer ageFrom, Integer ageTo, Integer birthDay, Integer birthMonth, Integer birthYear, Boolean online, Boolean hasPhoto, Integer schoolCountry, Integer schoolCity, Integer schoolClass, Integer school, Integer schoolYear, String religion, String interests, String company, String position, Integer groupId, String fromList) {
        return provideService(IUsersService.class, TokenType.USER)
                .flatMap(service -> service
                        .search(query, sort, offset, count, fields, city, country, hometown, universityCountry,
                                university, universityYear, universityFaculty, universityChair, sex, status,
                                ageFrom, ageTo, birthDay, birthMonth, birthYear, integerFromBoolean(online),
                                integerFromBoolean(hasPhoto), schoolCountry, schoolCity, schoolClass, school,
                                schoolYear, religion, interests, company, position, groupId, fromList)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<List<VKApiUser>> get(Collection<Integer> userIds, Collection<String> domains, String fields, String nameCase) {
        ArrayList<String> ids = new ArrayList<>(1);
        if (nonNull(userIds)) {
            ids.add(join(userIds, ","));
        }

        if (nonNull(domains)) {
            ids.add(join(domains, ","));
        }

        return provideService(IUsersService.class, TokenType.USER, TokenType.SERVICE)
                .flatMap(service -> service
                        .get(join(ids, ","), fields, nameCase)
                        .map(extractResponseWithErrorHandling()));
    }
}