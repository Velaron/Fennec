package dev.velaron.fennec.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.mvp.reflect.OnGuiCreated;
import dev.velaron.fennec.Injection;
import dev.velaron.fennec.R;
import dev.velaron.fennec.db.interfaces.ILogsStorage;
import dev.velaron.fennec.model.LogEvent;
import dev.velaron.fennec.model.LogEventType;
import dev.velaron.fennec.model.LogEventWrapper;
import dev.velaron.fennec.mvp.presenter.base.RxSupportPresenter;
import dev.velaron.fennec.mvp.view.ILogsView;
import dev.velaron.fennec.util.DisposableHolder;
import dev.velaron.fennec.util.RxUtils;
import dev.velaron.fennec.util.Utils;

/**
 * Created by Ruslan Kolbasa on 26.04.2017.
 * phoenix
 */
public class LogsPresenter extends RxSupportPresenter<ILogsView> {

    private final List<LogEventType> types;

    private final List<LogEventWrapper> events;

    private final ILogsStorage store;

    public LogsPresenter(@Nullable Bundle savedInstanceState) {
        super(savedInstanceState);

        this.store = Injection.provideLogsStore();
        this.types = createTypes();
        this.events = new ArrayList<>();

        loadAll();
    }

    @OnGuiCreated
    private void resolveEmptyTextVisibility(){
        if(isGuiReady()){
            getView().setEmptyTextVisible(events.isEmpty());
        }
    }

    private boolean loadingNow;

    private void setLoading(boolean loading) {
        this.loadingNow = loading;
        resolveRefreshingView();
    }

    @Override
    public void onGuiCreated(@NonNull ILogsView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayData(events);
        viewHost.displayTypes(types);
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
    }

    private void resolveRefreshingView(){
        if(isGuiResumed()){
            getView().showRefreshing(loadingNow);
        }
    }

    private void loadAll() {
        final int type = getSelectedType();

        setLoading(true);
        disposableHolder.append(store.getAll(type)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onDataReceived, throwable -> onDataReceiveError(Utils.getCauseIfRuntime(throwable))));
    }

    private void onDataReceiveError(Throwable throwable) {
        setLoading(false);
        safeShowError(getView(), throwable.getMessage());
    }

    private void onDataReceived(List<LogEvent> events) {
        setLoading(false);

        this.events.clear();

        for(LogEvent event : events){
            this.events.add(new LogEventWrapper(event));
        }

        callView(ILogsView::notifyEventDataChanged);
        resolveEmptyTextVisibility();
    }

    private int getSelectedType() {
        int type = LogEvent.Type.ERROR;
        for (LogEventType t : types) {
            if (t.isActive()) {
                type = t.getType();
            }
        }

        return type;
    }

    @Override
    public void onDestroyed() {
        disposableHolder.dispose();
        super.onDestroyed();
    }

    private DisposableHolder<Integer> disposableHolder = new DisposableHolder<>();

    private static List<LogEventType> createTypes() {
        List<LogEventType> types = new ArrayList<>();
        types.add(new LogEventType(LogEvent.Type.ERROR, R.string.log_type_error).setActive(true));
        return types;
    }

    public void fireTypeClick(LogEventType entry) {
        if(getSelectedType() == entry.getType()){
            return;
        }

        for(LogEventType t : types){
            t.setActive(t.getType() == entry.getType());
        }

        callView(ILogsView::notifyTypesDataChanged);
        loadAll();
    }

    public void fireRefresh() {
        loadAll();
    }
}
