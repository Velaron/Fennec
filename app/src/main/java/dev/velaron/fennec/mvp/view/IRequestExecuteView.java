package dev.velaron.fennec.mvp.view;

import dev.velaron.fennec.mvp.core.IMvpView;
import dev.velaron.fennec.mvp.view.base.IAccountDependencyView;

/**
 * Created by Ruslan Kolbasa on 05.07.2017.
 * phoenix
 */
public interface IRequestExecuteView extends IMvpView, IErrorView, IProgressView, IAccountDependencyView, IToastView {
    void displayBody(String body);
    void hideKeyboard();
    void requestWriteExternalStoragePermission();
}