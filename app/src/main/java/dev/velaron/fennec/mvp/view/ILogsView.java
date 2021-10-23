package dev.velaron.fennec.mvp.view;

import java.util.List;

import dev.velaron.fennec.mvp.core.IMvpView;
import dev.velaron.fennec.model.LogEventType;
import dev.velaron.fennec.model.LogEventWrapper;

/**
 * Created by Ruslan Kolbasa on 26.04.2017.
 * phoenix
 */
public interface ILogsView extends IMvpView, IErrorView {

    void displayTypes(List<LogEventType> types);

    void displayData(List<LogEventWrapper> events);

    void showRefreshing(boolean refreshing);

    void notifyEventDataChanged();

    void notifyTypesDataChanged();

    void setEmptyTextVisible(boolean visible);
}
