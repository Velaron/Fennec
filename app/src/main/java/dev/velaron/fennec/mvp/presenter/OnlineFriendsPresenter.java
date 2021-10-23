package dev.velaron.fennec.mvp.presenter;

import static dev.velaron.fennec.util.Utils.nonEmpty;

import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.List;

import dev.velaron.fennec.domain.IRelationshipInteractor;
import dev.velaron.fennec.domain.InteractorFactory;
import dev.velaron.fennec.model.User;
import dev.velaron.fennec.mvp.view.ISimpleOwnersView;
import dev.velaron.fennec.util.RxUtils;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by Ruslan Kolbasa on 08.09.2017.
 * phoenix
 */
public class OnlineFriendsPresenter extends SimpleOwnersPresenter<ISimpleOwnersView> {

    private final int userId;
    private boolean endOfContent;
    private final IRelationshipInteractor relationshipInteractor;

    public OnlineFriendsPresenter(int accountId, int userId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.userId = userId;
        this.relationshipInteractor = InteractorFactory.createRelationshipInteractor();

        requestActualData(0);
    }

    private boolean actualDataLoading;
    private CompositeDisposable actualDataDisposable = new CompositeDisposable();

    private void resolveRefreshingView() {
        if (isGuiResumed()) {
            getView().displayRefreshing(actualDataLoading);
        }
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
    }

    private void requestActualData(int offset) {
        this.actualDataLoading = true;
        resolveRefreshingView();

        final int accountId = super.getAccountId();
        actualDataDisposable.add(relationshipInteractor.getOnlineFriends(accountId, userId, 200, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(users -> onDataReceived(offset, users), this::onDataGetError));
    }

    private void onDataGetError(Throwable t) {
        this.actualDataLoading = false;
        resolveRefreshingView();

        showError(getView(), t);
    }

    private void onDataReceived(int offset, List<User> users) {
        this.actualDataLoading = false;

        this.endOfContent = users.isEmpty();

        if (offset == 0) {
            super.data.clear();
            super.data.addAll(users);
            callView(ISimpleOwnersView::notifyDataSetChanged);
        } else {
            int sizeBefore = super.data.size();
            super.data.addAll(users);
            callView(view -> view.notifyDataAdded(sizeBefore, users.size()));
        }

        resolveRefreshingView();
    }

    @Override
    void onUserScrolledToEnd() {
        if (!endOfContent && !actualDataLoading && nonEmpty(super.data)) {
            requestActualData(super.data.size());
        }
    }

    @Override
    void onUserRefreshed() {
        this.actualDataDisposable.clear();
        requestActualData(0);
    }

    @Override
    public void onDestroyed() {
        actualDataDisposable.dispose();
        super.onDestroyed();
    }
}