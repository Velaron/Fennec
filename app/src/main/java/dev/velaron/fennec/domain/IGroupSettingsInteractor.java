package dev.velaron.fennec.domain;

import java.util.List;

import dev.velaron.fennec.fragment.search.nextfrom.IntNextFrom;
import dev.velaron.fennec.model.Banned;
import dev.velaron.fennec.model.GroupSettings;
import dev.velaron.fennec.model.Manager;
import dev.velaron.fennec.model.User;
import dev.velaron.fennec.util.Pair;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by Ruslan Kolbasa on 15.06.2017.
 * phoenix
 */
public interface IGroupSettingsInteractor {

    Single<GroupSettings> getGroupSettings(int accountId, int groupId);

    Completable ban(int accountId, int groupId, int ownerId, Long endDateUnixtime, int reason, String comment, boolean commentVisible);

    Completable editManager(int accountId, int groupId, User user, String role, boolean asContact, String position, String email, String phone);

    Completable unban(int accountId, int groupId, int ownerId);

    Single<Pair<List<Banned>, IntNextFrom>> getBanned(int accountId, int groupId, IntNextFrom startFrom, int count);

    Single<List<Manager>> getManagers(int accountId, int groupId);
}