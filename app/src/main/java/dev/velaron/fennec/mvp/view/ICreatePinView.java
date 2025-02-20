package dev.velaron.fennec.mvp.view;

import androidx.annotation.StringRes;

import dev.velaron.fennec.mvp.core.IMvpView;

/**
 * Created by ruslan.kolbasa on 10-Jun-16.
 * mobilebankingandroid
 */
public interface ICreatePinView extends IMvpView, IErrorView {
    void displayTitle(@StringRes int titleRes);
    void displayErrorAnimation();
    void displayPin(int[] value, int noValue);
    void sendSkipAndClose();
    void sendSuccessAndClose(int[] values);
}
