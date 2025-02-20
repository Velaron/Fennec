package dev.velaron.fennec.view.pager;

import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

/**
 * Created by ruslan.kolbasa on 19.10.2016.
 * phoenix
 */
public class AbsPagerHolder {

    private boolean mDestroyed;
    private int mAdapterPosition;
    public View mItemView;

    public AbsPagerHolder(int adapterPosition, @NonNull View itemView) {
        this.mItemView = itemView;
        this.mAdapterPosition = adapterPosition;
    }

    public final void destroy(){
        onDestroy();
        mDestroyed = true;
    }

    @CallSuper
    protected void onDestroy(){

    }

    public boolean isDestroyed() {
        return mDestroyed;
    }

    public int getAdapterPosition() {
        return mAdapterPosition;
    }
}
