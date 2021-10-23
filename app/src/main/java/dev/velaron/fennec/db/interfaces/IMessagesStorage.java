package dev.velaron.fennec.db.interfaces;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collection;
import java.util.List;

import dev.velaron.fennec.db.model.MessageEditEntity;
import dev.velaron.fennec.db.model.MessagePatch;
import dev.velaron.fennec.db.model.entity.MessageEntity;
import dev.velaron.fennec.model.DraftMessage;
import dev.velaron.fennec.model.MessageStatus;
import dev.velaron.fennec.model.criteria.MessagesCriteria;
import dev.velaron.fennec.util.Optional;
import dev.velaron.fennec.util.Pair;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;

/**
 * Created by hp-dv6 on 01.06.2016.
 * VKMessenger
 */
public interface IMessagesStorage extends IStorage {

    Completable insertPeerDbos(int accountId, int peerId, @NonNull List<MessageEntity> dbos, boolean clearHistory);

    Single<int[]> insert(int accountId, @NonNull List<MessageEntity> dbos);

    Single<List<MessageEntity>> getByCriteria(@NonNull MessagesCriteria criteria, boolean withAtatchments, boolean withForwardMessages);

    Single<Integer> insert(int accountId, int peerId, @NonNull MessageEditEntity patch);

    Single<Integer> applyPatch(int accountId, int messageId, @NonNull MessageEditEntity patch);

    @CheckResult
    Maybe<DraftMessage> findDraftMessage(int accountId, int peerId);

    @CheckResult
    Single<Integer> saveDraftMessageBody(int acocuntId, int peerId, String body);

    //@CheckResult
    //Maybe<Integer> getDraftMessageId(int accoutnId, int peerId);

    Single<Integer> getMessageStatus(int accountId, int dbid);

    Completable applyPatches(int accountId, @NonNull Collection<MessagePatch> patches);

    @CheckResult
    Completable changeMessageStatus(int accountId, int messageId, @MessageStatus int status, @Nullable Integer vkid);

    //@CheckResult
    //Completable updateMessageFlag(int accountId, int messageId, Collection<Pair<Integer, Boolean>> values);

    @CheckResult
    Single<Boolean> deleteMessage(int accountId, int messageId);

    Single<Optional<Integer>> findLastSentMessageIdForPeer(int accounId, int peerId);

    Single<List<MessageEntity>> findMessagesByIds(int accountId, List<Integer> ids, boolean withAtatchments, boolean withForwardMessages);

    Single<Optional<Pair<Integer, MessageEntity>>> findFirstUnsentMessage(Collection<Integer> accountIds, boolean withAtatchments, boolean withForwardMessages);

    Completable notifyMessageHasAttachments(int accountId, int messageId);

    ///**
    // * Получить список сообщений, которые "приаттаччены" к сообщению с идентификатором attachTo
    // *
    // * @param accountId          идентификатор аккаунта
    // * @param attachTo           идентификатор сообщения
    // * @param includeFwd         если true - рекурсивно загрузить всю иерархию сообщений (вложенные во вложенных и т.д.)
    // * @param includeAttachments - если true - включить вложения к пересланным сообщениям
    // * @param forceAttachments   если true - то алгоритм проигнорирует значение в HAS_ATTACHMENTS
    // *                           и в любом случае будет делать выборку из таблицы вложений
    // * @return список сообщений
    // */
    //@CheckResult
    //Single<List<Message>> getForwardMessages(int accountId, int attachTo, boolean includeFwd, boolean includeAttachments, boolean forceAttachments);

    @CheckResult
    Single<List<Integer>> getForwardMessageIds(int accountId, int attachTo);

    //Observable<MessageUpdate> observeMessageUpdates();

    Single<List<Integer>> getMissingMessages(int accountId, Collection<Integer> ids);
}