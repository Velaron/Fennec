package dev.velaron.fennec.push;

import java.util.concurrent.Executors;

import dev.velaron.fennec.Injection;
import io.reactivex.Scheduler;
import io.reactivex.SingleTransformer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Ruslan Kolbasa on 07.07.2017.
 * phoenix
 */
public class NotificationScheduler {

    public static final Scheduler INSTANCE = Schedulers.from(Executors.newFixedThreadPool(1));

    public static <T> SingleTransformer<T,T> fromNotificationThreadToMain(){
        return single -> single
                .subscribeOn(INSTANCE)
                .observeOn(Injection.provideMainThreadScheduler());
    }
}