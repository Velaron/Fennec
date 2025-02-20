package dev.velaron.fennec.mvp.presenter;

import static dev.velaron.fennec.util.Utils.findIndexById;
import static dev.velaron.fennec.util.Utils.getCauseIfRuntime;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.velaron.fennec.Injection;
import dev.velaron.fennec.domain.IAccountsInteractor;
import dev.velaron.fennec.domain.IBlacklistRepository;
import dev.velaron.fennec.domain.InteractorFactory;
import dev.velaron.fennec.model.BannedPart;
import dev.velaron.fennec.model.User;
import dev.velaron.fennec.mvp.presenter.base.AccountDependencyPresenter;
import dev.velaron.fennec.mvp.view.IUserBannedView;
import dev.velaron.fennec.util.Pair;
import dev.velaron.fennec.util.RxUtils;

/**
 * Created by admin on 09.07.2017.
 * phoenix
 */
public class UserBannedPresenter extends AccountDependencyPresenter<IUserBannedView> {

    private final IAccountsInteractor interactor;
    private final List<User> users;

    private boolean endOfContent;

    public UserBannedPresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.interactor = InteractorFactory.createAccountInteractor();

        this.users = new ArrayList<>();

        loadNextPart(0);

        IBlacklistRepository repository = Injection.provideBlacklistRepository();

        appendDisposable(repository.observeAdding()
                .filter(pair -> pair.getFirst() == getAccountId())
                .map(Pair::getSecond)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUserAdded));

        appendDisposable(repository.observeRemoving()
                .filter(pair -> pair.getFirst() == getAccountId())
                .map(Pair::getSecond)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUserRemoved));
    }

    private void onUserRemoved(int id) {
        int index = findIndexById(users, id);
        if(index != -1){
            users.remove(index);

            callView(view -> view.notifyItemRemoved(index));
        }
    }

    private void onUserAdded(User user) {
        users.add(0, user);

        callView(view -> {
            view.notifyItemsAdded(0, 1);
            view.scrollToPosition(0);
        });
    }

    @Override
    public void onGuiCreated(@NonNull IUserBannedView view) {
        super.onGuiCreated(view);
        view.displayUserList(users);
    }

    private void onBannedPartReceived(int offset, BannedPart part) {
        setLoadinNow(false);

        this.endOfContent = part.getUsers().isEmpty();

        if (offset == 0) {
            users.clear();
            users.addAll(part.getUsers());
            callView(IUserBannedView::notifyDataSetChanged);
        } else {
            int startSize = users.size();
            users.addAll(part.getUsers());
            callView(view -> view.notifyItemsAdded(startSize, part.getUsers().size()));
        }

        this.endOfContent = endOfContent || part.getTotalCount() == users.size();
    }

    private void onBannedPartGetError(Throwable throwable) {
        setLoadinNow(false);
        showError(getView(), throwable);
    }

    private void setLoadinNow(boolean loadinNow) {
        this.loadinNow = loadinNow;
        resolveRefreshingView();
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
    }

    private void resolveRefreshingView() {
        if (isGuiResumed()) {
            getView().displayRefreshing(loadinNow);
        }
    }

    private boolean loadinNow;

    private void loadNextPart(final int offset) {
        if (loadinNow) return;

        final int accountId = super.getAccountId();

        setLoadinNow(true);
        appendDisposable(interactor.getBanned(accountId, 50, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(part -> onBannedPartReceived(offset, part),
                        throwable -> onBannedPartGetError(getCauseIfRuntime(throwable))));
    }

    public void fireRefresh() {
        loadNextPart(0);
    }

    public void fireButtonAddClick() {
        getView().startUserSelection(getAccountId());
    }

    private void onAddingComplete() {
        callView(IUserBannedView::showSuccessToast);
    }

    private void onAddError(Throwable throwable) {
        showError(getView(), throwable);
    }

    public void fireUsersSelected(ArrayList<User> users) {
        final int accountId = super.getAccountId();

        appendDisposable(interactor.banUsers(accountId, users)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(this::onAddingComplete, throwable -> onAddError(getCauseIfRuntime(throwable))));
    }

    public void fireScrollToEnd() {
        if (!loadinNow && !endOfContent) {
            loadNextPart(users.size());
        }
    }

    private void onRemoveComplete() {
        callView(IUserBannedView::showSuccessToast);
    }

    private void onRemoveError(Throwable throwable) {
        showError(getView(), throwable);
    }

    public void fireRemoveClick(User user) {
        final int accountId = super.getAccountId();

        appendDisposable(interactor.unbanUser(accountId, user.getId())
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(this::onRemoveComplete, throwable -> onRemoveError(getCauseIfRuntime(throwable))));
    }

    public void fireUserClick(User user) {
        getView().showUserProfile(getAccountId(), user);
    }
}