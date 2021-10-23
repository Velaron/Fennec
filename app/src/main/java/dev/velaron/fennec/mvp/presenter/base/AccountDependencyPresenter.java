package dev.velaron.fennec.mvp.presenter.base;

import android.os.Bundle;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import dev.velaron.fennec.mvp.core.IMvpView;
import dev.velaron.fennec.Injection;
import dev.velaron.fennec.mvp.view.base.IAccountDependencyView;
import dev.velaron.fennec.settings.Settings;

/**
 * Created by admin on 24.09.2016.
 * phoenix
 */
public abstract class AccountDependencyPresenter<V extends IMvpView & IAccountDependencyView> extends RxSupportPresenter<V> {

    private static final String SAVE_ACCOUNT_ID = "save_account_id";
    private static final String SAVE_INVALID_ACCOUNT_CONTEXT = "save_invalid_account_context";

    private int mAccountId;
    private boolean mSupportAccountHotSwap;
    private boolean mInvalidAccountContext;

    public AccountDependencyPresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(savedInstanceState);
        if (savedInstanceState != null) {
            mAccountId = savedInstanceState.getInt(SAVE_ACCOUNT_ID);
            mInvalidAccountContext = savedInstanceState.getBoolean(SAVE_INVALID_ACCOUNT_CONTEXT);
        } else {
            mAccountId = accountId;
        }

        appendDisposable(Settings.get()
                .accounts()
                .observeChanges()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onAccountChange));
    }

    private void onAccountChange(int newAccountId) {
        int oldAccountId = mAccountId;
        if (oldAccountId == newAccountId) {
            mInvalidAccountContext = false;
            return;
        }

        if (mSupportAccountHotSwap) {
            beforeAccountChange(oldAccountId, newAccountId);
            mAccountId = newAccountId;
            afterAccountChange(oldAccountId, newAccountId);
        } else {
            mInvalidAccountContext = true;

            if (isGuiReady()) {
                getView().displayAccountNotSupported();
            }
        }
    }

    @Override
    public void onGuiCreated(@NonNull V view) {
        super.onGuiCreated(view);

        if (mInvalidAccountContext) {
            view.displayAccountNotSupported();
        }
    }

    @CallSuper
    protected void afterAccountChange(int oldAccountId, int newAccountId) {

    }

    @CallSuper
    protected void beforeAccountChange(int oldAccountId, int newAccountId) {

    }

    protected int getAccountId() {
        return mAccountId;
    }

    @Override
    public void onDestroyed() {
        super.onDestroyed();
    }

    protected void setSupportAccountHotSwap(boolean supportAccountHotSwap) {
        this.mSupportAccountHotSwap = supportAccountHotSwap;
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putInt(SAVE_ACCOUNT_ID, mAccountId);
        outState.putBoolean(SAVE_INVALID_ACCOUNT_CONTEXT, mInvalidAccountContext);
    }
}