package dev.velaron.fennec.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Transformation;

import java.util.EventListener;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import dev.velaron.fennec.Constants;
import dev.velaron.fennec.R;
import dev.velaron.fennec.model.Chat;
import dev.velaron.fennec.model.Community;
import dev.velaron.fennec.model.User;
import dev.velaron.fennec.settings.CurrentTheme;
import dev.velaron.fennec.util.ViewUtils;

/**
 * Created by golde on 02.05.2017.
 * phoenix
 */
public class DialogPreviewAdapter extends RecyclerView.Adapter<DialogPreviewAdapter.DialogPreviewHolder> {

    private List<Object> mData;
    private Transformation mTransformation;

    private final ActionListener actionListener;

    public DialogPreviewAdapter(Context context, List<Object> items, ActionListener actionListener) {
        this.mData = items;
        this.mTransformation = CurrentTheme.createTransformationForAvatar(context);
        this.actionListener = actionListener;
    }

    @Override
    public DialogPreviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DialogPreviewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dialog_preview, parent, false));
    }

    @Override
    public void onBindViewHolder(DialogPreviewHolder holder, int position) {
        Object item = mData.get(position);

        if (item instanceof User) {
            bindUserDialog(holder, (User) item);
        }

        if (item instanceof Community) {
            bindGroupDialog(holder, (Community) item);
        }

        if (item instanceof Chat) {
            bindGroupChat(holder, (Chat) item);
        }

        holder.itemView.setOnClickListener(v -> actionListener.onEntryClick(item));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    private void bindUserDialog(DialogPreviewHolder holder, User item) {
        holder.mTitle.setText(item.getFullName());
        ViewUtils.displayAvatar(holder.mAvatar, mTransformation, item.get100photoOrSmaller(), Constants.PICASSO_TAG);
    }

    private void bindGroupDialog(DialogPreviewHolder holder, Community item) {
        holder.mTitle.setText(item.getFullName());
        ViewUtils.displayAvatar(holder.mAvatar, mTransformation, item.get100photoOrSmaller(), Constants.PICASSO_TAG);
    }

    private void bindGroupChat(DialogPreviewHolder holder, Chat item){
        holder.mTitle.setText(item.getTitle());
        ViewUtils.displayAvatar(holder.mAvatar, mTransformation, item.get100orSmallerAvatar(), Constants.PICASSO_TAG, R.drawable.ic_group_chat);
    }

    public interface ActionListener extends EventListener {
        void onEntryClick(Object o);
    }

    public void setData(List<Object> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    class DialogPreviewHolder extends RecyclerView.ViewHolder {

        private ImageView mAvatar;
        private TextView mTitle;

        DialogPreviewHolder(View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.dialog_title);
            mAvatar = itemView.findViewById(R.id.item_chat_avatar);
        }
    }
}