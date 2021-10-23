package dev.velaron.fennec.mvp.view;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.mvp.core.IMvpView;
import dev.velaron.fennec.model.Photo;
import dev.velaron.fennec.mvp.view.base.IAccountDependencyView;

/**
 * Created by admin on 09.01.2017.
 * phoenix
 */
public interface IFavePhotosView extends IAccountDependencyView, IMvpView, IErrorView {
    void displayData(List<Photo> photos);
    void notifyDataSetChanged();
    void notifyDataAdded(int position, int count);
    void showRefreshing(boolean refreshing);
    void goToGallery(int accountId, ArrayList<Photo> photos, int position);
}