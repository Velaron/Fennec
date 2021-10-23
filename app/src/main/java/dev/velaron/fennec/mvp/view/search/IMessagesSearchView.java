package dev.velaron.fennec.mvp.view.search;

import dev.velaron.fennec.model.Message;

/**
 * Created by admin on 01.05.2017.
 * phoenix
 */
public interface IMessagesSearchView extends IBaseSearchView<Message> {

    void goToMessagesLookup(int accountId, int peerId, int messageId);
}
