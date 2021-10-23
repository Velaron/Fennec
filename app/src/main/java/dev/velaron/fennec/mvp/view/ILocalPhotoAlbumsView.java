package dev.velaron.fennec.mvp.view;

import androidx.annotation.NonNull;

import java.util.List;

import dev.velaron.fennec.mvp.core.IMvpView;
import dev.velaron.fennec.model.LocalImageAlbum;

/**
 * Created by admin on 03.10.2016.
 * phoenix
 */
public interface ILocalPhotoAlbumsView extends IMvpView {

    void displayData(@NonNull List<LocalImageAlbum> data);
    void setEmptyTextVisible(boolean visible);
    void displayProgress(boolean loading);
    void openAlbum(@NonNull LocalImageAlbum album);
    void notifyDataChanged();

    void requestReadExternalStoragePermission();
}
