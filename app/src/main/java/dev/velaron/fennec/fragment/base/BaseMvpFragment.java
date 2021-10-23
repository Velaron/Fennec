package dev.velaron.fennec.fragment.base;

import android.app.ProgressDialog;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import dev.velaron.fennec.activity.ActivityUtils;
import dev.velaron.fennec.mvp.view.IErrorView;
import dev.velaron.fennec.mvp.view.IProgressView;
import dev.velaron.fennec.mvp.view.IToastView;
import dev.velaron.fennec.mvp.view.IToolbarView;
import dev.velaron.fennec.mvp.view.base.IAccountDependencyView;
import dev.velaron.fennec.util.Utils;
import dev.velaron.fennec.util.ViewUtils;
import dev.velaron.fennec.mvp.compat.AbsMvpFragment;
import dev.velaron.fennec.mvp.core.AbsPresenter;
import dev.velaron.fennec.mvp.core.IMvpView;

import static dev.velaron.fennec.util.Objects.nonNull;

/**
 * Created by ruslan.kolbasa on 11.10.2016.
 * phoenix
 */
public abstract class BaseMvpFragment<P extends AbsPresenter<V>, V extends IMvpView>
        extends AbsMvpFragment<P, V> implements IMvpView, IAccountDependencyView, IProgressView, IErrorView, IToastView, IToolbarView {

    public static final String EXTRA_HIDE_TOOLBAR = "extra_hide_toolbar";

    protected boolean hasHideToolbarExtra(){
        return nonNull(getArguments()) && getArguments().getBoolean(EXTRA_HIDE_TOOLBAR);
    }

    @Override
    public void showToast(@StringRes int titleTes, boolean isLong, Object... params) {
        if(isAdded()){
            Toast.makeText(requireActivity(), getString(titleTes, params), isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showError(String text) {
        if(isAdded()){
            Utils.showRedTopToast(requireActivity(), text);
        }
    }

    @Override
    public void showError(@StringRes int titleTes, Object... params) {
        if(isAdded()){
            showError(getString(titleTes, params));
        }
    }

    @Override
    public void setToolbarSubtitle(String subtitle) {
        ActivityUtils.setToolbarSubtitle(this, subtitle);
    }

    @Override
    public void setToolbarTitle(String title) {
        ActivityUtils.setToolbarTitle(this, title);
    }

    @Override
    public void displayAccountNotSupported() {
        // TODO: 18.12.2017
    }

    @Override
    public void displayAccountSupported() {
        // TODO: 18.12.2017
    }

    protected static void safelySetCheched(CompoundButton button, boolean checked){
        if(nonNull(button)){
            button.setChecked(checked);
        }
    }

    protected static void safelySetText(TextView target, String text){
        if(nonNull(target)){
            target.setText(text);
        }
    }

    protected static void safelySetText(TextView target, @StringRes int text){
        if(nonNull(target)){
            target.setText(text);
        }
    }

    protected static void safelySetVisibleOrGone(View target, boolean visible){
        if(nonNull(target)){
            target.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    protected void styleSwipeRefreshLayoutWithCurrentTheme(@NonNull SwipeRefreshLayout swipeRefreshLayout, boolean needToolbarOffset) {
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), swipeRefreshLayout, needToolbarOffset);
    }

    private ProgressDialog mLoadingProgressDialog;

    @Override
    public void displayProgressDialog(@StringRes int title, @StringRes int message, boolean cancelable) {
        dismissProgressDialog();

        mLoadingProgressDialog = new ProgressDialog(requireActivity());
        mLoadingProgressDialog.setTitle(title);
        mLoadingProgressDialog.setMessage(getString(message));
        mLoadingProgressDialog.setCancelable(cancelable);
        mLoadingProgressDialog.show();
    }

    @Override
    public void dismissProgressDialog() {
        if (nonNull(mLoadingProgressDialog)) {
            if (mLoadingProgressDialog.isShowing()) {
                mLoadingProgressDialog.cancel();
            }
        }
    }
}
