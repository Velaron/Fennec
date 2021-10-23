package dev.velaron.fennec.mvp.view;

import biz.dealnote.mvp.core.IMvpView;
import dev.velaron.fennec.model.FriendsCounters;
import dev.velaron.fennec.mvp.view.base.IAccountDependencyView;

/**
 * Created by Ruslan Kolbasa on 08.09.2017.
 * phoenix
 */
public interface IFriendsTabsView extends IMvpView, IAccountDependencyView, IErrorView {
    void displayConters(FriendsCounters counters);
    void configTabs(int accountId, int userId, boolean showMutualTab);
    void displayUserNameAtToolbar(String userName);

    void setDrawerFriendsSectionSelected(boolean selected);
}