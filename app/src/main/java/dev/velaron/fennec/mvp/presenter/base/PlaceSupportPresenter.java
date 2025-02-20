package dev.velaron.fennec.mvp.presenter.base;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import dev.velaron.fennec.mvp.core.IMvpView;
import dev.velaron.fennec.domain.ILikesInteractor;
import dev.velaron.fennec.fragment.search.SearchContentType;
import dev.velaron.fennec.fragment.search.criteria.NewsFeedCriteria;
import dev.velaron.fennec.model.Audio;
import dev.velaron.fennec.model.Commented;
import dev.velaron.fennec.model.Document;
import dev.velaron.fennec.model.Link;
import dev.velaron.fennec.model.Message;
import dev.velaron.fennec.model.Photo;
import dev.velaron.fennec.model.Poll;
import dev.velaron.fennec.model.Post;
import dev.velaron.fennec.model.Video;
import dev.velaron.fennec.model.WikiPage;
import dev.velaron.fennec.mvp.view.IAttachmentsPlacesView;
import dev.velaron.fennec.mvp.view.base.IAccountDependencyView;

/**
 * Created by ruslan.kolbasa on 04.10.2016.
 * phoenix
 */
public abstract class PlaceSupportPresenter<V extends IMvpView & IAttachmentsPlacesView & IAccountDependencyView>
        extends AccountDependencyPresenter<V> {

    public PlaceSupportPresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
    }

    public void fireLinkClick(@NonNull Link link){
        getView().openLink(getAccountId(), link);
    }

    public void fireWikiPageClick(@NonNull WikiPage page){
        getView().openWikiPage(getAccountId(), page);
    }

    public void firePhotoClick(@NonNull ArrayList<Photo> photos, int index){
        getView().openSimplePhotoGallery(getAccountId(), photos, index, true);
    }

    public void firePostClick(@NonNull Post post){
        getView().openPost(getAccountId(), post);
    }

    public void fireDocClick(@NonNull Document document){
        getView().openDocPreview(getAccountId(), document);
    }

    public void fireOwnerClick(int ownerId){
        getView().openOwnerWall(getAccountId(), ownerId);
    }

    public void fireForwardMessagesClick(@NonNull ArrayList<Message> messages){
        getView().openForwardMessages(getAccountId(), messages);
    }

    public void fireAudioPlayClick(int position, @NonNull ArrayList<Audio> apiAudio){
        getView().playAudioList(getAccountId(), position, apiAudio);
    }

    public void fireVideoClick(@NonNull Video apiVideo){
        getView().openVideo(getAccountId(), apiVideo);
    }

    public void firePollClick(@NonNull Poll poll){
        getView().openPoll(getAccountId(), poll);
    }

    public void fireHashtagClick(String hashTag) {
        getView().openSearch(getAccountId(), SearchContentType.NEWS, new NewsFeedCriteria(hashTag));
    }

    public void fireShareClick(Post post) {
        getView().repostPost(getAccountId(), post);
    }

    public void fireCommentsClick(Post post) {
        getView().openComments(getAccountId(), Commented.from(post), null);
    }

    public final void fireCopiesLikesClick(String type, int ownerId, int itemId, String filter){
        if(ILikesInteractor.FILTER_LIKES.equals(filter)){
            getView().goToLikes(getAccountId(), type, ownerId, itemId);
        } else if(ILikesInteractor.FILTER_COPIES.equals(filter)){
            getView().goToReposts(getAccountId(), type, ownerId, itemId);
        }
    }
}