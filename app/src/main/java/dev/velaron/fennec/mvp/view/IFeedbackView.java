package dev.velaron.fennec.mvp.view;

import androidx.annotation.NonNull;

import java.util.List;

import dev.velaron.fennec.mvp.core.IMvpView;
import dev.velaron.fennec.model.LoadMoreState;
import dev.velaron.fennec.model.feedback.Feedback;
import dev.velaron.fennec.mvp.view.base.IAccountDependencyView;

/**
 * Created by admin on 11.12.2016.
 * phoenix
 */
public interface IFeedbackView extends IAccountDependencyView, IMvpView, IAttachmentsPlacesView, IErrorView {
    void displayData(List<Feedback> data);
    void showLoading(boolean loading);
    void notifyDataAdding(int position, int count);
    void notifyDataSetChanged();
    void configLoadMore(@LoadMoreState int loadmoreState);
    void showLinksDialog(int accountId, @NonNull Feedback notification);
}
