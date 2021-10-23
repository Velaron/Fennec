package dev.velaron.fennec.task;

import java.util.concurrent.TimeUnit;

import dev.velaron.fennec.api.Apis;
import dev.velaron.fennec.util.RxUtils;
import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;

public class TextingNotifier {

    private int accountId;
    private long lastNotifyTime;
    private boolean isRequestNow;
    private Disposable disposable = Disposables.disposed();

    public TextingNotifier(int accountId) {
        this.accountId = accountId;
    }

    private static Completable createNotifier(int accountId, int peerId) {
        return Apis.get()
                .vkDefault(accountId)
                .messages()
                .setActivity(peerId, true)
                .delay(5, TimeUnit.SECONDS)
                .ignoreElement();
    }

    public void notifyAboutTyping(int peerId) {
        if (!canNotifyNow()) {
            return;
        }

        lastNotifyTime = System.currentTimeMillis();

        isRequestNow = true;
        disposable = createNotifier(accountId, peerId)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> isRequestNow = false, ignored -> isRequestNow = false);
    }

    public void shutdown(){
        disposable.dispose();
    }

    private boolean canNotifyNow() {
        return !isRequestNow && Math.abs(System.currentTimeMillis() - lastNotifyTime) > 5000;
    }
}