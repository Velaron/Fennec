package dev.velaron.fennec.mvp.view;

import java.util.List;

import biz.dealnote.mvp.core.IMvpView;
import dev.velaron.fennec.model.AppChatUser;
import dev.velaron.fennec.model.Owner;
import dev.velaron.fennec.mvp.view.base.IAccountDependencyView;

/**
 * Created by Ruslan Kolbasa on 19.09.2017.
 * phoenix
 */
public interface IChatMembersView extends IAccountDependencyView, IMvpView, IErrorView {
    void displayData(List<AppChatUser> users);
    void notifyItemRemoved(int position);
    void notifyDataSetChanged();
    void notifyDataAdded(int position, int count);
    void openUserWall(int accountId, Owner user);
    void displayRefreshing(boolean refreshing);

    void startSelectUsersActivity(int accountId);
}