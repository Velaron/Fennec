package dev.velaron.fennec.longpoll;

public interface ILongpoll {
    int getAccountId();
    void connect();
    void shutdown();
}