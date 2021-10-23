package dev.velaron.fennec.mvp.presenter;

import static dev.velaron.fennec.util.Utils.getCauseIfRuntime;
import static dev.velaron.fennec.util.Utils.nonEmpty;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.velaron.fennec.Injection;
import dev.velaron.fennec.R;
import dev.velaron.fennec.api.interfaces.INetworker;
import dev.velaron.fennec.db.interfaces.IOwnersStorage;
import dev.velaron.fennec.db.model.BanAction;
import dev.velaron.fennec.domain.IGroupSettingsInteractor;
import dev.velaron.fennec.domain.Repository;
import dev.velaron.fennec.domain.impl.GroupSettingsInteractor;
import dev.velaron.fennec.fragment.search.nextfrom.IntNextFrom;
import dev.velaron.fennec.model.Banned;
import dev.velaron.fennec.model.User;
import dev.velaron.fennec.mvp.presenter.base.AccountDependencyPresenter;
import dev.velaron.fennec.mvp.view.ICommunityBlacklistView;
import dev.velaron.fennec.util.RxUtils;
import dev.velaron.fennec.util.Utils;

/**
 * Created by admin on 13.06.2017.
 * phoenix
 */
public class CommunityBlacklistPresenter extends AccountDependencyPresenter<ICommunityBlacklistView> {

    private static final int COUNT = 20;

    private final int groupId;
    private final List<Banned> data;

    private final IGroupSettingsInteractor groupSettingsInteractor;

    private boolean loadingNow;

    private IntNextFrom moreStartFrom;
    private boolean endOfContent;

    public CommunityBlacklistPresenter(int accountId, int groupId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.groupId = groupId;
        this.data = new ArrayList<>();
        this.moreStartFrom = new IntNextFrom(0);

        INetworker networker = Injection.provideNetworkInterfaces();
        IOwnersStorage repository = Injection.provideStores().owners();

        this.groupSettingsInteractor = new GroupSettingsInteractor(networker, repository, Repository.INSTANCE.getOwners());

        appendDisposable(repository.observeBanActions()
                .filter(action -> action.getGroupId() == groupId)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onBanActionReceived));

        requestDataAtStart();
    }

    private void onBanActionReceived(BanAction action) {
        if (action.isBan()) {
            //refresh data
            requestDataAtStart();
        } else {
            int index = Utils.findIndexByPredicate(data, banned -> banned.getBanned().getOwnerId() == action.getOwnerId());
            if (index != -1) {
                data.remove(index);
                callView(view -> view.notifyItemRemoved(index));
            }
        }
    }

    private void setLoadingNow(boolean loadingNow) {
        this.loadingNow = loadingNow;
        resolveRefreshingView();
    }

    private void resolveRefreshingView() {
        if (isGuiReady()) {
            getView().displayRefreshing(loadingNow);
        }
    }

    private void requestDataAtStart(){
        request(new IntNextFrom(0));
    }

    private void request(IntNextFrom startFrom){
        if (loadingNow) return;

        final int accountId = super.getAccountId();

        setLoadingNow(true);
        appendDisposable(groupSettingsInteractor.getBanned(accountId, groupId, startFrom, COUNT)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(pair -> onBannedUsersReceived(startFrom, pair.getSecond(), pair.getFirst()),
                        throwable -> onRequqestError(getCauseIfRuntime(throwable))));
    }

    @Override
    public void onGuiCreated(@NonNull ICommunityBlacklistView view) {
        super.onGuiCreated(view);
        view.diplayData(this.data);
    }

    private void onRequqestError(Throwable throwable) {
        setLoadingNow(false);

        throwable.printStackTrace();
        showError(getView(), throwable);
    }

    private void onBannedUsersReceived(IntNextFrom startFrom, IntNextFrom nextFrom, List<Banned> users) {
        this.endOfContent = users.isEmpty();
        this.moreStartFrom = nextFrom;

        if(startFrom.getOffset() != 0){
            int startSize = this.data.size();
            this.data.addAll(users);
            callView(view -> view.notifyItemsAdded(startSize, users.size()));
        } else {
            this.data.clear();
            this.data.addAll(users);
            callView(ICommunityBlacklistView::notifyDataSetChanged);
        }

        setLoadingNow(false);
    }

    public void fireRefresh() {
        requestDataAtStart();
    }

    public void fireBannedClick(Banned banned) {
        getView().openBanEditor(getAccountId(), groupId, banned);
    }

    public void fireAddClick() {
        getView().startSelectProfilesActivity(getAccountId(), groupId);
    }

    public void fireAddToBanUsersSelected(ArrayList<User> users) {
        if (nonEmpty(users)) {
            getView().addUsersToBan(getAccountId(), groupId, users);
        }
    }

    public void fireBannedRemoveClick(Banned banned) {
        appendDisposable(groupSettingsInteractor
                .unban(getAccountId(), groupId, banned.getBanned().getOwnerId())
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> onUnbanComplete(banned), this::onUnbanError));
    }

    @SuppressWarnings("unused")
    private void onUnbanComplete(Banned banned) {
        safeShowToast(getView(), R.string.deleted, false);
    }

    private void onUnbanError(Throwable throwable) {
        showError(getView(), throwable);
    }

    private boolean canLoadMore() {
        return !endOfContent && !loadingNow && nonEmpty(data) && moreStartFrom.getOffset() > 0;
    }

    public void fireScrollToBottom() {
        if (canLoadMore()) {
            request(moreStartFrom);
        }
    }
}