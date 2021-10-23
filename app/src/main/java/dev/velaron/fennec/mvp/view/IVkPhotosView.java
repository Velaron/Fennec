package dev.velaron.fennec.mvp.view;

import java.util.List;

import dev.velaron.fennec.mvp.core.IMvpView;
import dev.velaron.fennec.model.Photo;
import dev.velaron.fennec.model.wrappers.SelectablePhotoWrapper;
import dev.velaron.fennec.mvp.view.base.IAccountDependencyView;
import dev.velaron.fennec.upload.Upload;

/**
 * Created by Ruslan Kolbasa on 13.07.2017.
 * phoenix
 */
public interface IVkPhotosView extends IMvpView, IAccountDependencyView, IErrorView, IToolbarView {
    String ACTION_SHOW_PHOTOS = "dev.velaron.fennec.messenger.ACTION_SHOW_PHOTOS";
    String ACTION_SELECT_PHOTOS = "dev.velaron.fennec.messenger.ACTION_SELECT_PHOTOS";

    void displayData(List<SelectablePhotoWrapper> photos, List<Upload> uploads);
    void notifyDataSetChanged();
    void notifyPhotosAdded(int position, int count);

    void displayRefreshing(boolean refreshing);

    void notifyUploadAdded(int position, int count);

    void notifyUploadRemoved(int index);

    void setButtonAddVisible(boolean visible, boolean anim);

    void notifyUploadItemChanged(int index);

    void notifyUploadProgressChanged(int id, int progress);

    void displayGallery(int accountId, int albumId, int ownerId, Integer focusToId);

    void displayDefaultToolbarTitle();

    void setDrawerPhotosSelected(boolean selected);

    void returnSelectionToParent(List<Photo> selected);

    void showSelectPhotosToast();

    void startLocalPhotosSelection();

    void startLocalPhotosSelectionIfHasPermission();
}