package dev.velaron.fennec.util;

import static dev.velaron.fennec.util.Objects.nonNull;

import io.reactivex.disposables.Disposable;

/**
 * Created by admin on 19.03.2017.
 * phoenix
 */
public class DisposableHolder<T> {

    private Disposable disposable;

    private T tag;

    public void append(Disposable disposable){
        append(disposable, null);
    }

    public void append(Disposable disposable, T tag){
        dispose();

        this.disposable = disposable;
        this.tag = tag;
    }

    public void dispose(){
        if(nonNull(disposable)){
            if(!disposable.isDisposed()){
                disposable.dispose();
            }

            disposable = null;
        }
    }

    public boolean isActive(){
        return nonNull(disposable) && !disposable.isDisposed();
    }

    public T getTag() {
        return tag;
    }
}
