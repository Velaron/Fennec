package dev.velaron.fennec.db.interfaces;

import androidx.annotation.NonNull;

import java.util.Collection;
import java.util.List;

import dev.velaron.fennec.api.model.VKApiChat;
import dev.velaron.fennec.db.PeerStateEntity;
import dev.velaron.fennec.db.model.PeerPatch;
import dev.velaron.fennec.db.model.entity.DialogEntity;
import dev.velaron.fennec.db.model.entity.SimpleDialogEntity;
import dev.velaron.fennec.model.Chat;
import dev.velaron.fennec.model.criteria.DialogsCriteria;
import dev.velaron.fennec.util.Optional;
import dev.velaron.fennec.util.Pair;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created by hp-dv6 on 04.06.2016.
 * VKMessenger
 */
public interface IDialogsStorage extends IStorage {

    int getUnreadDialogsCount(int accountId);

    Observable<Pair<Integer, Integer>> observeUnreadDialogsCount();

    Single<List<PeerStateEntity>> findPeerStates(int accountId, Collection<Integer> ids);

    void setUnreadDialogsCount(int accountId, int unreadCount);

    Single<Optional<SimpleDialogEntity>> findSimple(int accountId, int peerId);

    Completable saveSimple(int accountId, @NonNull SimpleDialogEntity entity);

    Single<List<DialogEntity>> getDialogs(@NonNull DialogsCriteria criteria);

    Completable removePeerWithId(int accountId, int peerId);

    Completable insertDialogs(int accountId, List<DialogEntity> dbos, boolean clearBefore);

    /**
     * Получение списка идентификаторов диалогов, информация о которых отсутствует в базе данных
     *
     * @param ids список входящих идентификаторов
     * @return отсутствующие
     */
    Single<Collection<Integer>> getMissingGroupChats(int accountId, Collection<Integer> ids);

    Completable insertChats(int accountId, List<VKApiChat> chats);

    Completable applyPatches(int accountId, @NonNull List<PeerPatch> patches);

    Single<Optional<Chat>> findChatById(int accountId, int peerId);
}