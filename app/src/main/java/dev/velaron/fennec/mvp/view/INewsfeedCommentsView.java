package dev.velaron.fennec.mvp.view;

import java.util.List;

import biz.dealnote.mvp.core.IMvpView;
import dev.velaron.fennec.model.NewsfeedComment;
import dev.velaron.fennec.mvp.view.base.IAccountDependencyView;

/**
 * Created by admin on 08.05.2017.
 * phoenix
 */
public interface INewsfeedCommentsView extends IAccountDependencyView, IAttachmentsPlacesView, IMvpView, IErrorView {
    void displayData(List<NewsfeedComment> data);
    void notifyDataAdded(int position, int count);
    void notifyDataSetChanged();
    void showLoading(boolean loading);
}
