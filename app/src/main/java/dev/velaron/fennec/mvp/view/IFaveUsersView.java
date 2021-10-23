package dev.velaron.fennec.mvp.view;

import java.util.List;

import dev.velaron.fennec.mvp.core.IMvpView;
import dev.velaron.fennec.model.FavePage;
import dev.velaron.fennec.model.Owner;
import dev.velaron.fennec.mvp.view.base.IAccountDependencyView;

/**
 * Created by Ruslan Kolbasa on 11.09.2017.
 * phoenix
 */
public interface IFaveUsersView extends IAccountDependencyView, IMvpView, IErrorView {
    void displayData(List<FavePage> pages);
    void notifyDataSetChanged();
    void notifyDataAdded(int position, int count);

    void showRefreshing(boolean refreshing);

    void openOwnerWall(int accountId, Owner owner);

    void notifyItemRemoved(int index);
}