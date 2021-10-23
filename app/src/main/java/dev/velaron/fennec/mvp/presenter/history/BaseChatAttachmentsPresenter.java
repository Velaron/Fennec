package dev.velaron.fennec.mvp.presenter.history;

import static dev.velaron.fennec.util.Objects.nonNull;
import static dev.velaron.fennec.util.Utils.safeIsEmpty;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.velaron.fennec.mvp.reflect.OnGuiCreated;
import dev.velaron.fennec.mvp.presenter.base.PlaceSupportPresenter;
import dev.velaron.fennec.mvp.view.IBaseChatAttachmentsView;
import dev.velaron.fennec.util.DisposableHolder;
import dev.velaron.fennec.util.Pair;
import dev.velaron.fennec.util.RxUtils;
import dev.velaron.fennec.util.Utils;
import io.reactivex.Single;

/**
 * Created by admin on 29.03.2017.
 * phoenix
 */
public abstract class BaseChatAttachmentsPresenter<T, V extends IBaseChatAttachmentsView<T>>
        extends PlaceSupportPresenter<V> {

    private final int peerId;

    final List<T> data;

    private String nextFrom;

    private boolean endOfContent;

    BaseChatAttachmentsPresenter(int peerId, int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.peerId = peerId;
        this.data = new ArrayList<>();

        initLoading();
    }

    @Override
    public void onGuiCreated(@NonNull V viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayAttachments(data);
    }

    private DisposableHolder<Void> loadingHolder = new DisposableHolder<>();

    @Override
    public void onDestroyed() {
        loadingHolder.dispose();
        loadingHolder = null;
        super.onDestroyed();
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveLoadingView();
    }

    private void resolveLoadingView(){
        if(isGuiReady()){
            getView().showLoading(loadingHolder.isActive());
        }
    }

    private void initLoading() {
        load(null);
    }

    private void load(String startFrom){
        loadingHolder.append(requestAttachments(peerId, startFrom)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> onDataReceived(startFrom, data),
                        throwable -> onRequestError(Utils.getCauseIfRuntime(throwable))));
        resolveLoadingView();
    }

    private void onRequestError(Throwable throwable){
        loadingHolder.dispose();
        resolveLoadingView();

        safeShowError(getView(), throwable.getMessage());
    }

    private void onDataReceived(String startFrom, Pair<String, List<T>> result){
        loadingHolder.dispose();
        resolveLoadingView();

        this.nextFrom = result.getFirst();
        this.endOfContent = Utils.isEmpty(nextFrom);

        List<T> newData = result.getSecond();

        if(nonNull(startFrom)){
            int startSize = data.size();

            data.addAll(newData);

            if(isGuiReady()){
                getView().notifyDataAdded(startSize, newData.size());
            }
        } else {
            data.clear();
            data.addAll(newData);

            if(isGuiReady()){
                getView().notifyDatasetChanged();
            }
        }

        resolveEmptyTextVisiblity();
        onDataChanged();
    }

    @OnGuiCreated
    private void resolveEmptyTextVisiblity(){
        if(isGuiReady()){
            getView().setEmptyTextVisible(safeIsEmpty(data));
        }
    }

    void onDataChanged(){

    }

    private boolean canLoadMore(){
        return !endOfContent && !loadingHolder.isActive();
    }

    public void fireScrollToEnd(){
        if(canLoadMore()){
            load(nextFrom);
        }
    }

    public void fireRefresh(){
        loadingHolder.dispose();
        this.nextFrom = null;

        initLoading();
    }

    abstract Single<Pair<String, List<T>>> requestAttachments(int peerId, String nextFrom);
}
