package dev.velaron.fennec.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.EventListener;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import dev.velaron.fennec.Constants;
import dev.velaron.fennec.R;
import dev.velaron.fennec.adapter.base.RecyclerBindableAdapter;
import dev.velaron.fennec.api.PicassoInstance;
import dev.velaron.fennec.model.Document;
import dev.velaron.fennec.model.PhotoSize;

import static dev.velaron.fennec.util.Objects.nonNull;
import static dev.velaron.fennec.util.Utils.nonEmpty;

/**
 * Created by admin on 25.12.2016.
 * phoenix
 */
public class DocsAsImagesAdapter extends RecyclerBindableAdapter<Document, DocsAsImagesAdapter.DocViewHolder> {

    public DocsAsImagesAdapter(List<Document> data) {
        super(data);
    }

    public void setData(List<Document> data) {
        super.setItems(data);
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

        holder.title.setText(item.getTitle());

        String previewUrl = item.getPreviewWithSize(PhotoSize.Q, false);
        boolean withImage = nonEmpty(previewUrl);

        if (withImage) {
            PicassoInstance.with()
                    .load(previewUrl)
                    .tag(Constants.PICASSO_TAG)
                    .into(holder.image);
        } else {
            PicassoInstance.with()
                    .cancelRequest(holder.image);
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
        return R.layout.item_doc_as_image;
    }

    static class DocViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView title;

        DocViewHolder(View root) {
            super(root);
            image = root.findViewById(R.id.image);
            title = root.findViewById(R.id.title);
        }
    }
}