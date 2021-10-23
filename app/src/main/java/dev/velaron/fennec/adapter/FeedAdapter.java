package dev.velaron.fennec.adapter;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Transformation;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import dev.velaron.fennec.Constants;
import dev.velaron.fennec.R;
import dev.velaron.fennec.adapter.base.RecyclerBindableAdapter;
import dev.velaron.fennec.adapter.holder.IdentificableHolder;
import dev.velaron.fennec.link.internal.LinkActionAdapter;
import dev.velaron.fennec.link.internal.OwnerLinkSpanFactory;
import dev.velaron.fennec.model.News;
import dev.velaron.fennec.model.Post;
import dev.velaron.fennec.settings.CurrentTheme;
import dev.velaron.fennec.util.AppTextUtils;
import dev.velaron.fennec.util.Utils;
import dev.velaron.fennec.util.ViewUtils;
import dev.velaron.fennec.view.CircleCounterButton;

import static dev.velaron.fennec.util.Utils.safeAllIsEmpty;
import static dev.velaron.fennec.util.Utils.safeLenghtOf;

public class FeedAdapter extends RecyclerBindableAdapter<News, FeedAdapter.PostHolder> {

    private final Activity context;
    private AttachmentsViewBinder attachmentsViewBinder;
    private Transformation transformation;
    private ClickListener clickListener;

    public FeedAdapter(Activity context, List<News> data, AttachmentsViewBinder.OnAttachmentsActionCallback attachmentsActionCallback) {
        super(data);
        this.context = context;
        this.attachmentsViewBinder = new AttachmentsViewBinder(context, attachmentsActionCallback);
        this.transformation = CurrentTheme.createTransformationForAvatar(context);
    }

    @Override
    protected void onBindItemViewHolder(final PostHolder holder, int position, int type) {
        final News item = getItem(position);

        attachmentsViewBinder.displayAttachments(item.getAttachments(), holder.attachmentsHolder, false);
        attachmentsViewBinder.displayCopyHistory(item.getCopyHistory(), holder.attachmentsHolder.getVgPosts(), true, R.layout.item_copy_history_post);

        holder.tvOwnerName.setText(item.getOwnerName());

        String result = AppTextUtils.reduceStringForPost(item.getText());
        holder.tvText.setText(OwnerLinkSpanFactory.withSpans(result, true, false, new LinkActionAdapter() {
            @Override
            public void onOwnerClick(int ownerId) {
                if (clickListener != null) {
                    clickListener.onAvatarClick(ownerId);
                }
            }
        }));

        holder.tvShowMore.setVisibility(safeLenghtOf(item.getText()) > 400 ? View.VISIBLE : View.GONE);

        /*
        if (item.getSource() != null){
            switch (item.getSource().data){
                case PROFILE_ACTIVITY:
                    postSubtitle = context.getString(R.string.updated_status_at) + SPACE + AppTextUtils.getDateFromUnixTime(context, item.getDate());
                    break;
                case PROFILE_PHOTO:
                    postSubtitle = context.getString(R.string.updated_profile_photo_at) + SPACE + AppTextUtils.getDateFromUnixTime(context, item.getDate());
                    break;
            }
        }
        */

        String postTime = AppTextUtils.getDateFromUnixTime(context, item.getDate());
        holder.tvTime.setText(postTime);

        holder.vTextRoot.setVisibility(TextUtils.isEmpty(item.getText()) ? View.GONE : View.VISIBLE);

        String ownerAvaUrl = item.getOwnerMaxSquareAvatar();
        ViewUtils.displayAvatar(holder.ivOwnerAvatar, transformation, ownerAvaUrl, Constants.PICASSO_TAG);

        holder.ivOwnerAvatar.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onAvatarClick(item.getSourceId());
            }
        });

        fillCounters(holder, item);

        holder.cardView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onPostClick(item);
            }
        });

        holder.topDivider.setVisibility(View.GONE);
        holder.bottomDivider.setVisibility(needToShowBottomDivider(item) ? View.VISIBLE : View.GONE);

        holder.viewsCounterRoot.setVisibility(item.getViewCount() > 0 ? View.VISIBLE : View.GONE);
        //holder.viewsCounter.setText(String.valueOf(item.getViewCount()));

        ViewUtils.setCountText(holder.viewsCounter, item.getViewCount(), false);
    }

    @Override
    protected PostHolder viewHolder(View view, int type) {
        return new PostHolder(view);
    }

    @Override
    protected int layoutId(int type) {
        return R.layout.item_feed;
    }

    private int nextHolderId;

    private int genereateHolderId(){
        nextHolderId++;
        return nextHolderId;
    }

    class PostHolder extends RecyclerView.ViewHolder implements IdentificableHolder {

        private View cardView;

        View topDivider;
        TextView tvOwnerName;
        ImageView ivOwnerAvatar;

        View vTextRoot;
        TextView tvText;
        TextView tvShowMore;
        TextView tvTime;

        View bottomDivider;

        ViewGroup bottomActionsContainer;
        CircleCounterButton likeButton;
        CircleCounterButton shareButton;
        CircleCounterButton commentsButton;

        AttachmentsHolder attachmentsHolder;

        View viewsCounterRoot;
        TextView viewsCounter;

        PostHolder(View root) {
            super(root);
            cardView = root.findViewById(R.id.card_view);
            cardView.setTag(genereateHolderId());

            topDivider = root.findViewById(R.id.top_divider);
            ivOwnerAvatar = root.findViewById(R.id.item_post_avatar);
            tvOwnerName = root.findViewById(R.id.item_post_owner_name);
            vTextRoot = root.findViewById(R.id.item_text_container);
            tvText = root.findViewById(R.id.item_post_text);
            tvShowMore = root.findViewById(R.id.item_post_show_more);
            tvTime = root.findViewById(R.id.item_post_time);
            bottomDivider = root.findViewById(R.id.bottom_divider);
            bottomActionsContainer = root.findViewById(R.id.buttons_bar);
            likeButton = root.findViewById(R.id.like_button);
            commentsButton = root.findViewById(R.id.comments_button);
            shareButton = root.findViewById(R.id.share_button);

            attachmentsHolder = AttachmentsHolder.forPost((ViewGroup) root);
            this.viewsCounterRoot = itemView.findViewById(R.id.post_views_counter_root);
            this.viewsCounter = itemView.findViewById(R.id.post_views_counter);
        }

        @Override
        public int getHolderId() {
            return (int) cardView.getTag();
        }
    }

    private void fillCounters(PostHolder holder, final News news) {
        int targetLikeRes = news.isUserLike() ? R.drawable.heart_filled : R.drawable.heart;
        holder.likeButton.setIcon(targetLikeRes);

        holder.likeButton.setActive(news.isUserLike());
        holder.likeButton.setCount(news.getLikeCount());

        holder.likeButton.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onLikeClick(news, !news.isUserLike());
            }
        });

        holder.likeButton.setOnLongClickListener(v -> clickListener != null && clickListener.onLikeLongClick(news));

        holder.commentsButton.setVisibility(news.isCommentCanPost() || news.getCommentCount() > 0 ? View.VISIBLE : View.INVISIBLE);
        holder.commentsButton.setCount(news.getCommentCount());
        holder.commentsButton.setOnClickListener(view -> {
            if (clickListener != null) {
                clickListener.onCommentButtonClick(news);
            }
        });

        holder.shareButton.setActive(news.isUserReposted());
        holder.shareButton.setCount(news.getRepostsCount());
        holder.shareButton.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onRepostClick(news);
            }
        });

        holder.shareButton.setOnLongClickListener(v -> clickListener != null && clickListener.onShareLongClick(news));
    }

    private static boolean needToShowTopDivider(News news) {
        if (!TextUtils.isEmpty(news.getText())) {
            return true;
        }

        if (!Utils.safeIsEmpty(news.getCopyHistory()) && (news.getAttachments() == null || safeAllIsEmpty(news.getAttachments().getPhotos(), news.getAttachments().getVideos()))) {
            return true;
        }

        if (news.getAttachments() == null) {
            return true;
        }

        return safeAllIsEmpty(news.getAttachments().getPhotos(), news.getAttachments().getVideos());
    }

    private static boolean needToShowBottomDivider(News news) {
        if (Utils.safeIsEmpty(news.getCopyHistory())) {
            return news.getAttachments() == null || !news.getAttachments().isPhotosVideosGifsOnly();
        }

        Post last = news.getCopyHistory().get(news.getCopyHistory().size() - 1);
        return last.getAttachments() == null || !last.getAttachments().isPhotosVideosGifsOnly();
    }

    public interface ClickListener {
        void onAvatarClick(int ownerId);

        void onRepostClick(News news);

        void onPostClick(News news);

        void onCommentButtonClick(News news);

        void onLikeClick(News news, boolean add);

        boolean onLikeLongClick(News news);

        boolean onShareLongClick(News news);
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }
}
