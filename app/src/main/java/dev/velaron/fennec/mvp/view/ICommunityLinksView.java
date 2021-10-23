package dev.velaron.fennec.mvp.view;

import java.util.List;

import biz.dealnote.mvp.core.IMvpView;
import dev.velaron.fennec.api.model.VKApiCommunity;
import dev.velaron.fennec.mvp.view.base.IAccountDependencyView;

/**
 * Created by admin on 13.06.2017.
 * phoenix
 */
public interface ICommunityLinksView extends IAccountDependencyView, IErrorView, IMvpView {

    void displayRefreshing(boolean loadingNow);

    void notifyDataSetChanged();

    void displayData(List<VKApiCommunity.Link> links);

    void openLink(String link);
}
