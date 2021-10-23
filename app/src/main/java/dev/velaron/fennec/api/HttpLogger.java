package dev.velaron.fennec.api;

import dev.velaron.fennec.BuildConfig;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by Ruslan Kolbasa on 28.07.2017.
 * phoenix
 */
public class HttpLogger {

    public static final HttpLoggingInterceptor DEFAULT_LOGGING_INTERCEPTOR = new HttpLoggingInterceptor();

    static {
        if (BuildConfig.DEBUG) {
            DEFAULT_LOGGING_INTERCEPTOR.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            DEFAULT_LOGGING_INTERCEPTOR.setLevel(HttpLoggingInterceptor.Level.NONE);
        }
    }
}