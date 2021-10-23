package dev.velaron.fennec.mvp.view;

import java.util.ArrayList;
import java.util.List;

import dev.velaron.fennec.mvp.core.IMvpView;
import dev.velaron.fennec.model.Banned;
import dev.velaron.fennec.model.User;
import dev.velaron.fennec.mvp.view.base.IAccountDependencyView;

/**
 * Created by admin on 13.06.2017.
 * phoenix
 */
public interface ICommunityBlacklistView extends IAccountDependencyView, IErrorView, IMvpView, IToastView {

    void displayRefreshing(boolean loadingNow);

    void notifyDataSetChanged();

    void diplayData(List<Banned> data);

    void notifyItemRemoved(int index);

    void openBanEditor(int accountId, int groupId, Banned banned);

    void startSelectProfilesActivity(int accountId, int groupId);

    void addUsersToBan(int accountId, int groupId, ArrayList<User> users);

    void notifyItemsAdded(int position, int size);
}
