package dev.velaron.fennec.mvp.view;

import androidx.annotation.StringRes;

/**
 * Created by admin on 21.03.2017.
 * phoenix
 */
public interface IErrorView {
    void showError(String errorText);
    void showError(@StringRes int titleTes, Object... params);
}
