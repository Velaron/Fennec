package dev.velaron.fennec.mvp.view;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.mvp.core.IMvpView;
import dev.velaron.fennec.model.LocalPhoto;

/**
 * Created by admin on 03.10.2016.
 * phoenix
 */
public interface ILocalPhotosView extends IMvpView, IErrorView {
    void displayData(@NonNull List<LocalPhoto> data);
    void setEmptyTextVisible(boolean visible);
    void displayProgress(boolean loading);
    void returnResultToParent(ArrayList<LocalPhoto> photos);
    void updateSelectionAndIndexes();
    void setFabVisible(boolean visible, boolean anim);
}
