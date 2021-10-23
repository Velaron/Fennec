package dev.velaron.fennec.mvp.view;

import androidx.annotation.NonNull;

import java.util.List;

import biz.dealnote.mvp.core.IMvpView;
import dev.velaron.fennec.model.VideoAlbum;
import dev.velaron.fennec.mvp.view.base.IAccountDependencyView;

/**
 * Created by admin on 21.11.2016.
 * phoenix
 */
public interface IVideoAlbumsView extends IMvpView, IAccountDependencyView, IErrorView {

    void displayData(@NonNull List<VideoAlbum> data);
    void notifyDataAdded(int position, int count);
    void displayLoading(boolean loading);
    void notifyDataSetChanged();

    void openAlbum(int accountId, int ownerId, int albumId, String action, String title);
}
