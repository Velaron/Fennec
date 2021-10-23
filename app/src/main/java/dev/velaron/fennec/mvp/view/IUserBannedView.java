package dev.velaron.fennec.mvp.view;

import java.util.List;

import dev.velaron.fennec.mvp.core.IMvpView;
import dev.velaron.fennec.model.User;
import dev.velaron.fennec.mvp.view.base.IAccountDependencyView;

/**
 * Created by admin on 09.07.2017.
 * phoenix
 */
public interface IUserBannedView extends IAccountDependencyView, IMvpView, IErrorView {
    void displayUserList(List<User> users);

    void notifyItemsAdded(int position, int count);
    void notifyDataSetChanged();
    void notifyItemRemoved(int position);

    void displayRefreshing(boolean refreshing);

    void startUserSelection(int accountId);
    void showSuccessToast();

    void scrollToPosition(int position);

    void showUserProfile(int accountId, User user);
}