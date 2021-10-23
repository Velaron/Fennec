package dev.velaron.fennec.mvp.view;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.mvp.core.IMvpView;
import dev.velaron.fennec.model.DocFilter;
import dev.velaron.fennec.model.Document;
import dev.velaron.fennec.mvp.view.base.IAccountDependencyView;
import dev.velaron.fennec.upload.Upload;

/**
 * Created by admin on 25.12.2016.
 * phoenix
 */
public interface IDocListView extends IAccountDependencyView, IMvpView, IErrorView {

    void displayData(List<Document> documents, boolean asImages);
    void showRefreshing(boolean refreshing);

    void notifyDataSetChanged();
    void notifyDataAdd(int position, int count);

    void openDocument(int accountId, @NonNull Document document);
    void returnSelection(ArrayList<Document> docs);

    void goToGifPlayer(int accountId, @NonNull ArrayList<Document> gifs, int selected);

    void requestReadExternalStoragePermission();

    void startSelectUploadFileActivity(int accountId);

    void setUploadDataVisible(boolean visible);
    void displayUploads(List<Upload> data);
    void notifyUploadDataChanged();
    void notifyUploadItemsAdded(int position, int count);
    void notifyUploadItemChanged(int position);
    void notifyUploadItemRemoved(int position);
    void notifyUploadProgressChanged(int position, int progress, boolean smoothly);

    void displayFilterData(List<DocFilter> filters);

    void notifyFiltersChanged();

    void setAdapterType(boolean imagesOnly);
}
