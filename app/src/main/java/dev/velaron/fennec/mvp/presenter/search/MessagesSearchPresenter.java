package dev.velaron.fennec.mvp.presenter.search;

import static dev.velaron.fennec.util.Utils.trimmedNonEmpty;

import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.List;

import dev.velaron.fennec.domain.IMessagesRepository;
import dev.velaron.fennec.domain.Repository;
import dev.velaron.fennec.fragment.search.criteria.MessageSeachCriteria;
import dev.velaron.fennec.fragment.search.nextfrom.IntNextFrom;
import dev.velaron.fennec.model.Message;
import dev.velaron.fennec.mvp.view.search.IMessagesSearchView;
import dev.velaron.fennec.util.Objects;
import dev.velaron.fennec.util.Pair;
import io.reactivex.Single;

/**
 * Created by admin on 01.05.2017.
 * phoenix
 */
public class MessagesSearchPresenter extends AbsSearchPresenter<IMessagesSearchView, MessageSeachCriteria, Message, IntNextFrom> {

    private final IMessagesRepository messagesInteractor;

    public MessagesSearchPresenter(int accountId, @Nullable MessageSeachCriteria criteria, @Nullable Bundle savedInstanceState) {
        super(accountId, criteria, savedInstanceState);
        this.messagesInteractor = Repository.INSTANCE.getMessages();

        if(canSearch(getCriteria())){
            doSearch();
        }
    }

    @Override
    IntNextFrom getInitialNextFrom() {
        return new IntNextFrom(0);
    }

    @Override
    boolean isAtLast(IntNextFrom startFrom) {
        return startFrom.getOffset() == 0;
    }

    private static final int COUNT = 50;

    @Override
    Single<Pair<List<Message>, IntNextFrom>> doSearch(int accountId, MessageSeachCriteria criteria, IntNextFrom nextFrom) {
        final int offset = Objects.isNull(nextFrom) ? 0 : nextFrom.getOffset();
        return messagesInteractor
                .searchMessages(accountId, criteria.getPeerId(), COUNT, offset, criteria.getQuery())
                .map(messages -> Pair.Companion.create(messages, new IntNextFrom(offset + COUNT)));
    }

    @Override
    MessageSeachCriteria instantiateEmptyCriteria() {
        return new MessageSeachCriteria("");
    }

    @Override
    boolean canSearch(MessageSeachCriteria criteria) {
        return trimmedNonEmpty(criteria.getQuery());
    }

    public void fireMessageClick(Message message) {
        getView().goToMessagesLookup(getAccountId(), message.getPeerId(), message.getId());
    }
}
