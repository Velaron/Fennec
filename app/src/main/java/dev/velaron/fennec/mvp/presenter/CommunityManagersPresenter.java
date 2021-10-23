package dev.velaron.fennec.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.velaron.fennec.Injection;
import dev.velaron.fennec.R;
import dev.velaron.fennec.domain.IGroupSettingsInteractor;
import dev.velaron.fennec.domain.Repository;
import dev.velaron.fennec.domain.impl.GroupSettingsInteractor;
import dev.velaron.fennec.model.Manager;
import dev.velaron.fennec.model.User;
import dev.velaron.fennec.mvp.presenter.base.AccountDependencyPresenter;
import dev.velaron.fennec.mvp.view.ICommunityManagersView;
import dev.velaron.fennec.util.Analytics;
import dev.velaron.fennec.util.RxUtils;
import dev.velaron.fennec.util.Utils;

/**
 * Created by admin on 13.06.2017.
 * phoenix
 */
public class CommunityManagersPresenter extends AccountDependencyPresenter<ICommunityManagersView> {

    private final int groupId;

    private final List<Manager> data;

    private final IGroupSettingsInteractor interactor;

    public CommunityManagersPresenter(int accountId, int groupId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.interactor = new GroupSettingsInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores().owners(), Repository.INSTANCE.getOwners());
        this.groupId = groupId;
        this.data = new ArrayList<>();

        appendDisposable(Injection.provideStores()
                .owners()
                .observeManagementChanges()
                .filter(pair -> pair.getFirst() == groupId)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(pair -> onManagerActionReceived(pair.getSecond()), Analytics::logUnexpectedError));

        requestData();
    }

    private void onManagerActionReceived(Manager manager){
        int index = Utils.findIndexByPredicate(data, m -> m.getUser().getId() == manager.getUser().getId());
        boolean removing = Utils.isEmpty(manager.getRole());

        if (index != -1) {
            if(removing){
                data.remove(index);
                callView(view -> view.notifyItemRemoved(index));
            } else {
                data.set(index, manager);
                callView(view -> view.notifyItemChanged(index));
            }
        } else {
            if(!removing){
                data.add(0, manager);
                callView(view -> view.notifyItemAdded(0));
            }
        }
    }

    private void requestData() {
        final int accountId = super.getAccountId();

        setLoadingNow(true);
        appendDisposable(interactor.getManagers(accountId, groupId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onDataReceived, this::onRequestError));
    }

    @Override
    public void onGuiCreated(@NonNull ICommunityManagersView view) {
        super.onGuiCreated(view);
        view.displayData(data);
    }

    private boolean loadingNow;

    private void setLoadingNow(boolean loadingNow) {
        this.loadingNow = loadingNow;
        resolveRefreshingView();
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
    }

    private void resolveRefreshingView() {
        if (isGuiResumed()) {
            getView().displayRefreshing(loadingNow);
        }
    }

    private void onRequestError(Throwable throwable) {
        setLoadingNow(false);
        showError(getView(), throwable);
    }

    private void onDataReceived(List<Manager> managers) {
        setLoadingNow(false);

        this.data.clear();
        this.data.addAll(managers);

        callView(ICommunityManagersView::notifyDataSetChanged);
    }

    public void fireRefresh() {
        requestData();
    }

    public void fireManagerClick(Manager manager) {
        getView().goToManagerEditing(getAccountId(), groupId, manager);
    }

    public void fireRemoveClick(Manager manager) {
        final int accountId = super.getAccountId();
        final User user = manager.getUser();

        appendDisposable(interactor.editManager(accountId, groupId, user, null, false, null, null, null)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(this::onRemoveComplete, throwable -> onRemoveError(Utils.getCauseIfRuntime(throwable))));
    }

    private void onRemoveError(Throwable throwable) {
        throwable.printStackTrace();
        showError(getView(), throwable);
    }

    private void onRemoveComplete() {
        safeShowToast(getView(), R.string.deleted, false);
    }

    public void fireButtonAddClick() {
        getView().startSelectProfilesActivity(getAccountId(), groupId);
    }

    public void fireProfilesSelected(ArrayList<User> users) {
        getView().startAddingUsersToManagers(getAccountId(), groupId, users);
    }
}