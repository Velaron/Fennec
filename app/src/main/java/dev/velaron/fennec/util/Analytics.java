package dev.velaron.fennec.util;

import dev.velaron.fennec.BuildConfig;

/**
 * Created by Ruslan Kolbasa on 07.07.2017.
 * phoenix
 */
public class Analytics {

    public static void logUnexpectedError(Throwable throwable){
        if(BuildConfig.DEBUG){
            throwable.printStackTrace();
        }

        //FirebaseCrash.report(throwable);
    }
}