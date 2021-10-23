package dev.velaron.fennec.mvp.view;

import androidx.annotation.NonNull;

import java.util.List;

import biz.dealnote.mvp.core.IMvpView;
import dev.velaron.fennec.model.LoadMoreState;
import dev.velaron.fennec.model.Topic;
import dev.velaron.fennec.mvp.view.base.IAccountDependencyView;

/**
 * Created by admin on 13.12.2016.
 * phoenix
 */
public interface ITopicsView extends IAccountDependencyView, IMvpView, IErrorView {
    void displayData(@NonNull List<Topic> topics);
    void notifyDataSetChanged();
    void notifyDataAdd(int position, int count);
    void showRefreshing(boolean refreshing);
    void setupLoadMore(@LoadMoreState int state);

    void goToComments(int accountId, @NonNull Topic topic);
    void setButtonCreateVisible(boolean visible);
}