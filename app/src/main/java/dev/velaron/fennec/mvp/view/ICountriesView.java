package dev.velaron.fennec.mvp.view;

import java.util.List;

import biz.dealnote.mvp.core.IMvpView;
import dev.velaron.fennec.model.database.Country;

/**
 * Created by Ruslan Kolbasa on 20.09.2017.
 * phoenix
 */
public interface ICountriesView extends IMvpView, IErrorView {
    void displayData(List<Country> countries);
    void notifyDataSetChanged();
    void displayLoading(boolean loading);

    void returnSelection(Country country);
}