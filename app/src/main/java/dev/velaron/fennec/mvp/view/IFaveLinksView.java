package dev.velaron.fennec.mvp.view;

import java.util.List;

import dev.velaron.fennec.mvp.core.IMvpView;
import dev.velaron.fennec.model.FaveLink;
import dev.velaron.fennec.mvp.view.base.IAccountDependencyView;

/**
 * Created by admin on 07.10.2017.
 * Phoenix-for-VK
 */
public interface IFaveLinksView extends IAccountDependencyView, IMvpView, IErrorView {
    void displayLinks(List<FaveLink> links);
    void notifyDataSetChanged();
    void notifyDataAdded(int position, int count);
    void displayRefreshing(boolean refreshing);
    void openLink(int accountId, FaveLink link);
    void notifyItemRemoved(int index);
}