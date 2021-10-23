package dev.velaron.fennec.realtime;

import java.util.List;

import dev.velaron.fennec.api.model.longpoll.AddMessageUpdate;
import dev.velaron.fennec.util.Pair;
import io.reactivex.Observable;

/**
 * Created by admin on 11.04.2017.
 * phoenix
 */
public interface IRealtimeMessagesProcessor {

    Observable<TmpResult> observeResults();

    int process(int accountId, List<AddMessageUpdate> updates);

    int process(int accountId, int messageId, boolean ignoreIfExists) throws QueueContainsException;

    void registerNotificationsInterceptor(int interceptorId, Pair<Integer, Integer> aidPeerPair);

    void unregisterNotificationsInterceptor(int interceptorId);
}
