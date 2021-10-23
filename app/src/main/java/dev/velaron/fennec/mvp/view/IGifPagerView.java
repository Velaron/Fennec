package dev.velaron.fennec.mvp.view;

import androidx.annotation.StringRes;

import dev.velaron.fennec.media.gif.IGifPlayer;

/**
 * Created by ruslan.kolbasa on 11.10.2016.
 * phoenix
 */
public interface IGifPagerView extends IBasicDocumentView, IErrorView {

    void displayData(int pageCount, int selectedIndex);
    void setAspectRatioAt(int position, int w, int h);
    void setPreparingProgressVisible(int position, boolean preparing);
    void setupAddRemoveButton(boolean addEnable);

    void attachDisplayToPlayer(int adapterPosition, IGifPlayer gifPlayer);
    void setToolbarTitle(@StringRes int titleRes, Object ... params);
    void setToolbarSubtitle(@StringRes int titleRes, Object ... params);
    void configHolder(int adapterPosition, boolean progress, int aspectRatioW, int aspectRatioH);
}