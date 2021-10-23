package dev.velaron.fennec.api;

import dev.velaron.fennec.Injection;
import dev.velaron.fennec.api.interfaces.INetworker;

/**
 * Created by ruslan.kolbasa on 29.12.2016.
 * phoenix
 */
public class Apis {

    public static INetworker get(){
        return Injection.provideNetworkInterfaces();
    }

}
