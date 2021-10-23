package dev.velaron.fennec.mvp.view.base;

import androidx.annotation.NonNull;

import biz.dealnote.mvp.core.IMvpView;
import dev.velaron.fennec.view.steppers.base.AbsStepsHost;

/**
 * Created by ruslan.kolbasa on 30.11.2016.
 * phoenix
 */
public interface ISteppersView<H extends AbsStepsHost> extends IMvpView {
    void updateStepView(int step);
    void moveSteppers(int from, int to);
    void goBack();
    void hideKeyboard();
    void updateStepButtonsAvailability(int step);
    void attachSteppersHost(@NonNull H mHost);
}
