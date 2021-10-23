package dev.velaron.fennec.mvp.view;

import androidx.annotation.NonNull;

import java.util.List;

import biz.dealnote.mvp.core.IMvpView;
import dev.velaron.fennec.model.Video;
import dev.velaron.fennec.mvp.view.base.IAccountDependencyView;

/**
 * Created by admin on 21.11.2016.
 * phoenix
 */
public interface IVideosListView extends IAccountDependencyView, IMvpView, IToolbarView, IErrorView {

    String ACTION_SELECT = "VideosFragment.ACTION_SELECT";
    String ACTION_SHOW = "VideosFragment.ACTION_SHOW";

    void displayData(@NonNull List<Video> data);
    void notifyDataAdded(int position, int count);
    void displayLoading(boolean loading);

    void notifyDataSetChanged();

    void returnSelectionToParent(Video video);

    void showVideoPreview(int accountId, Video video);
}
