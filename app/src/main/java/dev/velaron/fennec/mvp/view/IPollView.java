package dev.velaron.fennec.mvp.view;

import java.util.List;
import java.util.Set;

import biz.dealnote.mvp.core.IMvpView;
import dev.velaron.fennec.model.Poll;
import dev.velaron.fennec.mvp.view.base.IAccountDependencyView;

/**
 * Created by admin on 19.12.2016.
 * phoenix
 */
public interface IPollView extends IAccountDependencyView, IMvpView, IErrorView {
    void displayQuestion(String title);

    void displayType(boolean anonymous);

    void displayCreationTime(long unixtime);

    void displayVoteCount(int count);

    void displayVotesList(List<Poll.Answer> answers, boolean canCheck, boolean multiply, Set<Integer> checked);

    void displayLoading(boolean loading);

    void setupButton(boolean voted);
}