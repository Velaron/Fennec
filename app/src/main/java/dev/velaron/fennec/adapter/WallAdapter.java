package dev.velaron.fennec.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Transformation;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import dev.velaron.fennec.Constants;
import dev.velaron.fennec.R;
import dev.velaron.fennec.adapter.base.RecyclerBindableAdapter;
import dev.velaron.fennec.api.model.VKApiPost;
import dev.velaron.fennec.link.internal.LinkActionAdapter;
import dev.velaron.fennec.link.internal.OwnerLinkSpanFactory;
import dev.velaron.fennec.model.Attachments;
import dev.velaron.fennec.model.Post;
import dev.velaron.fennec.settings.CurrentTheme;
import dev.velaron.fennec.util.AppTextUtils;
import dev.velaron.fennec.util.ViewUtils;
import dev.velaron.fennec.view.CircleCounterButton;
import dev.velaron.fennec.view.emoji.EmojiconTextView;

import static dev.velaron.fennec.api.model.VkApiPostSource.Data.PROFILE_ACTIVITY;
import static dev.velaron.fennec.api.model.VkApiPostSource.Data.PROFILE_PHOTO;
import static dev.velaron.fennec.util.Objects.isNull;
import static dev.velaron.fennec.util.Objects.nonNull;
import static dev.velaron.fennec.util.Utils.intValueIn;
import static dev.velaron.fennec.util.Utils.isEmpty;
import static dev.velaron.fennec.util.Utils.nonEmpty;
import static dev.velaron.fennec.util.Utils.safeAllIsEmpty;

public class WallAdapter extends RecyclerBindableAdapter<Post, RecyclerView.ViewHolder> {

    private static final int TYPE_SCHEDULED = 2;
    private static final int TYPE_DELETED = 1;
    private static final int TYPE_NORMAL = 0;

    private Context mContext;
    private AttachmentsViewBinder attachmentsViewBinder;
    private Transformation transformation;
    private ClickListener clickListener;
    private NonPublishedPostActionListener nonPublishedPostActionListener;
    private LinkActionAdapter mLinkActionAdapter;

    public WallAdapter(Context context, List<Post> items, @NonNull AttachmentsViewBinder.OnAttachmentsActionCallback attachmentsActionCallback,
                       @NonNull ClickListener adapterListener) {
        super(items);
        this.mContext = context;
        this.attachmentsViewBinder = new AttachmentsViewBinder(context, attachmentsActionCallback);
        this.transformation = CurrentTheme.createTransformationForAvatar(context);
        this.clickListener = adapterListener;
        this.mLinkActionAdapter = new LinkActionAdapter() {
            @Override
            public void onOwnerClick(int ownerId) {
                clickListener.onAvatarClick(ownerId);
            }
        };
    }

    @Override
    protected void onBindItemViewHolder(RecyclerView.ViewHolder viewHolder, int position, int type) {
        Post item = getItem(position);
        switch (type) {
            case TYPE_NORMAL:
                NormalHolder normalHolder = (NormalHolder) viewHolder;
                configNormalPost(normalHolder, item);
                fillNormalPostButtonsBlock(normalHolder, item);
                break;
            case TYPE_SCHEDULED:
                ScheludedHolder scheludedHolder = (ScheludedHolder) viewHolder;
                configNormalPost(scheludedHolder, item);
                bindScheludedButtonsBlock(scheludedHolder, item);
                break;
            case TYPE_DELETED:
                bindDeleted((DeletedHolder) viewHolder, item);
                break;
        }
    }

    public void setNonPublishedPostActionListener(NonPublishedPostActionListener listener) {
        this.nonPublishedPostActionListener = listener;
    }

    private void bindScheludedButtonsBlock(ScheludedHolder holder, final Post post) {
        holder.deleteButton.setOnClickListener(v -> {
            if (nonPublishedPostActionListener != null) {
                nonPublishedPostActionListener.onButtonRemoveClick(post);
            }
        });
    }

    private void bindDeleted(final DeletedHolder holder, final Post item) {
        holder.bRestore.setOnClickListener(v -> clickListener.onRestoreClick(item));
    }

    private void configNormalPost(final AbsPostHolder holder, final Post post) {
        attachmentsViewBinder.displayAttachments(post.getAttachments(), holder.attachmentContainers, false);
        attachmentsViewBinder.displayCopyHistory(post.getCopyHierarchy(), holder.attachmentContainers.getVgPosts(), true, R.layout.item_copy_history_post);

        holder.tvOwnerName.setText(post.getAuthorName());

        String reduced = AppTextUtils.reduceStringForPost(post.getText());
        holder.tvText.setText(OwnerLinkSpanFactory.withSpans(reduced, true, false, mLinkActionAdapter));

        holder.tvShowMore.setVisibility(post.hasText() && post.getText().length() > 400 ? View.VISIBLE : View.GONE);

        holder.tvText.setVisibility(post.hasText() ? View.VISIBLE : View.GONE);
        holder.vTextContainer.setVisibility(post.hasText() ? View.VISIBLE : View.GONE);

        String ownerAvaUrl = post.getAuthorPhoto();
        ViewUtils.displayAvatar(holder.ivOwnerAvatar, transformation, ownerAvaUrl, Constants.PICASSO_TAG);

        holder.ivOwnerAvatar.setOnClickListener(v -> clickListener.onAvatarClick(post.getAuthorId()));

        holder.ivFriendOnly.setVisibility(post.isFriendsOnly() ? View.VISIBLE : View.GONE);

        boolean displaySigner = post.getSignerId() > 0 && nonNull(post.getCreator());

        holder.vSignerRoot.setVisibility(displaySigner ? View.VISIBLE : View.GONE);

        if (displaySigner) {
            holder.tvSignerName.setText(post.getCreator().getFullName());
            ViewUtils.displayAvatar(holder.ivSignerIcon, transformation, post.getCreator().get100photoOrSmaller(), Constants.PICASSO_TAG);

            holder.vSignerRoot.setOnClickListener(v -> clickListener.onAvatarClick(post.getSignerId()));
        }

        holder.root.setOnClickListener(v -> clickListener.onPostClick(post));

        holder.topDivider.setVisibility(View.GONE);
        holder.bottomDivider.setVisibility(needToShowBottomDivider(post) ? View.VISIBLE : View.GONE);

        if (holder.viewCounterRoot != null) {
            holder.viewCounterRoot.setVisibility(post.getViewCount() > 0 ? View.VISIBLE : View.GONE);
            holder.viewCounter.setText(String.valueOf(post.getViewCount()));
        }
    }

    private void fillNormalPostButtonsBlock(NormalHolder holder, final Post post) {
        holder.pinRoot.setVisibility(post.isPinned() ? View.VISIBLE : View.GONE);

        String formattedDate = AppTextUtils.getDateFromUnixTime(mContext, post.getDate());
        String postSubtitle = formattedDate;

        if (post.getSource() != null) {
            switch (post.getSource().getData()) {
                case PROFILE_ACTIVITY:
                    postSubtitle = mContext.getString(R.string.updated_status_at, formattedDate);
                    break;
                case PROFILE_PHOTO:
                    postSubtitle = mContext.getString(R.string.updated_profile_photo_at, formattedDate);
                    break;
            }

            if (post.getSource().getPlatform() != null) {
                switch (post.getSource().getPlatform()) {
                    case "instagram":
                        holder.ivPlatform.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.instagram));
                        holder.ivPlatform.setVisibility(View.VISIBLE);
                        break;
                }
            }
        }
        holder.tvTime.setText(postSubtitle);

        holder.likeButton.setIcon(post.isUserLikes() ? R.drawable.heart_filled : R.drawable.heart);
        holder.likeButton.setActive(post.isUserLikes());
        holder.likeButton.setCount(post.getLikesCount());

        holder.likeButton.setOnClickListener(v -> clickListener.onLikeClick(post));

        holder.likeButton.setOnLongClickListener(v -> {
            clickListener.onLikeLongClick(post);
            return true;
        });

        holder.commentsButton.setVisibility(post.isCanPostComment() || post.getCommentsCount() > 0 ? View.VISIBLE : View.INVISIBLE);
        holder.commentsButton.setCount(post.getCommentsCount());
        holder.commentsButton.setOnClickListener(view -> clickListener.onCommentsClick(post));

        holder.shareButton.setActive(post.isUserReposted());
        holder.shareButton.setCount(post.getRepostCount());
        holder.shareButton.setOnClickListener(v -> clickListener.onShareClick(post));

        holder.shareButton.setOnLongClickListener(v -> {
            clickListener.onShareLongClick(post);
            return true;
        });
    }

    public static boolean needToShowTopDivider(Post post) {
        if (!TextUtils.isEmpty(post.getText())) {
            return true;
        }

        Attachments attachments = post.getAttachments();
        // если есть копи-хистори и нет вложений фото-видео в главном посте
        if (nonEmpty(post.getCopyHierarchy()) && (isNull(attachments) || safeAllIsEmpty(attachments.getPhotos(), attachments.getVideos()))) {
            return true;
        }

        if (post.getAttachments() == null) {
            return true;
        }

        return safeAllIsEmpty(attachments.getPhotos(), attachments.getVideos());
    }

    public static boolean needToShowBottomDivider(Post post) {
        if (post.getSignerId() > 0 && nonNull(post.getCreator())) {
            return true;
        }

        if (isEmpty(post.getCopyHierarchy())) {
            return isNull(post.getAttachments()) || !post.getAttachments().isPhotosVideosGifsOnly();
        }

        Post last = post.getCopyHierarchy().get(post.getCopyHierarchy().size() - 1);
        return isNull(last.getAttachments()) || !last.getAttachments().isPhotosVideosGifsOnly();
    }

    @Override
    protected RecyclerView.ViewHolder viewHolder(View view, int type) {
        switch (type) {
            case TYPE_NORMAL:
                return new NormalHolder(view);
            case TYPE_DELETED:
                return new DeletedHolder(view);
            case TYPE_SCHEDULED:
                return new ScheludedHolder(view);
        }

        throw new IllegalArgumentException();
    }

    @Override
    protected int layoutId(int type) {
        switch (type) {
            case TYPE_DELETED:
                return R.layout.item_post_deleted;
            case TYPE_NORMAL:
                return R.layout.item_post_normal;
            case TYPE_SCHEDULED:
                return R.layout.item_post_scheduled;
        }

        throw new IllegalArgumentException();
    }

    public interface NonPublishedPostActionListener {
        void onButtonRemoveClick(Post post);
    }

    public interface ClickListener {
        void onAvatarClick(int ownerId);

        void onShareClick(Post post);

        void onPostClick(Post post);

        void onRestoreClick(Post post);

        void onCommentsClick(Post post);

        void onLikeLongClick(Post post);

        void onShareLongClick(Post post);

        void onLikeClick(Post post);
    }

    private class DeletedHolder extends RecyclerView.ViewHolder {

        Button bRestore;

        DeletedHolder(View itemView) {
            super(itemView);
            bRestore = itemView.findViewById(R.id.item_post_deleted_restore);
        }
    }

    private abstract class AbsPostHolder extends RecyclerView.ViewHolder {

        View root;
        View topDivider;
        TextView tvOwnerName;
        ImageView ivOwnerAvatar;
        View vTextContainer;
        EmojiconTextView tvText;
        TextView tvShowMore;
        TextView tvTime;
        ImageView ivFriendOnly;
        View viewCounterRoot;
        TextView viewCounter;
        View vSignerRoot;
        ImageView ivSignerIcon;
        TextView tvSignerName;
        View bottomDivider;
        ImageView ivPlatform;

        AttachmentsHolder attachmentContainers;

        AbsPostHolder(View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.card_view);
            topDivider = itemView.findViewById(R.id.top_divider);
            ivOwnerAvatar = itemView.findViewById(R.id.item_post_avatar);
            tvOwnerName = itemView.findViewById(R.id.item_post_owner_name);
            vTextContainer = itemView.findViewById(R.id.item_text_container);
            tvText = itemView.findViewById(R.id.item_post_text);
            tvText.setOnHashTagClickListener(mOnHashTagClickListener);
            tvShowMore = itemView.findViewById(R.id.item_post_show_more);
            tvTime = itemView.findViewById(R.id.item_post_time);
            bottomDivider = itemView.findViewById(R.id.bottom_divider);

            ivFriendOnly = itemView.findViewById(R.id.item_post_friedns_only);

            vSignerRoot = itemView.findViewById(R.id.item_post_signer_root);
            ivSignerIcon = itemView.findViewById(R.id.item_post_signer_icon);
            tvSignerName = itemView.findViewById(R.id.item_post_signer_name);
            ivPlatform = itemView.findViewById(R.id.platform_icon);
            this.attachmentContainers = AttachmentsHolder.forPost((ViewGroup) itemView);

            this.viewCounterRoot = itemView.findViewById(R.id.post_views_counter_root);
            this.viewCounter = itemView.findViewById(R.id.post_views_counter);
        }
    }

    private EmojiconTextView.OnHashTagClickListener mOnHashTagClickListener;

    public void setOnHashTagClickListener(EmojiconTextView.OnHashTagClickListener onHashTagClickListener) {
        this.mOnHashTagClickListener = onHashTagClickListener;
    }

    private class NormalHolder extends AbsPostHolder {

        View pinRoot;
        CircleCounterButton likeButton;
        CircleCounterButton shareButton;
        CircleCounterButton commentsButton;

        NormalHolder(View view) {
            super(view);
            pinRoot = root.findViewById(R.id.item_post_normal_pin_root);
            likeButton = root.findViewById(R.id.like_button);
            commentsButton = root.findViewById(R.id.comments_button);
            shareButton = root.findViewById(R.id.share_button);
        }
    }

    private class ScheludedHolder extends AbsPostHolder {

        CircleCounterButton deleteButton;

        ScheludedHolder(View view) {
            super(view);
            deleteButton = root.findViewById(R.id.button_delete);
        }
    }

    @Override
    protected int getItemType(int position) {
        Post post = getItem(position - getHeadersCount());

        if (post.isDeleted()) {
            return TYPE_DELETED;
        }

        return intValueIn(post.getPostType(), VKApiPost.Type.POSTPONE, VKApiPost.Type.SUGGEST) ? TYPE_SCHEDULED : TYPE_NORMAL;
    }
}
