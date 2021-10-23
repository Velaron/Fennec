package dev.velaron.fennec.mvp.view;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import java.util.List;

import biz.dealnote.mvp.core.IMvpView;
import dev.velaron.fennec.model.Dialog;
import dev.velaron.fennec.model.Owner;
import dev.velaron.fennec.model.User;
import dev.velaron.fennec.mvp.view.base.IAccountDependencyView;

/**
 * Created by admin on 11.01.2017.
 * phoenix
 */
public interface IDialogsView extends IAccountDependencyView, IMvpView, IErrorView, IToastView {

    void displayData(List<Dialog> data);
    void notifyDataSetChanged();
    void notifyDataAdded(int position, int count);
    void showRefreshing(boolean refreshing);

    void goToChat(int accountId, int messagesOwnerId, int peerId, String title, String avaurl);
    void goToSearch(int accountId);

    void showSnackbar(@StringRes int res, boolean isLong);

    void showEnterNewGroupChatTitle(List<User> users);
    void showNotificationSettings(int accountId, int peerId);

    void goToOwnerWall(int accountId, int ownerId, @Nullable Owner owner);

    void setCreateGroupChatButtonVisible(boolean visible);

    interface IContextView {
        void setCanDelete(boolean can);
        void setCanAddToHomescreen(boolean can);
        void setCanConfigNotifications(boolean can);
        void setCanAddToShortcuts(boolean can);
    }

    interface IOptionView {
        void setCanSearch(boolean can);
    }
}