package dev.velaron.fennec.mvp.view;

import androidx.annotation.StringRes;

/**
 * Created by admin on 21.12.2016.
 * phoenix
 */
public interface IProgressView {
    void displayProgressDialog(@StringRes int title, @StringRes int message, boolean cancelable);
    void dismissProgressDialog();
}
