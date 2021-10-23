package dev.velaron.fennec.mvp.view;

import java.util.ArrayList;
import java.util.List;

import dev.velaron.fennec.mvp.core.IMvpView;
import dev.velaron.fennec.model.Manager;
import dev.velaron.fennec.model.User;
import dev.velaron.fennec.mvp.view.base.IAccountDependencyView;

/**
 * Created by admin on 13.06.2017.
 * phoenix
 */
public interface ICommunityManagersView extends IAccountDependencyView, IErrorView, IMvpView, IToastView {

    void notifyDataSetChanged();

    void displayRefreshing(boolean loadingNow);

    void displayData(List<Manager> managers);

    void goToManagerEditing(int accountId, int groupId, Manager manager);

    void showUserProfile(int accountId, User user);

    void startSelectProfilesActivity(int accountId, int groupId);

    void startAddingUsersToManagers(int accountId, int groupId, ArrayList<User> users);

    void notifyItemRemoved(int index);

    void notifyItemChanged(int index);

    void notifyItemAdded(int index);
}
