package dev.velaron.fennec.longpoll;

import dev.velaron.fennec.api.model.longpoll.VkApiLongpollUpdates;
import io.reactivex.Flowable;

public interface ILongpollManager {
    void forceDestroy(int accountId);

    Flowable<VkApiLongpollUpdates> observe();

    Flowable<Integer> observeKeepAlive();

    void keepAlive(int accountId);
}