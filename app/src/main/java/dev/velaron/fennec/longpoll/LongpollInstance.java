package dev.velaron.fennec.longpoll;

import dev.velaron.fennec.App;
import dev.velaron.fennec.Injection;
import dev.velaron.fennec.realtime.Processors;

public class LongpollInstance {

    private static volatile ILongpollManager longpollManager;

    public static ILongpollManager get() {
        if(longpollManager == null){
            synchronized (LongpollInstance.class){
                if(longpollManager == null){
                    longpollManager = new AndroidLongpollManager(App.getInstance(), Injection.provideNetworkInterfaces(), Processors.realtimeMessages());
                }
            }
        }
        return longpollManager;
    }
}