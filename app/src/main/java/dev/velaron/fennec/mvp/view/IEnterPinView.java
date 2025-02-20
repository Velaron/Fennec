package dev.velaron.fennec.mvp.view;

import androidx.annotation.NonNull;

import dev.velaron.fennec.mvp.core.IMvpView;

/**
 * Created by ruslan.kolbasa on 30-May-16.
 * mobilebankingandroid
 */
public interface IEnterPinView extends IMvpView, IErrorView, IToastView {
    void displayPin(int[] value, int noValue);
    void sendSuccessAndClose();
    void displayErrorAnimation();
    void displayAvatarFromUrl(@NonNull String url);
    void displayDefaultAvatar();
}
