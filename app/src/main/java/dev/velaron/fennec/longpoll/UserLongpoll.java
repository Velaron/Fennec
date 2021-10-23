package dev.velaron.fennec.longpoll;

import java.util.concurrent.TimeUnit;

import dev.velaron.fennec.Injection;
import dev.velaron.fennec.api.interfaces.INetworker;
import dev.velaron.fennec.api.model.VkApiLongpollServer;
import dev.velaron.fennec.api.model.longpoll.AddMessageUpdate;
import dev.velaron.fennec.api.model.longpoll.VkApiLongpollUpdates;
import dev.velaron.fennec.util.Logger;
import dev.velaron.fennec.util.PersistentLogger;
import dev.velaron.fennec.util.RxUtils;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import static dev.velaron.fennec.util.Objects.nonNull;
import static dev.velaron.fennec.util.Utils.nonEmpty;

class UserLongpoll implements ILongpoll {

    private static final String TAG = "Longpoll_TAG";
    private static final int DELAY_ON_ERROR = 10 * 1000;

    private final int accountId;
    private String key;
    private String server;
    private Long ts;
    private Callback callback;
    private final INetworker networker;

    UserLongpoll(INetworker networker, int accountId, Callback callback) {
        this.accountId = accountId;
        this.callback = callback;
        this.networker = networker;
    }

    @Override
    public int getAccountId() {
        return accountId;
    }

    private void resetServerAttrs() {
        this.server = null;
        this.key = null;
        this.ts = null;
    }

    @Override
    public void shutdown() {
        Logger.d(TAG, "shutdown, aid: " + accountId);
        resetUpdatesDisposable();
    }

    @Override
    public void connect() {
        Logger.d(TAG, "connect, aid: " + accountId);
        if (!isListeningNow()) {
            get();
        }
    }

    private boolean isListeningNow() {
        return nonNull(mCurrentUpdatesDisposable) && !mCurrentUpdatesDisposable.isDisposed();
    }

    private Disposable mCurrentUpdatesDisposable;

    private void resetUpdatesDisposable() {
        if (nonNull(mCurrentUpdatesDisposable)) {
            if (!mCurrentUpdatesDisposable.isDisposed()) {
                mCurrentUpdatesDisposable.dispose();
            }

            mCurrentUpdatesDisposable = null;
        }
    }

    private void onServerInfoReceived(VkApiLongpollServer info) {
        Logger.d(TAG, "onResponse, info: " + info);

        this.ts = info.ts;
        this.key = info.key;
        this.server = info.server;

        get();
    }

    private void onServerGetError(Throwable throwable) {
        PersistentLogger.logThrowable("Longpoll, ServerGet", throwable);
        getWithDelay();
    }

    private void get() {
        resetUpdatesDisposable();

        boolean serverIsValid = nonEmpty(server) && nonEmpty(key) && nonNull(ts);

        if (!serverIsValid) {
            setDisposable(networker.vkDefault(accountId)
                    .messages()
                    .getLongpollServer(true, V)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(this::onServerInfoReceived, this::onServerGetError));
            return;
        }

        setDisposable(networker.longpoll()
                .getUpdates("https://" + server, key, ts, 25, MODE, V)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onUpdates, this::onUpdatesGetError));
    }

    private static final int V = 3;

    private void setDisposable(Disposable disposable) {
        mCurrentUpdatesDisposable = disposable;
    }

    private void onUpdates(VkApiLongpollUpdates updates) {
        Logger.d(TAG, "onUpdates, updates: " + updates);

        if (updates.failed > 0) {
            resetServerAttrs();
            getWithDelay();
        } else {
            ts = updates.ts;

            if (updates.getUpdatesCount() > 0) {
                fixUpdates(updates);

                callback.onUpdates(accountId, updates);
            }

            get();
        }
    }

    private void fixUpdates(VkApiLongpollUpdates updates){
        if(nonEmpty(updates.add_message_updates)){
            for(AddMessageUpdate update : updates.add_message_updates){
                if(update.outbox){
                    update.from = accountId;
                }
            }
        }
    }

    private void onUpdatesGetError(Throwable throwable) {
        PersistentLogger.logThrowable("Longpoll, UpdatesGet", throwable);
        getWithDelay();
    }

    private Observable<Long> mDelayedObservable = Observable.interval(DELAY_ON_ERROR, DELAY_ON_ERROR,
            TimeUnit.MILLISECONDS, Injection.provideMainThreadScheduler());

    private void getWithDelay() {
        setDisposable(mDelayedObservable.subscribe(o -> get()));
    }

    private static final int MODE =
            2 + //получать вложения;
                    8 + // возвращать расширенный набор событий;
                    //32 + //возвращать pts (это требуется для работы метода messages.getLongPollHistory без ограничения в 256 последних событий);
                    64 + //в событии с кодом 8 (друг стал онлайн) возвращать дополнительные данные в поле $extra (подробнее в разделе Структура событий);
                    128; //возвращать с сообщением параметр random_id (random_id может быть передан при отправке сообщения методом messages.send).

    public interface Callback {
        void onUpdates(int aid, VkApiLongpollUpdates updates);
    }
}