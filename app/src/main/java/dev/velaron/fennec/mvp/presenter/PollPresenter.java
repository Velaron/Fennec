package dev.velaron.fennec.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashSet;
import java.util.Set;

import dev.velaron.fennec.mvp.reflect.OnGuiCreated;
import dev.velaron.fennec.R;
import dev.velaron.fennec.domain.IPollInteractor;
import dev.velaron.fennec.domain.InteractorFactory;
import dev.velaron.fennec.model.Poll;
import dev.velaron.fennec.mvp.presenter.base.AccountDependencyPresenter;
import dev.velaron.fennec.mvp.view.IPollView;
import dev.velaron.fennec.util.RxUtils;

/**
 * Created by admin on 19.12.2016.
 * phoenix
 */
public class PollPresenter extends AccountDependencyPresenter<IPollView> {

    private Poll mPoll;
    private Set<Integer> mTempCheckedId;

    private final IPollInteractor pollInteractor;

    public PollPresenter(int accountId, @NonNull Poll poll, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.mPoll = poll;
        this.mTempCheckedId = arrayToSet(poll.getMyAnswerIds());
        this.pollInteractor = InteractorFactory.createPollInteractor();

        refreshPollData();
    }

    private boolean loadingNow;

    private void setLoadingNow(boolean loadingNow) {
        this.loadingNow = loadingNow;
        resolveButtonView();
    }

    private void refreshPollData() {
        if (loadingNow) return;

        final int accountId = super.getAccountId();

        setLoadingNow(true);
        appendDisposable(pollInteractor.getPollById(accountId, mPoll.getOwnerId(), mPoll.getId(), mPoll.isBoard())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onPollInfoUpdated, this::onLoadingError));
    }

    private void onLoadingError(Throwable t) {
        showError(getView(), t);
        setLoadingNow(false);
    }

    private static Set<Integer> arrayToSet(int[] ids){
        Set<Integer> set = new HashSet<>(ids.length);
        for(int id : ids){
            set.add(id);
        }
        return set;
    }

    private void onPollInfoUpdated(Poll poll) {
        mPoll = poll;
        mTempCheckedId = arrayToSet(poll.getMyAnswerIds());

        setLoadingNow(false);

        resolveQuestionView();
        resolveVotesCountView();
        resolvePollTypeView();
        resolveVotesListView();
    }

    @OnGuiCreated
    private void resolveButtonView() {
        if (isGuiReady()) {
            getView().displayLoading(loadingNow);
            getView().setupButton(isVoted());
        }
    }

    @OnGuiCreated
    private void resolveVotesListView() {
        if (isGuiReady()) {
            getView().displayVotesList(mPoll.getAnswers(), !isVoted(), mPoll.isMultiple(), mTempCheckedId);
        }
    }

    @OnGuiCreated
    private void resolveVotesCountView() {
        if (isGuiReady()) {
            getView().displayVoteCount(mPoll.getVoteCount());
        }
    }

    @OnGuiCreated
    private void resolvePollTypeView() {
        if (isGuiReady()) {
            getView().displayType(mPoll.isAnonymous());
        }
    }

    @OnGuiCreated
    private void resolveQuestionView() {
        if (isGuiReady()) {
            getView().displayQuestion(mPoll.getQuestion());
        }
    }

    @OnGuiCreated
    private void resolveCreationTimeView() {
        if (isGuiReady()) {
            getView().displayCreationTime(mPoll.getCreationTime());
        }
    }

    public void fireVoteChecked(Set<Integer> newid) {
        mTempCheckedId = newid;
    }

    private void vote() {
        if (loadingNow) return;

        final int accountId = super.getAccountId();
        final Set<Integer> voteIds = new HashSet<>(mTempCheckedId);

        setLoadingNow(true);
        appendDisposable(pollInteractor.addVote(accountId, mPoll, voteIds)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onPollInfoUpdated, this::onLoadingError));
    }

    private boolean isVoted(){
        return mPoll.getMyAnswerIds() != null && mPoll.getMyAnswerIds().length > 0;
    }

    public void fireButtonClick() {
        if (loadingNow) return;

        if(isVoted()){
            removeVote();
        } else {
            if (mTempCheckedId.isEmpty()) {
                getView().showError(R.string.select);
            } else {
                vote();
            }
        }
    }

    private void removeVote() {
        final int accountId = super.getAccountId();
        final int answerId = mPoll.getMyAnswerIds()[0];

        setLoadingNow(true);
        appendDisposable(pollInteractor.removeVote(accountId, mPoll, answerId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onPollInfoUpdated, this::onLoadingError));
    }
}