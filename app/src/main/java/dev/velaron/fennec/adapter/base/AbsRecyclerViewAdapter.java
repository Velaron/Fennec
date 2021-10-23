package dev.velaron.fennec.adapter.base;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import dev.velaron.fennec.adapter.listener.OwnerClickListener;

import static dev.velaron.fennec.util.Objects.nonNull;

/**
 * Created by Ruslan Kolbasa on 15.05.2017.
 * phoenix
 */
public abstract class AbsRecyclerViewAdapter<H extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<H> {

    private OwnerClickListener ownerClickListener;

    public void setOwnerClickListener(OwnerClickListener ownerClickListener) {
        this.ownerClickListener = ownerClickListener;
    }

    protected void addOwnerAvatarClickHandling(View view, final int ownerId){
        view.setOnClickListener(v -> {
            if(nonNull(ownerClickListener)){
                ownerClickListener.onOwnerClick(ownerId);
            }
        });
    }
}