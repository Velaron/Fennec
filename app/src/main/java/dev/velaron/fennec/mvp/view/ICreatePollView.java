package dev.velaron.fennec.mvp.view;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import dev.velaron.fennec.mvp.core.IMvpView;
import dev.velaron.fennec.model.Poll;
import dev.velaron.fennec.mvp.view.base.IAccountDependencyView;

/**
 * Created by admin on 20.12.2016.
 * phoenix
 */
public interface ICreatePollView extends IAccountDependencyView, IMvpView, IProgressView, IErrorView {
    void displayQuestion(String question);

    void setAnonymous(boolean anomymous);

    void displayOptions(String[] options);

    void showQuestionError(@StringRes int message);

    void showOptionError(int index, @StringRes int message);

    void sendResultAndGoBack(@NonNull Poll poll);

    void setMultiply(boolean multiply);
}
