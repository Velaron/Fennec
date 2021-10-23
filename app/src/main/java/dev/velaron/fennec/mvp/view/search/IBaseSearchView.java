package dev.velaron.fennec.mvp.view.search;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.mvp.core.IMvpView;
import dev.velaron.fennec.fragment.search.options.BaseOption;
import dev.velaron.fennec.mvp.view.IAttachmentsPlacesView;
import dev.velaron.fennec.mvp.view.IErrorView;
import dev.velaron.fennec.mvp.view.base.IAccountDependencyView;

/**
 * Created by admin on 01.05.2017.
 * phoenix
 */
public interface IBaseSearchView<T> extends IMvpView, IErrorView, IAccountDependencyView, IAttachmentsPlacesView {
    void displayData(List<T> data);

    void setEmptyTextVisible(boolean visible);

    void notifyDataSetChanged();

    void notifyItemChanged(int index);

    void notifyDataAdded(int position, int count);

    void showLoading(boolean loading);

    void displaySearchQuery(String query);

    void displayFilter(int accountId, ArrayList<BaseOption> options);
}