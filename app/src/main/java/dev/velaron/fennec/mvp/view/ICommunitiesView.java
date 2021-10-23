package dev.velaron.fennec.mvp.view;

import dev.velaron.fennec.mvp.core.IMvpView;
import dev.velaron.fennec.model.Community;
import dev.velaron.fennec.model.DataWrapper;
import dev.velaron.fennec.mvp.view.base.IAccountDependencyView;

/**
 * Created by Ruslan Kolbasa on 19.09.2017.
 * phoenix
 */
public interface ICommunitiesView extends IAccountDependencyView, IMvpView, IErrorView {
    void displayData(DataWrapper<Community> own, DataWrapper<Community> filtered, DataWrapper<Community> seacrh);
    void notifyDataSetChanged();
    void notifyOwnDataAdded(int position, int count);
    void displayRefreshing(boolean refreshing);

    void showCommunityWall(int accountId, Community community);

    void notifySeacrhDataAdded(int position, int count);
}