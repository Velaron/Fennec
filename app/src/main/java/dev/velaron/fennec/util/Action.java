package dev.velaron.fennec.util;

/**
 * Created by admin on 30.05.2017.
 * phoenix
 */
public interface Action<T> {
    void call(T targer);
}
