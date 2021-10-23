package dev.velaron.fennec.mvp.view;

import java.util.List;

import dev.velaron.fennec.mvp.core.IMvpView;
import dev.velaron.fennec.model.Audio;
import dev.velaron.fennec.model.AudioFilter;
import dev.velaron.fennec.mvp.view.base.IAccountDependencyView;

/**
 * Created by admin on 1/4/2018.
 * Phoenix-for-VK
 */
public interface IAudiosView extends IMvpView, IErrorView, IAccountDependencyView {
    void fillFilters(List<AudioFilter> sources);
    void displayList(List<Audio> audios);
    void notifyListChanged();

    void notifyFilterListChanged();
    void displayRefreshing(boolean refresing);
    void setBlockedScreen(boolean visible);

    void showFilters(boolean canFilter);
}