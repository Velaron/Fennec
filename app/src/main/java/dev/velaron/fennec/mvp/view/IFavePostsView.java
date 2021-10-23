package dev.velaron.fennec.mvp.view;

import java.util.List;

import biz.dealnote.mvp.core.IMvpView;
import dev.velaron.fennec.model.Post;
import dev.velaron.fennec.mvp.view.base.IAccountDependencyView;

/**
 * Created by Ruslan Kolbasa on 14.07.2017.
 * phoenix
 */
public interface IFavePostsView extends IAccountDependencyView, IMvpView, IErrorView, IAttachmentsPlacesView {
    void displayData(List<Post> posts);
    void notifyDataSetChanged();
    void notifyDataAdded(int position, int count);
    void showRefreshing(boolean refreshing);

    void notifyItemChanged(int index);
}