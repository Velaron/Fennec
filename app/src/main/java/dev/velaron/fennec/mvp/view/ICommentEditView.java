package dev.velaron.fennec.mvp.view;

import androidx.annotation.Nullable;

import dev.velaron.fennec.model.Comment;

/**
 * Created by admin on 06.05.2017.
 * phoenix
 */
public interface ICommentEditView extends IBaseAttachmentsEditView, IProgressView {
    void goBackWithResult(@Nullable Comment comment);
    void showConfirmWithoutSavingDialog();

    void goBack();
}
