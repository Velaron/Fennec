package dev.velaron.fennec.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.velaron.fennec.model.Owner;
import dev.velaron.fennec.mvp.presenter.base.AccountDependencyPresenter;
import dev.velaron.fennec.mvp.view.ISimpleOwnersView;

/**
 * Created by Ruslan Kolbasa on 08.09.2017.
 * phoenix
 */
public abstract class SimpleOwnersPresenter<V extends ISimpleOwnersView> extends AccountDependencyPresenter<V> {

    List<Owner> data;

    public SimpleOwnersPresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.data = new ArrayList<>();
    }

    @Override
    public void onGuiCreated(@NonNull V view) {
        super.onGuiCreated(view);
        view.displayOwnerList(data);
    }

    public final void fireRefresh(){
        this.onUserRefreshed();
    }

    void onUserRefreshed(){

    }

    public final void fireScrollToEnd() {
        this.onUserScrolledToEnd();
    }

    void onUserScrolledToEnd(){

    }

    void onUserOwnerClicked(Owner owner){
        getView().showOwnerWall(getAccountId(), owner);
    }

    public final void fireOwnerClick(Owner owner) {
        this.onUserOwnerClicked(owner);
    }
}