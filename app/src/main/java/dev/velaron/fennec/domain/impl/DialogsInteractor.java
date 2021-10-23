package dev.velaron.fennec.domain.impl;

import dev.velaron.fennec.api.interfaces.INetworker;
import dev.velaron.fennec.db.interfaces.IStorages;
import dev.velaron.fennec.domain.IDialogsInteractor;
import dev.velaron.fennec.domain.mappers.Dto2Model;
import dev.velaron.fennec.exception.NotFoundException;
import dev.velaron.fennec.model.Chat;
import dev.velaron.fennec.model.Peer;
import io.reactivex.Single;

import static dev.velaron.fennec.util.Utils.isEmpty;

/**
 * Created by admin on 19.03.2017.
 * phoenix
 */
public class DialogsInteractor implements IDialogsInteractor {

    private final INetworker networker;

    private final IStorages repositories;

    public DialogsInteractor(INetworker networker, IStorages repositories) {
        this.networker = networker;
        this.repositories = repositories;
    }

    @Override
    public Single<Chat> getChatById(int accountId, int peerId) {
        return repositories.dialogs()
                .findChatById(accountId, peerId)
                .flatMap(optional -> {
                    if(optional.nonEmpty()){
                        return Single.just(optional.get());
                    }

                    final int chatId = Peer.toChatId(peerId);
                    return networker.vkDefault(accountId)
                            .messages()
                            .getChat(chatId, null, null, null)
                            .map(chats -> {
                                if(isEmpty(chats)){
                                    throw new NotFoundException();
                                }

                                return chats.get(0);
                            })
                            .map(Dto2Model::transform);
                });
    }
}