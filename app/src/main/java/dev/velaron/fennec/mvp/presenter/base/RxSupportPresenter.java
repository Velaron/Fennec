package dev.velaron.fennec.mvp.presenter.base;

import static dev.velaron.fennec.util.Objects.isNull;
import static dev.velaron.fennec.util.Objects.nonNull;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import dev.velaron.fennec.mvp.core.AbsPresenter;
import dev.velaron.fennec.mvp.core.IMvpView;
import dev.velaron.fennec.App;
import dev.velaron.fennec.BuildConfig;
import dev.velaron.fennec.Injection;
import dev.velaron.fennec.db.Stores;
import dev.velaron.fennec.mvp.view.IErrorView;
import dev.velaron.fennec.mvp.view.IToastView;
import dev.velaron.fennec.service.ErrorLocalizer;
import dev.velaron.fennec.util.InstancesCounter;
import dev.velaron.fennec.util.RxUtils;
import dev.velaron.fennec.util.Utils;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Created by admin on 24.09.2016.
 * phoenix
 */
public abstract class RxSupportPresenter<V extends IMvpView> extends AbsPresenter<V> {

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private static InstancesCounter instancesCounter = new InstancesCounter();

    private static final String SAVE_INSTANCE_ID = "save_instance_id";
    private static final String SAVE_TEMP_DATA_USAGE = "save_temp_data_usage";

    private final int instanceId;

    public RxSupportPresenter(@Nullable Bundle savedInstanceState) {
        super(savedInstanceState);

        if (nonNull(savedInstanceState)) {
            instanceId = savedInstanceState.getInt(SAVE_INSTANCE_ID);
            instancesCounter.fireExists(getClass(), instanceId);
            tempDataUsage = savedInstanceState.getBoolean(SAVE_TEMP_DATA_USAGE);
        } else {
            instanceId = instancesCounter.incrementAndGet(getClass());
        }
    }

    private boolean tempDataUsage;

    protected void fireTempDataUsage() {
        this.tempDataUsage = true;
    }

    private int viewCreationCounter;

    @Override
    public void onGuiCreated(@NonNull V view) {
        viewCreationCounter++;
        super.onGuiCreated(view);
    }

    public int getViewCreationCount() {
        return viewCreationCounter;
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putInt(SAVE_INSTANCE_ID, instanceId);
        outState.putBoolean(SAVE_TEMP_DATA_USAGE, tempDataUsage);
    }

    protected int getInstanceId() {
        return instanceId;
    }

    @Override
    public void onDestroyed() {
        compositeDisposable.dispose();

        if (tempDataUsage) {
            RxUtils.subscribeOnIOAndIgnore(Stores.getInstance()
                    .tempStore()
                    .delete(getInstanceId()));

            tempDataUsage = false;
        }

        super.onDestroyed();
    }

    protected void appendDisposable(@NonNull Disposable disposable) {
        compositeDisposable.add(disposable);
    }

    protected void showError(IErrorView view, Throwable throwable) {
        if (isNull(view)) {
            return;
        }

        throwable = Utils.getCauseIfRuntime(throwable);

        if(BuildConfig.DEBUG){
            throwable.printStackTrace();
        }

        view.showError(ErrorLocalizer.localizeThrowable(getApplicationContext(), throwable));
    }

    protected void safeShowError(IErrorView view, @StringRes int text, Object... params) {
        if (isGuiReady()) {
            view.showError(text, params);
        }
    }

    protected void safeShowLongToast(IToastView view, @StringRes int text, Object... params) {
        safeShowToast(view, text, true, params);
    }


    protected void safeShowToast(IToastView view, @StringRes int text, boolean isLong, Object... params) {
        if (nonNull(view)) {
            view.showToast(text, isLong, params);
        }
    }

    protected static void safeShowError(IErrorView view, String text) {
        if (nonNull(view)) {
            view.showError(text);
        }
    }

    protected Context getApplicationContext() {
        return Injection.provideApplicationContext();
    }

    protected String getString(@StringRes int res) {
        return App.getInstance().getString(res);
    }

    protected String getString(@StringRes int res, Object... params) {
        return App.getInstance().getString(res, params);
    }
}