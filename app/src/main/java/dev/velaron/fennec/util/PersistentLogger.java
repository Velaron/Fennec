package dev.velaron.fennec.util;

import static dev.velaron.fennec.util.Utils.safelyClose;

import java.io.PrintWriter;
import java.io.StringWriter;

import dev.velaron.fennec.Injection;
import dev.velaron.fennec.db.interfaces.ILogsStorage;
import dev.velaron.fennec.model.LogEvent;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Ruslan Kolbasa on 26.04.2017.
 * phoenix
 */
public class PersistentLogger {

    public static void logThrowable(String tag, Throwable throwable){
        ILogsStorage store = Injection.provideLogsStore();
        Throwable cause = Utils.getCauseIfRuntime(throwable);

        getStackTrace(cause)
                .flatMapCompletable(s -> store.add(LogEvent.Type.ERROR, tag, s)
                        .ignoreElement())
                .onErrorComplete()
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {}, ignore -> {});
    }

    private static Single<String> getStackTrace(final Throwable throwable){
        return Single.fromCallable(() -> {
            StringWriter sw = null;
            PrintWriter pw = null;
            try {
                sw = new StringWriter();
                pw = new PrintWriter(sw);
                throwable.printStackTrace(pw);
                return sw.toString();
            } finally {
                safelyClose(pw);
                safelyClose(sw);
            }
        });
    }
}