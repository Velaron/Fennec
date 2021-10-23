package dev.velaron.fennec.api.interfaces;

import androidx.annotation.CheckResult;

import java.util.List;

import dev.velaron.fennec.api.model.Items;
import dev.velaron.fennec.api.model.VKApiUser;
import dev.velaron.fennec.api.model.VkApiFriendList;
import dev.velaron.fennec.api.model.response.DeleteFriendResponse;
import dev.velaron.fennec.api.model.response.OnlineFriendsResponse;
import io.reactivex.Single;

/**
 * Created by admin on 30.12.2016.
 * phoenix
 */
public interface IFriendsApi {

    @CheckResult
    Single<OnlineFriendsResponse> getOnline(int userId, String order, int count,
                                            int offset, String fields);
    @CheckResult
    Single<Items<VKApiUser>> get(Integer userId, String order, Integer listId, Integer count, Integer offset,
                                 String fields, String nameCase);

    @CheckResult
    Single<Items<VkApiFriendList>> getLists(Integer userId, Boolean returnSystem);

    @CheckResult
    Single<DeleteFriendResponse> delete(int userId);

    @CheckResult
    Single<Integer> add(int userId, String text, Boolean follow);

    @CheckResult
    Single<Items<VKApiUser>> search(int userId, String query, String fields, String nameCase,
                                    Integer offset, Integer count);

    @CheckResult
    Single<List<VKApiUser>> getMutual(Integer sourceUid, int targetUid, int count, int offset, String fields);

}
