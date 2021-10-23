package dev.velaron.fennec.adapter;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.EventListener;
import java.util.List;

import dev.velaron.fennec.Constants;
import dev.velaron.fennec.R;
import dev.velaron.fennec.adapter.base.RecyclerBindableAdapter;
import dev.velaron.fennec.api.PicassoInstance;
import dev.velaron.fennec.model.Document;
import dev.velaron.fennec.model.PhotoSize;
import dev.velaron.fennec.settings.CurrentTheme;
import dev.velaron.fennec.util.AppTextUtils;

import static dev.velaron.fennec.util.Objects.nonNull;

/**
 * Created by admin on 25.12.2016.
 * phoenix
 */
public class DocsAdapter extends RecyclerBindableAdapter<Document, DocsAdapter.DocViewHolder> {

    public DocsAdapter(List<Document> data) {
        super(data);
    }

    private ActionListener mActionListner;

    public void setActionListner(ActionListener listner) {
        this.mActionListner = listner;
    }

    public interface ActionListener extends EventListener {
        void onDocClick(int index, @NonNull Document doc);
        boolean onDocLongClick(int index, @NonNull Document doc);
    }

    @Override
    protected void onBindItemViewHolder(DocViewHolder holder, int position, int type) {
        Document item = getItem(position);

        String targetExt = "." + item.getExt().toUpperCase();

        holder.tvExt.setText(targetExt);
        holder.tvTitle.setText(item.getTitle());
        holder.tvSize.setText(AppTextUtils.getSizeString((int) item.getSize()));

        String previewUrl = item.getPreviewWithSize(PhotoSize.M, false);
        boolean withImage = !TextUtils.isEmpty(previewUrl);

        holder.ivImage.setVisibility(withImage ? View.VISIBLE : View.GONE);
        holder.ivImage.setBackgroundColor(Color.TRANSPARENT);

        if (withImage) {
            PicassoInstance.with()
                    .load(previewUrl)
                    .tag(Constants.PICASSO_TAG)
                    .into(holder.ivImage);
        }

        holder.itemView.setOnClickListener(v -> {
            if(nonNull(mActionListner)){
                mActionListner.onDocClick(holder.getAdapterPosition(), item);
            }
        });

        holder.itemView.setOnLongClickListener(v -> nonNull(mActionListner)
                && mActionListner.onDocLongClick(holder.getAdapterPosition(), item));
    }

    @Override
    protected DocViewHolder viewHolder(View view, int type) {
        return new DocViewHolder(view);
    }

    @Override
    protected int layoutId(int type) {
        return R.layout.item_document_big;
    }

    static class DocViewHolder extends RecyclerView.ViewHolder {

        TextView tvExt;
        ImageView ivImage;
        TextView tvTitle;
        TextView tvSize;

        private DocViewHolder(View root) {
            super(root);
            tvExt = root.findViewById(R.id.item_document_big_ext);
            ivImage = root.findViewById(R.id.item_document_big_image);
            tvTitle = root.findViewById(R.id.item_document_big_title);
            tvSize = root.findViewById(R.id.item_document_big_size);

            tvExt.getBackground().setColorFilter(CurrentTheme.getColorPrimary(root.getContext()),
                    PorterDuff.Mode.MULTIPLY);
        }
    }
}