package dev.velaron.fennec.mvp.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import dev.velaron.fennec.fragment.search.SearchContentType;
import dev.velaron.fennec.fragment.search.criteria.BaseSearchCriteria;
import dev.velaron.fennec.model.Audio;
import dev.velaron.fennec.model.Commented;
import dev.velaron.fennec.model.Document;
import dev.velaron.fennec.model.Link;
import dev.velaron.fennec.model.Message;
import dev.velaron.fennec.model.Peer;
import dev.velaron.fennec.model.Photo;
import dev.velaron.fennec.model.Poll;
import dev.velaron.fennec.model.Post;
import dev.velaron.fennec.model.Video;
import dev.velaron.fennec.model.WikiPage;

/**
 * Created by admin on 03.10.2016.
 * phoenix
 */
public interface IAttachmentsPlacesView {

    void openChatWith(int accountId, int messagesOwnerId, @NonNull Peer peer);

    void openLink(int accountId, @NonNull Link link);

    void openWikiPage(int accountId, @NonNull WikiPage page);

    void openSimplePhotoGallery(int accountId, @NonNull ArrayList<Photo> photos, int index, boolean needUpdate);

    void openPost(int accountId, @NonNull Post post);

    void openDocPreview(int accountId, @NonNull Document document);

    void openOwnerWall(int accountId, int ownerId);

    void openForwardMessages(int accountId, @NonNull ArrayList<Message> messages);

    void playAudioList(int accountId, int position, @NonNull ArrayList<Audio> apiAudio);

    void openVideo(int accountId, @NonNull Video apiVideo);

    void openPoll(int accoountId, @NonNull Poll apiPoll);

    void openSearch(int accountId, @SearchContentType int type, @Nullable BaseSearchCriteria criteria);

    void openComments(int accountId, Commented commented, Integer focusToCommentId);

    void goToLikes(int accountId, String type, int ownerId, int id);

    void goToReposts(int accountId, String type, int ownerId, int id);

    void repostPost(int accountId, @NonNull Post post);
}
