package dev.velaron.fennec.mvp.presenter;

import static dev.velaron.fennec.util.Utils.getCauseIfRuntime;
import static dev.velaron.fennec.util.Utils.nonEmpty;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.velaron.fennec.domain.IFaveInteractor;
import dev.velaron.fennec.domain.InteractorFactory;
import dev.velaron.fennec.model.FaveLink;
import dev.velaron.fennec.mvp.presenter.base.AccountDependencyPresenter;
import dev.velaron.fennec.mvp.view.IFaveLinksView;
import dev.velaron.fennec.util.RxUtils;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by admin on 07.10.2017.
 * Phoenix-for-VK
 */
public class FaveLinksPresenter extends AccountDependencyPresenter<IFaveLinksView> {

    private final IFaveInteractor faveInteractor;

    private final List<FaveLink> links;

    private boolean endOfContent;

    private boolean actualDataReceived;

    public FaveLinksPresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.links = new ArrayList<>();
        this.faveInteractor = InteractorFactory.createFaveInteractor();

        loadCachedData();
        loadActual(0);
    }

    private CompositeDisposable cacheDisposable = new CompositeDisposable();

    private boolean cacheLoading;

    private void loadCachedData() {
        this.cacheLoading = true;
        final int accountId = super.getAccountId();
        cacheDisposable.add(faveInteractor.getCachedLinks(accountId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onCachedDataReceived, RxUtils.ignore()));
    }

    private CompositeDisposable actualDisposable = new CompositeDisposable();

    private boolean actualLoading;

    private void loadActual(int offset) {
        this.actualLoading = true;
        final int accountId = super.getAccountId();

        resolveRefreshingView();
        actualDisposable.add(faveInteractor.getLinks(accountId, 50, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> onActualDataReceived(data.get(), offset, data.hasNext()), this::onActualGetError));
    }

    private void onActualGetError(Throwable t) {
        actualLoading = false;
        resolveRefreshingView();
        showError(getView(), getCauseIfRuntime(t));
    }

    private void onActualDataReceived(List<FaveLink> data, int offset, boolean hasNext) {
        this.cacheDisposable.clear();
        this.cacheLoading = false;

        this.actualLoading = false;
        this.endOfContent = !hasNext;
        this.actualDataReceived = true;

        if (offset == 0) {
            this.links.clear();
            this.links.addAll(data);
            callView(IFaveLinksView::notifyDataSetChanged);
        } else {
            int sizeBefore = this.links.size();
            this.links.addAll(data);
            callView(view -> view.notifyDataAdded(sizeBefore, data.size()));
        }

        resolveRefreshingView();
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
    }

    private void resolveRefreshingView() {
        if (isGuiResumed()) {
            getView().displayRefreshing(actualLoading);
        }
    }

    public void fireRefresh() {
        cacheDisposable.clear();
        cacheLoading = false;

        actualDisposable.clear();
        loadActual(0);
    }

    public void fireScrollToEnd() {
        if (actualDataReceived && !endOfContent && !cacheLoading && !actualLoading && nonEmpty(links)) {
            loadActual(this.links.size());
        }
    }

    @Override
    public void onDestroyed() {
        cacheDisposable.dispose();
        actualDisposable.dispose();
        super.onDestroyed();
    }

    private void onCachedDataReceived(List<FaveLink> links) {
        this.cacheLoading = false;

        this.links.clear();
        this.links.addAll(links);
        callView(IFaveLinksView::notifyDataSetChanged);
    }

    @Override
    public void onGuiCreated(@NonNull IFaveLinksView view) {
        super.onGuiCreated(view);
        view.displayLinks(links);
    }

    public void fireDeleteClick(FaveLink link) {
        final int accountId = super.getAccountId();
        final String id = link.getId();
        appendDisposable(faveInteractor.removeLink(accountId, id)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> onLinkRemoved(accountId, id), t -> showError(getView(), getCauseIfRuntime(t))));
    }

    private void onLinkRemoved(int accountId, String id) {
        if (getAccountId() != accountId) {
            return;
        }

        for (int i = 0; i < links.size(); i++) {
            if (links.get(i).getId().equals(id)) {
                this.links.remove(i);

                int finalI = i;
                callView(view -> view.notifyItemRemoved(finalI));
                break;
            }
        }
    }

    public void fireLinkClick(FaveLink link) {
        getView().openLink(getAccountId(), link);
    }
}