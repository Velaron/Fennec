package dev.velaron.fennec.mvp.presenter;

import static dev.velaron.fennec.util.Utils.getCauseIfRuntime;
import static dev.velaron.fennec.util.Utils.isEmpty;
import static dev.velaron.fennec.util.Utils.nonEmpty;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.velaron.fennec.domain.INewsfeedInteractor;
import dev.velaron.fennec.domain.InteractorFactory;
import dev.velaron.fennec.model.Comment;
import dev.velaron.fennec.model.NewsfeedComment;
import dev.velaron.fennec.mvp.presenter.base.PlaceSupportPresenter;
import dev.velaron.fennec.mvp.view.INewsfeedCommentsView;
import dev.velaron.fennec.util.AssertUtils;
import dev.velaron.fennec.util.RxUtils;

/**
 * Created by admin on 08.05.2017.
 * phoenix
 */
public class NewsfeedCommentsPresenter extends PlaceSupportPresenter<INewsfeedCommentsView> {

    private static final String TAG = NewsfeedCommentsPresenter.class.getSimpleName();

    private final List<NewsfeedComment> data;
    private String nextFrom;

    private final INewsfeedInteractor interactor;

    public NewsfeedCommentsPresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.data = new ArrayList<>();
        this.interactor = InteractorFactory.createNewsfeedInteractor();

        loadAtLast();
    }

    private boolean loadingNow;

    private void setLoadingNow(boolean loadingNow) {
        this.loadingNow = loadingNow;
        resolveLoadingView();
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveLoadingView();
    }

    private void resolveLoadingView(){
        if(isGuiResumed()){
            getView().showLoading(loadingNow);
        }
    }

    private void loadAtLast() {
        setLoadingNow(true);

        load(null);
    }

    private void load(final String startFrom){
        appendDisposable(interactor.getNewsfeedComments(getAccountId(), 10, startFrom, "post,photo,video,topic")
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(pair -> onDataReceived(startFrom, pair.getSecond(), pair.getFirst()), this::onRequestError));
    }

    private void loadNext() {
        setLoadingNow(true);

        final String startFrom = this.nextFrom;
        load(startFrom);
    }

    private void onRequestError(Throwable throwable){
        showError(getView(), getCauseIfRuntime(throwable));
        setLoadingNow(false);
    }

    private void onDataReceived(String startFrom, String newNextFrom, List<NewsfeedComment> comments){
        setLoadingNow(false);

        boolean atLast = isEmpty(startFrom);
        nextFrom = newNextFrom;

        if(atLast){
            data.clear();
            data.addAll(comments);
            callView(INewsfeedCommentsView::notifyDataSetChanged);
        } else {
            int startCount = data.size();
            data.addAll(comments);
            callView(view -> view.notifyDataAdded(startCount, comments.size()));
        }
    }

    @Override
    public void onGuiCreated(@NonNull INewsfeedCommentsView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayData(data);
    }

    private boolean canLoadMore(){
        return nonEmpty(nextFrom) && !loadingNow;
    }

    public void fireScrollToEnd() {
        if(canLoadMore()){
            loadNext();
        }
    }

    public void fireRefresh() {
        if(loadingNow){
            return;
        }

        loadAtLast();
    }

    public void fireCommentBodyClick(NewsfeedComment newsfeedComment) {
        Comment comment = newsfeedComment.getComment();
        AssertUtils.requireNonNull(comment);
        
        getView().openComments(getAccountId(), comment.getCommented(), null);
    }
}