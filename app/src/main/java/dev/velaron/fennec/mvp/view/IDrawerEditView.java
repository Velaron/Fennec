package dev.velaron.fennec.mvp.view;

import java.util.List;

import dev.velaron.fennec.mvp.core.IMvpView;
import dev.velaron.fennec.model.DrawerCategory;

/**
 * Created by Ruslan Kolbasa on 20.07.2017.
 * phoenix
 */
public interface IDrawerEditView extends IMvpView {
    void displayData(List<DrawerCategory> data);
    void goBackAndApplyChanges();
}