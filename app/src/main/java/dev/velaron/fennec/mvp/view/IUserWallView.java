package dev.velaron.fennec.mvp.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import java.util.List;

import dev.velaron.fennec.model.FriendsCounters;
import dev.velaron.fennec.model.Post;
import dev.velaron.fennec.model.PostFilter;
import dev.velaron.fennec.model.User;
import dev.velaron.fennec.model.UserDetails;

/**
 * Created by ruslan.kolbasa on 23.01.2017.
 * phoenix
 */
public interface IUserWallView extends IWallView, IProgressView, ISnackbarView {

    void displayWallFilters(List<PostFilter> filters);
    void notifyWallFiltersChanged();

    void setupPrimaryActionButton(@StringRes Integer resourceId);

    void openFriends(int accountId, int userId, int tab, FriendsCounters counters);

    void openGroups(int accountId, int userId, @Nullable User user);

    void showEditStatusDialog(String initialValue);

    void showAddToFriendsMessageDialog();

    void showDeleteFromFriendsMessageDialog();

    void showAvatarContextMenu(boolean canUploadAvatar);

    void displayCounters(int friends, int followers, int groups, int photos, int audios, int videos);

    void displayUserStatus(String statusText);

    void displayBaseUserInfo(User user);

    void openUserDetails(int accountId, @NonNull User user, @NonNull UserDetails details);

    void showAvatarUploadedMessage(int accountId, Post post);
}