package dev.velaron.fennec.mvp.presenter;

import static dev.velaron.fennec.util.Objects.isNull;
import static dev.velaron.fennec.util.Utils.getCauseIfRuntime;
import static dev.velaron.fennec.util.Utils.safeIsEmpty;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.mvp.reflect.OnGuiCreated;
import dev.velaron.fennec.R;
import dev.velaron.fennec.domain.IPollInteractor;
import dev.velaron.fennec.domain.InteractorFactory;
import dev.velaron.fennec.model.Poll;
import dev.velaron.fennec.mvp.presenter.base.AccountDependencyPresenter;
import dev.velaron.fennec.mvp.view.ICreatePollView;
import dev.velaron.fennec.util.RxUtils;

/**
 * Created by admin on 20.12.2016.
 * phoenix
 */
public class CreatePollPresenter extends AccountDependencyPresenter<ICreatePollView> {

    private String mQuestion;
    private String[] mOptions;
    private int mOwnerId;
    private boolean mAnonymous;
    private boolean mMultiply;

    private final IPollInteractor pollInteractor;

    public CreatePollPresenter(int accountId, int ownerId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.mOwnerId = ownerId;
        this.pollInteractor = InteractorFactory.createPollInteractor();

        if (isNull(savedInstanceState)) {
            mOptions = new String[10];
        }
    }

    @Override
    public void onGuiCreated(@NonNull ICreatePollView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayQuestion(mQuestion);
        viewHost.setAnonymous(mAnonymous);
        viewHost.setMultiply(mMultiply);
        viewHost.displayOptions(mOptions);
    }

    private boolean creationNow;

    private void setCreationNow(boolean creationNow) {
        this.creationNow = creationNow;
        resolveProgressDialog();
    }

    private void create() {
        if (safeIsEmpty(mQuestion)) {
            getView().showQuestionError(R.string.field_is_required);
            return;
        }

        List<String> nonEmptyOptions = new ArrayList<>();
        for (String o : mOptions) {
            if (!safeIsEmpty(o)) {
                nonEmptyOptions.add("\"" + o + "\"");
            }
        }

        if (nonEmptyOptions.isEmpty()) {
            getView().showOptionError(0, R.string.field_is_required);
            return;
        }

        setCreationNow(true);
        final int accountId = super.getAccountId();

        appendDisposable(pollInteractor.createPoll(accountId, mQuestion, mAnonymous, mMultiply, mOwnerId, nonEmptyOptions)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onPollCreated, this::onPollCreateError));
    }

    private void onPollCreateError(Throwable t) {
        setCreationNow(false);
        showError(getView(), getCauseIfRuntime(t));
    }

    private void onPollCreated(Poll poll) {
        setCreationNow(false);
        callView(view -> view.sendResultAndGoBack(poll));
    }

    @OnGuiCreated
    private void resolveProgressDialog() {
        if (isGuiReady()) {
            if (creationNow) {
                getView().displayProgressDialog(R.string.please_wait, R.string.publication, false);
            } else {
                getView().dismissProgressDialog();
            }
        }
    }

    public void fireQuestionEdited(CharSequence text) {
        mQuestion = isNull(text) ? null : text.toString();
    }

    public void fireOptionEdited(int index, CharSequence s) {
        mOptions[index] = isNull(s) ? null : s.toString();
    }

    public void fireAnonyamousChecked(boolean b) {
        mAnonymous = b;
    }

    public void fireDoneClick() {
        create();
    }

    public void fireMultiplyChecked(boolean isChecked) {
        mMultiply = isChecked;
    }
}