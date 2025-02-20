package dev.velaron.fennec.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import dev.velaron.fennec.R;
import dev.velaron.fennec.adapter.base.RecyclerBindableAdapter;
import dev.velaron.fennec.adapter.holder.IdentificableHolder;
import dev.velaron.fennec.adapter.holder.SharedHolders;
import dev.velaron.fennec.api.PicassoInstance;
import dev.velaron.fennec.model.AbsModel;
import dev.velaron.fennec.model.AttachmenEntry;
import dev.velaron.fennec.model.Audio;
import dev.velaron.fennec.model.Document;
import dev.velaron.fennec.model.FwdMessages;
import dev.velaron.fennec.model.Link;
import dev.velaron.fennec.model.Photo;
import dev.velaron.fennec.model.PhotoSize;
import dev.velaron.fennec.model.Poll;
import dev.velaron.fennec.model.Post;
import dev.velaron.fennec.model.Video;
import dev.velaron.fennec.upload.Upload;
import dev.velaron.fennec.view.CircleRoadProgress;

import static dev.velaron.fennec.util.Objects.nonNull;
import static dev.velaron.fennec.util.Utils.nonEmpty;

/**
 * Created by ruslan.kolbasa on 01.06.2017.
 * phoenix
 */
public class AttchmentsEditorAdapter extends RecyclerBindableAdapter<AttachmenEntry, AttchmentsEditorAdapter.ViewHolder> {

    private Context context;
    private Callback callback;
    private final SharedHolders<ViewHolder> sharedHolders;

    public AttchmentsEditorAdapter(Context context, List<AttachmenEntry> items, Callback callback) {
        super(items);
        this.context = context;
        this.callback = callback;
        this.sharedHolders = new SharedHolders<>(false);
    }

    private static final int ERROR_COLOR = Color.parseColor("#ff0000");

    @Override
    protected void onBindItemViewHolder(ViewHolder holder, int position, int type) {
        final AttachmenEntry attachment = getItem(position);

        sharedHolders.put(attachment.getId(), holder);

        configView(attachment, holder);

        holder.vRemove.setOnClickListener(view -> {
            int dataposition = holder.getAdapterPosition() - getHeadersCount();
            callback.onRemoveClick(dataposition, attachment);
        });

        holder.vTitleRoot.setOnClickListener(v -> {
            int dataposition = holder.getAdapterPosition() - getHeadersCount();
            callback.onTitleClick(dataposition, attachment);
        });
    }

    public void cleanup(){
        sharedHolders.release();
    }

    @Override
    protected ViewHolder viewHolder(View view, int type) {
        return new ViewHolder(view);
    }

    @Override
    protected int layoutId(int type) {
        return R.layout.item_post_attachments;
    }

    private static int idGenerator;

    private static int generateNextHolderId(){
        idGenerator++;
        return idGenerator;
    }

    public void updateEntityProgress(int attachmentId, int progress){
        ViewHolder holder = sharedHolders.findOneByEntityId(attachmentId);

        if(nonNull(holder)){
            bindProgress(holder, progress, true);
        }
    }

    private void bindProgress(ViewHolder holder, int progress, boolean smoothly){
        String progressLine = progress + "%";
        holder.tvTitle.setText(progressLine);

        if(smoothly){
            holder.pbProgress.changePercentageSmoothly(progress);
        } else {
            holder.pbProgress.changePercentage(progress);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements IdentificableHolder {

        ImageView photoImageView;
        TextView tvTitle;
        View vRemove;
        CircleRoadProgress pbProgress;
        View vTint;
        View vTitleRoot;

        ViewHolder(View itemView) {
            super(itemView);

            photoImageView = itemView.findViewById(R.id.item_attachment_image);
            tvTitle = itemView.findViewById(R.id.item_attachment_title);
            vRemove = itemView.findViewById(R.id.item_attachment_progress_root);
            pbProgress = itemView.findViewById(R.id.item_attachment_progress);
            vTint = itemView.findViewById(R.id.item_attachment_tint);
            vTitleRoot = itemView.findViewById(R.id.item_attachment_title_root);

            itemView.setTag(generateNextHolderId());
        }

        @Override
        public int getHolderId() {
            return (int) itemView.getTag();
        }
    }

    private void configUploadObject(Upload upload, ViewHolder holder) {
        holder.pbProgress.setVisibility(upload.getStatus() == Upload.STATUS_UPLOADING ? View.VISIBLE : View.GONE);
        holder.vTint.setVisibility(View.VISIBLE);

        int nonErrorTextColor = holder.tvTitle.getTextColors().getDefaultColor();
        switch (upload.getStatus()) {
            case Upload.STATUS_ERROR:
                holder.tvTitle.setText(R.string.error);
                holder.tvTitle.setTextColor(ERROR_COLOR);
                break;
            case Upload.STATUS_QUEUE:
                holder.tvTitle.setText(R.string.in_order);
                holder.tvTitle.setTextColor(nonErrorTextColor);
                break;
            case Upload.STATUS_CANCELLING:
                holder.tvTitle.setText(R.string.cancelling);
                holder.tvTitle.setTextColor(nonErrorTextColor);
                break;
            default:
                holder.tvTitle.setTextColor(nonErrorTextColor);
                String progressLine = upload.getProgress() + "%";
                holder.tvTitle.setText(progressLine);
                break;
        }

        holder.pbProgress.changePercentage(upload.getProgress());

        if (upload.hasThumbnail()) {
            PicassoInstance.with()
                    .load(upload.buildThumnailUri())
                    .placeholder(R.drawable.background_gray)
                    .into(holder.photoImageView);
        } else {
            PicassoInstance.with().cancelRequest(holder.photoImageView);
            holder.photoImageView.setImageResource(R.drawable.background_gray);
        }
    }

    private void bindLink(ViewHolder holder, Link link){
        holder.tvTitle.setText(R.string.link);

        String photoLink = nonNull(link.getPhoto()) ? link.getPhoto().getUrlForSize(PhotoSize.X, false) : null;

        if (nonEmpty(photoLink)) {
            PicassoInstance.with()
                    .load(photoLink)
                    .placeholder(R.drawable.background_gray)
                    .into(holder.photoImageView);
        } else {
            PicassoInstance.with().cancelRequest(holder.photoImageView);
            holder.photoImageView.setImageResource(R.drawable.background_gray);
        }
    }

    private void bindPhoto(ViewHolder holder, Photo photo) {
        holder.tvTitle.setText(R.string.photo);

        PicassoInstance.with()
                .load(photo.getUrlForSize(PhotoSize.X, false))
                .placeholder(R.drawable.background_gray)
                .into(holder.photoImageView);

        holder.photoImageView.setOnClickListener(null);
    }

    private void bindVideo(ViewHolder holder, Video video) {
        holder.tvTitle.setText(video.getTitle());

        PicassoInstance.with()
                .load(video.getMaxResolutionPhoto())
                .placeholder(R.drawable.background_gray)
                .into(holder.photoImageView);

        holder.photoImageView.setOnClickListener(null);
    }

    private void bindAudio(ViewHolder holder, Audio audio) {
        PicassoInstance.with()
                .load(R.drawable.background_gray)
                .into(holder.photoImageView);

        String audiostr = audio.getArtist() + " - " + audio.getTitle();
        holder.tvTitle.setText(audiostr);
        holder.photoImageView.setOnClickListener(null);
    }

    private void bindPoll(ViewHolder holder, Poll poll) {
        PicassoInstance.with()
                .load(R.drawable.background_gray)
                .into(holder.photoImageView);

        holder.tvTitle.setText(poll.getQuestion());
        holder.photoImageView.setOnClickListener(null);
    }

    private void bindPost(ViewHolder holder, Post post) {
        String postImgUrl = post.findFirstImageCopiesInclude();

        if (TextUtils.isEmpty(postImgUrl)) {
            PicassoInstance.with()
                    .load(R.drawable.background_gray)
                    .into(holder.photoImageView);
        } else {
            PicassoInstance.with()
                    .load(postImgUrl)
                    .into(holder.photoImageView);
        }

        holder.tvTitle.setText(R.string.attachment_wall_post);
        holder.photoImageView.setOnClickListener(null);
    }

    private void bindDoc(ViewHolder holder, Document document) {
        String previewUrl = document.getPreviewWithSize(PhotoSize.X, false);

        if (nonEmpty(previewUrl)) {
            PicassoInstance.with()
                    .load(previewUrl)
                    .into(holder.photoImageView);
        } else {
            PicassoInstance.with()
                    .load(R.drawable.background_gray)
                    .into(holder.photoImageView);
        }

        holder.photoImageView.setOnClickListener(null);
        holder.tvTitle.setText(document.getTitle());
    }

    @SuppressWarnings("unused")
    private void bindFwdMessages(ViewHolder holder, FwdMessages messages) {
        PicassoInstance.with()
                .load(R.drawable.background_gray)
                .into(holder.photoImageView);

        //holder.tvText.setVisibility(View.VISIBLE);
        //holder.photoImageView.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        //TODO
        //    }
        //});

        //holder.tvText.setText("["+messages.fwds.size()+"]");
        holder.tvTitle.setText(context.getString(R.string.title_mssages));
    }

    private void configView(AttachmenEntry item, ViewHolder holder) {
        holder.vRemove.setVisibility(item.isCanDelete() ? View.VISIBLE : View.GONE);

        AbsModel model = item.getAttachment();

        holder.pbProgress.setVisibility(View.GONE);
        holder.vTint.setVisibility(View.GONE);

        if (model instanceof Photo) {
            bindPhoto(holder, (Photo) model);
        } else if (model instanceof Video) {
            bindVideo(holder, (Video) model);
        } else if (model instanceof Audio) {
            bindAudio(holder, (Audio) model);
        } else if (model instanceof Poll) {
            bindPoll(holder, (Poll) model);
        } else if (model instanceof Post) {
            bindPost(holder, (Post) model);
        } else if (model instanceof Document) {
            bindDoc(holder, (Document) model);
        } else if (model instanceof FwdMessages) {
            bindFwdMessages(holder, (FwdMessages) model);
        } else if (model instanceof Upload) {
            configUploadObject((Upload) model, holder);
        } else if (model instanceof Link) {
            bindLink(holder, (Link) model);
        } else {
            throw new UnsupportedOperationException("Type " + model.getClass() + " in not supported");
        }
    }

    public interface Callback {
        void onRemoveClick(int dataposition, @NonNull AttachmenEntry entry);

        void onTitleClick(int dataposition, @NonNull AttachmenEntry entry);
    }
}