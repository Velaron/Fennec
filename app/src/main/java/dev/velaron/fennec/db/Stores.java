package dev.velaron.fennec.db;

import dev.velaron.fennec.Injection;
import dev.velaron.fennec.db.interfaces.IStorages;

public class Stores {

    public static IStorages getInstance(){
        return Injection.provideStores();
    }

}
