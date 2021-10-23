package dev.velaron.fennec.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import biz.dealnote.mvp.reflect.OnGuiCreated;
import dev.velaron.fennec.R;
import dev.velaron.fennec.domain.IWallsRepository;
import dev.velaron.fennec.domain.Repository;
import dev.velaron.fennec.model.AttachmenEntry;
import dev.velaron.fennec.model.Post;
import dev.velaron.fennec.mvp.view.IRepostView;
import dev.velaron.fennec.util.RxUtils;

/**
 * Created by admin on 15.05.2017.
 * phoenix
 */
public class RepostPresenter extends AbsAttachmentsEditPresenter<IRepostView> {

    private final Post post;
    private final Integer targetGroupId;
    private boolean publishingNow;

    private final IWallsRepository walls;

    public RepostPresenter(int accountId, Post post, Integer targetGroupId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.walls = Repository.INSTANCE.getWalls();
        this.post = post;
        this.targetGroupId = targetGroupId;

        getData().add(new AttachmenEntry(false, post));
    }

    @Override
    public void onGuiCreated(@NonNull IRepostView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.setSupportedButtons(false, false, false, false, false, false);
    }

    @OnGuiCreated
    private void resolveProgressDialog() {
        if (isGuiReady()) {
            if (publishingNow) {
                getView().displayProgressDialog(R.string.please_wait, R.string.publication, false);
            } else {
                getView().dismissProgressDialog();
            }
        }
    }

    private void setPublishingNow(boolean publishingNow) {
        this.publishingNow = publishingNow;
        resolveProgressDialog();
    }

    private void onPublishError(Throwable throwable) {
        setPublishingNow(false);
        showError(getView(), throwable);
    }

    @SuppressWarnings("unused")
    private void onPublishComplete(Post post) {
        setPublishingNow(false);
        getView().goBack();
    }

    public final void fireReadyClick() {
        setPublishingNow(true);

        final int accountId = super.getAccountId();
        final String body = getTextBody();

        appendDisposable(walls.repost(accountId, post.getVkid(), post.getOwnerId(), targetGroupId, body)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onPublishComplete, this::onPublishError));
    }
}