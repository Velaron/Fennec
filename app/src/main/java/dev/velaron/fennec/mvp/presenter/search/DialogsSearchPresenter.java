package dev.velaron.fennec.mvp.presenter.search;

import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.List;

import dev.velaron.fennec.domain.IMessagesRepository;
import dev.velaron.fennec.domain.Repository;
import dev.velaron.fennec.fragment.search.criteria.DialogsSearchCriteria;
import dev.velaron.fennec.fragment.search.nextfrom.IntNextFrom;
import dev.velaron.fennec.model.Chat;
import dev.velaron.fennec.model.Community;
import dev.velaron.fennec.model.Peer;
import dev.velaron.fennec.model.User;
import dev.velaron.fennec.mvp.view.search.IDialogsSearchView;
import dev.velaron.fennec.util.Pair;
import dev.velaron.fennec.util.Utils;
import io.reactivex.Single;

/**
 * Created by admin on 02.05.2017.
 * phoenix
 */
public class DialogsSearchPresenter extends AbsSearchPresenter<IDialogsSearchView, DialogsSearchCriteria, Object, IntNextFrom> {

    private final IMessagesRepository messagesInteractor;

    public DialogsSearchPresenter(int accountId, @Nullable DialogsSearchCriteria criteria, @Nullable Bundle savedInstanceState) {
        super(accountId, criteria, savedInstanceState);
        this.messagesInteractor = Repository.INSTANCE.getMessages();
    }

    @Override
    IntNextFrom getInitialNextFrom() {
        return new IntNextFrom(0);
    }

    @Override
    boolean isAtLast(IntNextFrom startFrom) {
        return startFrom.getOffset() == 0;
    }

    @Override
    Single<Pair<List<Object>, IntNextFrom>> doSearch(int accountId, DialogsSearchCriteria criteria, IntNextFrom startFrom) {
        return messagesInteractor.searchDialogs(accountId, 20, criteria.getQuery())
                .map(models -> {
                    // null because load more not supported
                    return Pair.Companion.create(models, null);
                });
    }

    @Override
    DialogsSearchCriteria instantiateEmptyCriteria() {
        return new DialogsSearchCriteria("");
    }

    @Override
    boolean canSearch(DialogsSearchCriteria criteria) {
        return Utils.trimmedNonEmpty(criteria.getQuery());
    }

    public void fireEntryClick(Object o) {
        final int accountId = super.getAccountId();
        final int messagesOwnerId = super.getAccountId(); // todo Community dialogs seacrh !!!

        if(o instanceof User){
            User user = (User) o;
            final Peer peer = new Peer(Peer.fromUserId(user.getId())).setTitle(user.getFullName()).setAvaUrl(user.getMaxSquareAvatar());
            getView().openChatWith(accountId, messagesOwnerId, peer);
        } else if(o instanceof Community){
            Community group = (Community) o;
            final Peer peer = new Peer(Peer.fromGroupId(group.getId())).setTitle(group.getFullName()).setAvaUrl(group.getMaxSquareAvatar());
            getView().openChatWith(accountId, messagesOwnerId, peer);
        } else if(o instanceof Chat){
            Chat chat = (Chat) o;
            final Peer peer = new Peer(Peer.fromChatId(chat.getId())).setTitle(chat.getTitle()).setAvaUrl(chat.getMaxSquareAvatar());
            getView().openChatWith(accountId, messagesOwnerId, peer);
        }
    }
}