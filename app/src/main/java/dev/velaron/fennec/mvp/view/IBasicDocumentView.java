package dev.velaron.fennec.mvp.view;

import androidx.annotation.NonNull;

import biz.dealnote.mvp.core.IMvpView;
import dev.velaron.fennec.model.Document;
import dev.velaron.fennec.mvp.view.base.IAccountDependencyView;

/**
 * Created by ruslan.kolbasa on 11.10.2016.
 * phoenix
 */
public interface IBasicDocumentView extends IMvpView, IAccountDependencyView, IToastView, IErrorView {

    void shareDocument(int accountId, @NonNull Document document);
    void requestWriteExternalStoragePermission();

}
