package dev.velaron.fennec.api.interfaces;

import dev.velaron.fennec.api.model.longpoll.VkApiGroupLongpollUpdates;
import dev.velaron.fennec.api.model.longpoll.VkApiLongpollUpdates;
import io.reactivex.Single;

/**
 * Created by Ruslan Kolbasa on 31.07.2017.
 * phoenix
 */
public interface ILongpollApi {
    Single<VkApiLongpollUpdates> getUpdates(String server, String key, long ts, int wait, int mode, int version);

    Single<VkApiGroupLongpollUpdates> getGroupUpdates(String server, String key, String ts, int wait);
}