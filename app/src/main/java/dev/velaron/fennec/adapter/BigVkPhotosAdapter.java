package dev.velaron.fennec.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import dev.velaron.fennec.R;
import dev.velaron.fennec.adapter.holder.IdentificableHolder;
import dev.velaron.fennec.adapter.holder.SharedHolders;
import dev.velaron.fennec.adapter.multidata.DifferentDataAdapter;
import dev.velaron.fennec.api.PicassoInstance;
import dev.velaron.fennec.model.LocalPhoto;
import dev.velaron.fennec.model.Photo;
import dev.velaron.fennec.model.PhotoSize;
import dev.velaron.fennec.model.wrappers.SelectablePhotoWrapper;
import dev.velaron.fennec.settings.CurrentTheme;
import dev.velaron.fennec.upload.Upload;
import dev.velaron.fennec.util.AppTextUtils;
import dev.velaron.fennec.util.Logger;
import dev.velaron.fennec.util.Utils;
import dev.velaron.fennec.view.CircleRoadProgress;

import static dev.velaron.fennec.util.Objects.nonNull;

public class BigVkPhotosAdapter extends DifferentDataAdapter {

    public static final int DATA_TYPE_PHOTO = 1;
    public static final int DATA_TYPE_UPLOAD = 0;

    private static final String TAG = BigVkPhotosAdapter.class.getSimpleName();

    private static final int VIEW_TYPE_PHOTO = 0;
    private static final int VIEW_TYPE_UPLOAD = 1;

    private final SharedHolders<UploadViewHolder> mUploadViewHolders;
    private final Set<PhotoViewHolder> mPhotoHolders;

    private Context mContext;
    private int mColorPrimaryWithAlpha;
    private String mPicassoTag;
    private PhotosActionListener mPhotosActionListener;
    private UploadActionListener mUploadActionListener;

    public BigVkPhotosAdapter(Context context, @NonNull List<Upload> uploads, @NonNull List<SelectablePhotoWrapper> photoWrappers, String picassoTag){
        this.mContext = context;
        this.mPhotoHolders = new HashSet<>();
        this.mUploadViewHolders = new SharedHolders<>(false);
        this.mPicassoTag = picassoTag;
        this.mColorPrimaryWithAlpha = Utils.adjustAlpha(CurrentTheme.getColorPrimary(mContext), 0.75F);

        super.setData(DATA_TYPE_UPLOAD, uploads);
        super.setData(DATA_TYPE_PHOTO, photoWrappers);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType){
            case VIEW_TYPE_PHOTO:
                return new PhotoViewHolder(LayoutInflater.from(mContext).inflate(R.layout.vk_photo_item, parent, false));
            case VIEW_TYPE_UPLOAD:
                return new UploadViewHolder(LayoutInflater.from(mContext).inflate(R.layout.vk_upload_photo_item, parent, false));
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int adapterPosition) {
        switch (getItemViewType(adapterPosition)){
            case VIEW_TYPE_PHOTO:
                bindPhotoViewHolder((PhotoViewHolder) holder, getItem(adapterPosition));
                break;
            case VIEW_TYPE_UPLOAD:
                bindUploadViewHolder((UploadViewHolder) holder, getItem(adapterPosition));
                break;
        }
    }

    private void removePhotoViewHolderByTag(@NonNull SelectablePhotoWrapper tag){
        Iterator<PhotoViewHolder> iterator = mPhotoHolders.iterator();
        while (iterator.hasNext()){
            if(tag.equals(iterator.next().itemView.getTag())){
                iterator.remove();
            }
        }
    }

    private void bindUploadViewHolder(UploadViewHolder holder, final Upload upload){
        mUploadViewHolders.put(upload.getId(), holder);

        holder.setupProgress(upload.getStatus(), upload.getProgress(), false);
        holder.setupTitle(upload.getStatus(), upload.getProgress());

        PicassoInstance.with()
                .load(LocalPhoto.buildUriForPicasso(upload.getFileId()))
                .tag(mPicassoTag)
                .placeholder(R.drawable.background_gray)
                .into(holder.image);

        holder.progressRoot.setOnClickListener(v -> {
            if(mUploadActionListener != null){
                mUploadActionListener.onUploadRemoveClicked(upload);
            }
        });
    }

    private void bindPhotoViewHolder(final PhotoViewHolder holder, final SelectablePhotoWrapper photoWrapper){
        removePhotoViewHolderByTag(photoWrapper);
        holder.itemView.setTag(photoWrapper);
        mPhotoHolders.add(holder);
        Logger.d(TAG, "Added photo view holder, total size: " + mPhotoHolders.size());

        Photo photo = photoWrapper.getPhoto();

        holder.tvLike.setText(AppTextUtils.getCounterWithK(photo.getLikesCount()));
        holder.tvLike.setVisibility(photo.getLikesCount() > 0 ? View.VISIBLE : View.GONE);
        holder.ivLike.setVisibility(photo.getLikesCount() > 0 ? View.VISIBLE : View.GONE);

        holder.tvComment.setText(AppTextUtils.getCounterWithK(photo.getCommentsCount()));
        holder.tvComment.setVisibility(photo.getCommentsCount() > 0 ? View.VISIBLE : View.GONE);
        holder.ivComment.setVisibility(photo.getCommentsCount() > 0 ? View.VISIBLE : View.GONE);

        holder.bottomRoot.setBackgroundColor(mColorPrimaryWithAlpha);
        holder.bottomRoot.setVisibility(photo.getLikesCount() + photo.getCommentsCount() > 0 ? View.VISIBLE : View.GONE);

        holder.setSelected(photoWrapper.isSelected());
        holder.resolveIndexText(photoWrapper);

        String targetUrl = photo.getUrlForSize(PhotoSize.Q, false);

        PicassoInstance.with()
                .load(targetUrl)
                .tag(mPicassoTag)
                .placeholder(R.drawable.background_gray)
                .into(holder.photoImageView);

        View.OnClickListener clickListener = v -> {
            if(mPhotosActionListener != null){
                mPhotosActionListener.onPhotoClick(holder, photoWrapper);
            }
        };

        holder.photoImageView.setOnClickListener(clickListener);
        holder.index.setOnClickListener(clickListener);
        holder.darkView.setOnClickListener(clickListener);
    }

    @Override
    public int getItemViewType(int adapterPosition) {
        int dataType = getDataTypeByAdapterPosition(adapterPosition);

        switch (dataType){
            case DATA_TYPE_PHOTO:
                return VIEW_TYPE_PHOTO;
            case DATA_TYPE_UPLOAD:
                return VIEW_TYPE_UPLOAD;
        }

        throw new IllegalStateException("Unknown data type, dataType: " + dataType);
    }

    public void setPhotosActionListener(PhotosActionListener photosActionListener) {
        this.mPhotosActionListener = photosActionListener;
    }

    public void setUploadActionListener(UploadActionListener uploadActionListener) {
        this.mUploadActionListener = uploadActionListener;
    }

    public void updatePhotoHoldersSelectionAndIndexes(){
        for(PhotoViewHolder holder : mPhotoHolders){
            SelectablePhotoWrapper photo = (SelectablePhotoWrapper) holder.itemView.getTag();
            holder.setSelected(photo.isSelected());
            holder.resolveIndexText(photo);
        }
    }

    public void updateUploadHoldersProgress(int uploadId, boolean smoothly, int progress){
        UploadViewHolder holder = mUploadViewHolders.findOneByEntityId(uploadId);

        if(nonNull(holder)){
            if(smoothly){
                holder.progress.changePercentageSmoothly(progress);
            } else {
                holder.progress.changePercentage(progress);
            }

            String progressText = progress + "%";
            holder.title.setText(progressText);
        }
    }

    public void cleanup(){
        mPhotoHolders.clear();
        mUploadViewHolders.release();
    }

    public interface PhotosActionListener {
        void onPhotoClick(PhotoViewHolder holder, SelectablePhotoWrapper photoWrapper);
    }

    public interface UploadActionListener {
        void onUploadRemoveClicked(Upload upload);
    }

    private class UploadViewHolder extends RecyclerView.ViewHolder implements IdentificableHolder {

        ImageView image;
        ImageView tint;
        View progressRoot;
        CircleRoadProgress progress;
        TextView title;

        UploadViewHolder(View itemView) {
            super(itemView);
            super.itemView.setTag(generateNextHolderId());

            this.image = itemView.findViewById(R.id.image);
            this.tint = itemView.findViewById(R.id.tint);
            this.progressRoot = itemView.findViewById(R.id.progress_root);
            this.progress = itemView.findViewById(R.id.progress);
            this.title = itemView.findViewById(R.id.title);
        }

        void setupProgress(int status, int progressValue, boolean smoothly){
            if(smoothly && status == Upload.STATUS_UPLOADING){
                progress.changePercentageSmoothly(progressValue);
            } else {
                progress.setVisibility(status == Upload.STATUS_UPLOADING ? View.VISIBLE : View.GONE);
                progress.changePercentage(status == Upload.STATUS_UPLOADING ? progressValue : 0);
            }
        }

        void setupTitle(int status, int progress){
            switch (status){
                case Upload.STATUS_QUEUE:
                    title.setText(R.string.in_order);
                    break;
                case Upload.STATUS_UPLOADING:
                    String progressText = progress + "%";
                    title.setText(progressText);
                    break;
                case Upload.STATUS_ERROR:
                    title.setText(R.string.error);
                    break;
                case Upload.STATUS_CANCELLING:
                    title.setText(R.string.cancelling);
                    break;
            }
        }

        @Override
        public int getHolderId() {
            return (int) itemView.getTag();
        }
    }

    private static int holderIdGenerator;

    private static int generateNextHolderId(){
        holderIdGenerator++;
        return holderIdGenerator;
    }

    public class PhotoViewHolder extends RecyclerView.ViewHolder {

        ImageView photoImageView;
        TextView index;
        View darkView;
        ViewGroup bottomRoot;
        TextView tvLike;
        TextView tvComment;
        ImageView ivLike;
        ImageView ivComment;

        PhotoViewHolder(View itemView) {
            super(itemView);
            photoImageView = itemView.findViewById(R.id.imageView);
            index = itemView.findViewById(R.id.item_photo_index);
            darkView = itemView.findViewById(R.id.selected);
            bottomRoot = itemView.findViewById(R.id.vk_photo_item_bottom);
            ivLike = itemView.findViewById(R.id.vk_photo_item_like);
            tvLike = itemView.findViewById(R.id.vk_photo_item_like_counter);
            ivComment = itemView.findViewById(R.id.vk_photo_item_comment);
            tvComment = itemView.findViewById(R.id.vk_photo_item_comment_counter);
        }

        public void setSelected(boolean selected){
            index.setVisibility(selected ? View.VISIBLE : View.GONE);
            darkView.setVisibility(selected ? View.VISIBLE : View.GONE);
        }

        void resolveIndexText(SelectablePhotoWrapper photo) {
            index.setText(photo.getIndex() == 0 ? "" : String.valueOf(photo.getIndex()));
        }
    }
}