package dev.velaron.fennec.mvp.view;

import java.util.List;

import biz.dealnote.mvp.core.IMvpView;
import dev.velaron.fennec.model.Owner;
import dev.velaron.fennec.mvp.view.base.IAccountDependencyView;

/**
 * Created by Ruslan Kolbasa on 08.09.2017.
 * phoenix
 */
public interface ISimpleOwnersView extends IMvpView, IErrorView, IAccountDependencyView {
    void displayOwnerList(List<Owner> owners);
    void notifyDataSetChanged();
    void notifyDataAdded(int position, int count);

    void displayRefreshing(boolean refreshing);
    void showOwnerWall(int accoutnId, Owner owner);
}