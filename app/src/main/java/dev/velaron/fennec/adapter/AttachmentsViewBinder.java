package dev.velaron.fennec.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieAnimationView;
import com.squareup.picasso.Transformation;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Set;

import dev.velaron.fennec.Constants;
import dev.velaron.fennec.R;
import dev.velaron.fennec.adapter.holder.IdentificableHolder;
import dev.velaron.fennec.adapter.holder.SharedHolders;
import dev.velaron.fennec.api.PicassoInstance;
import dev.velaron.fennec.link.internal.LinkActionAdapter;
import dev.velaron.fennec.link.internal.OwnerLinkSpanFactory;
import dev.velaron.fennec.model.Attachments;
import dev.velaron.fennec.model.Audio;
import dev.velaron.fennec.model.Document;
import dev.velaron.fennec.model.Link;
import dev.velaron.fennec.model.Message;
import dev.velaron.fennec.model.Photo;
import dev.velaron.fennec.model.Poll;
import dev.velaron.fennec.model.Post;
import dev.velaron.fennec.model.Sticker;
import dev.velaron.fennec.model.Types;
import dev.velaron.fennec.model.Video;
import dev.velaron.fennec.model.VoiceMessage;
import dev.velaron.fennec.model.WikiPage;
import dev.velaron.fennec.settings.CurrentTheme;
import dev.velaron.fennec.util.AppTextUtils;
import dev.velaron.fennec.util.Objects;
import dev.velaron.fennec.util.Utils;
import dev.velaron.fennec.util.ViewUtils;
import dev.velaron.fennec.view.WaveFormView;
import dev.velaron.fennec.view.emoji.EmojiconTextView;

import static dev.velaron.fennec.util.Objects.isNull;
import static dev.velaron.fennec.util.Objects.nonNull;
import static dev.velaron.fennec.util.Utils.dpToPx;
import static dev.velaron.fennec.util.Utils.isEmpty;
import static dev.velaron.fennec.util.Utils.safeIsEmpty;
import static dev.velaron.fennec.util.Utils.safeLenghtOf;

public class AttachmentsViewBinder {

    private static final int PREFFERED_STICKER_SIZE = 120;
    private static int sHolderIdCounter;

    private PhotosViewHelper photosViewHelper;
    private Transformation mAvatarTransformation;

    private int mActiveWaveFormColor;
    private int mNoactiveWaveFormColor;
    private SharedHolders<VoiceHolder> mVoiceSharedHolders;

    private VoiceActionListener mVoiceActionListener;
    private OnAttachmentsActionCallback mAttachmentsActionCallback;
    private EmojiconTextView.OnHashTagClickListener mOnHashTagClickListener;
    private Context mContext;

    public AttachmentsViewBinder(Context context, @NonNull OnAttachmentsActionCallback attachmentsActionCallback) {
        this.mContext = context;
        this.mVoiceSharedHolders = new SharedHolders<>(true);
        this.mAvatarTransformation = CurrentTheme.createTransformationForAvatar(context);
        this.photosViewHelper = new PhotosViewHelper(context, attachmentsActionCallback);
        this.mAttachmentsActionCallback = attachmentsActionCallback;
        this.mActiveWaveFormColor = CurrentTheme.getColorPrimary(context);
        this.mNoactiveWaveFormColor = Utils.adjustAlpha(mActiveWaveFormColor, 0.5f);
    }

    private static void safeSetVisibitity(@Nullable View view, int visibility) {
        if (view != null) view.setVisibility(visibility);
    }

    private static final byte[] DEFAUL_WAVEFORM = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    public void setOnHashTagClickListener(EmojiconTextView.OnHashTagClickListener onHashTagClickListener) {
        this.mOnHashTagClickListener = onHashTagClickListener;
    }

    private static int generateHolderId() {
        sHolderIdCounter++;
        return sHolderIdCounter;
    }

    public void displayAttachments(Attachments attachments, AttachmentsHolder containers, boolean postsAsLinks) {
        if (attachments == null) {
            safeSetVisibitity(containers.getVgAudios(), View.GONE);
            safeSetVisibitity(containers.getVgDocs(), View.GONE);
            safeSetVisibitity(containers.getVgPhotos(), View.GONE);
            safeSetVisibitity(containers.getVgPosts(), View.GONE);
            safeSetVisibitity(containers.getVgStickers(), View.GONE);
            safeSetVisibitity(containers.getVoiceMessageRoot(), View.GONE);
            safeSetVisibitity(containers.getVgFriends(), View.GONE);
        } else {
            displayAudios(attachments.getAudios(), containers.getVgAudios());
            displayVoiceMessages(attachments.getVoiceMessages(), containers.getVoiceMessageRoot());
            displayDocs(attachments.getDocLinks(postsAsLinks, true), containers.getVgDocs());

            if (containers.getVgStickers() != null) {
                displayStickers(attachments.getStickers(), containers.getVgStickers());
            }

            photosViewHelper.displayPhotos(attachments.getPostImages(), containers.getVgPhotos());
        }
    }

    private void displayVoiceMessages(final ArrayList<VoiceMessage> voices, ViewGroup container) {
        if (Objects.isNull(container)) return;

        boolean empty = safeIsEmpty(voices);
        container.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (empty) {
            return;
        }

        int i = voices.size() - container.getChildCount();
        for (int j = 0; j < i; j++) {
            container.addView(LayoutInflater.from(mContext).inflate(R.layout.item_voice_message, container, false));
        }

        for (int g = 0; g < container.getChildCount(); g++) {
            ViewGroup root = (ViewGroup) container.getChildAt(g);

            if (g < voices.size()) {
                VoiceHolder holder = (VoiceHolder) root.getTag();
                if (holder == null) {
                    holder = new VoiceHolder(root);
                    root.setTag(holder);
                }

                final VoiceMessage voice = voices.get(g);
                bindVoiceHolder(holder, voice);

                root.setVisibility(View.VISIBLE);
            } else {
                root.setVisibility(View.GONE);
            }
        }
    }

    public void bindVoiceHolderById(int holderId, boolean play, boolean paused, float progress, boolean amin) {
        VoiceHolder holder = mVoiceSharedHolders.findHolderByHolderId(holderId);
        if (nonNull(holder)) {
            bindVoiceHolderPlayState(holder, play, paused, progress, amin);
        }
    }

    private void bindVoiceHolderPlayState(VoiceHolder holder, boolean play, boolean paused, float progress, boolean anim) {
        @DrawableRes
        int icon = play && !paused ? R.drawable.pause : R.drawable.play;

        holder.mButtonPlay.setImageResource(icon);
        holder.mWaveFormView.setCurrentActiveProgress(play ? progress : 1.0f, anim);
    }

    public void configNowVoiceMessagePlaying(int voiceMessageId, float progress, boolean paused, boolean amin) {
        SparseArray<Set<WeakReference<VoiceHolder>>> holders = mVoiceSharedHolders.getCache();
        for (int i = 0; i < holders.size(); i++) {
            int key = holders.keyAt(i);

            boolean play = key == voiceMessageId;

            Set<WeakReference<VoiceHolder>> set = holders.get(key);
            for (WeakReference<VoiceHolder> reference : set) {
                VoiceHolder holder = reference.get();
                if (nonNull(holder)) {
                    bindVoiceHolderPlayState(holder, play, paused, progress, amin);
                }
            }
        }
    }

    public void setVoiceActionListener(VoiceActionListener voiceActionListener) {
        this.mVoiceActionListener = voiceActionListener;
    }

    public void disableVoiceMessagePlaying() {
        SparseArray<Set<WeakReference<VoiceHolder>>> holders = mVoiceSharedHolders.getCache();
        for (int i = 0; i < holders.size(); i++) {
            int key = holders.keyAt(i);
            Set<WeakReference<VoiceHolder>> set = holders.get(key);
            for (WeakReference<VoiceHolder> reference : set) {
                VoiceHolder holder = reference.get();
                if (nonNull(holder)) {
                    bindVoiceHolderPlayState(holder, false, false, 0f, false);
                }
            }
        }
    }

    private void bindVoiceHolder(@NonNull VoiceHolder holder, @NonNull VoiceMessage voice) {
        int voiceMessageId = voice.getId();
        mVoiceSharedHolders.put(voiceMessageId, holder);

        holder.mDurationText.setText(AppTextUtils.getDurationString(voice.getDuration()));

        // can bee NULL/empty
        if (nonNull(voice.getWaveform()) && voice.getWaveform().length > 0) {
            holder.mWaveFormView.setWaveForm(voice.getWaveform());
        } else {
            holder.mWaveFormView.setWaveForm(DEFAUL_WAVEFORM);
        }

        holder.mButtonPlay.setOnClickListener(v -> {
            if (nonNull(mVoiceActionListener)) {
                mVoiceActionListener.onVoicePlayButtonClick(holder.getHolderId(), voiceMessageId, voice);
            }
        });

        if (nonNull(mVoiceActionListener)) {
            mVoiceActionListener.onVoiceHolderBinded(voiceMessageId, holder.getHolderId());
        }
    }

    private void displayStickers(List<Sticker> stickers, ViewGroup stickersContainer) {
        stickersContainer.setVisibility(safeIsEmpty(stickers) ? View.GONE : View.VISIBLE);
        if (isEmpty(stickers)) {
            return;
        }

        if (stickersContainer.getChildCount() == 0) {
            LottieAnimationView localView = new LottieAnimationView(mContext);
            stickersContainer.addView(localView);
        }

        LottieAnimationView imageView = (LottieAnimationView) stickersContainer.getChildAt(0);
        Sticker sticker = stickers.get(0);

        int prefferedStickerSize = (int) dpToPx(PREFFERED_STICKER_SIZE, mContext);
        Sticker.Image image = sticker.getImage(256, true);

        boolean horisontal = image.getHeight() < image.getWidth();
        double proporsion = (double) image.getWidth() / (double) image.getHeight();

        final float finalWidth;
        final float finalHeihgt;

        if (horisontal) {
            finalWidth = prefferedStickerSize;
            finalHeihgt = (float) (finalWidth / proporsion);
        } else {
            finalHeihgt = prefferedStickerSize;
            finalWidth = (float) (finalHeihgt * proporsion);
        }

        imageView.getLayoutParams().height = (int) finalHeihgt;
        imageView.getLayoutParams().width = (int) finalWidth;

        if (sticker.isAnimated()) {
            imageView.setAnimationFromUrl(sticker.getAnimationUrl());
            imageView.setRepeatCount(3);
            imageView.playAnimation();
            stickersContainer.setOnLongClickListener(e -> {
                imageView.playAnimation();
                return true;
            });
        } else {
            PicassoInstance.with()
                    .load(image.getUrl())
                    .into(imageView);
        }
        stickersContainer.setOnClickListener(e -> openSticker(sticker));
    }

    public void displayCopyHistory(List<Post> posts, ViewGroup container, boolean reduce, int layout) {
        if (container != null) {
            container.setVisibility(safeIsEmpty(posts) ? View.GONE : View.VISIBLE);
        }

        if (safeIsEmpty(posts) || container == null) {
            return;
        }

        int i = posts.size() - container.getChildCount();
        for (int j = 0; j < i; j++) {
            View itemView = LayoutInflater.from(container.getContext()).inflate(layout, container, false);
            CopyHolder holder = new CopyHolder((ViewGroup) itemView, mAttachmentsActionCallback);
            itemView.setTag(holder);

            if (!reduce) {
                holder.bodyView.setAutoLinkMask(Linkify.WEB_URLS);
                holder.bodyView.setMovementMethod(LinkMovementMethod.getInstance());
            }

            container.addView(itemView);
        }

        for (int g = 0; g < container.getChildCount(); g++) {
            ViewGroup postViewGroup = (ViewGroup) container.getChildAt(g);

            if (g < posts.size()) {
                final CopyHolder holder = (CopyHolder) postViewGroup.getTag();
                final Post copy = posts.get(g);

                if (isNull(copy)) {
                    postViewGroup.setVisibility(View.GONE);
                    return;
                }

                postViewGroup.setVisibility(View.VISIBLE);

                String text = reduce ? AppTextUtils.reduceStringForPost(copy.getText()) : copy.getText();

                holder.bodyView.setVisibility(isEmpty(copy.getText()) ? View.GONE : View.VISIBLE);
                holder.bodyView.setOnHashTagClickListener(mOnHashTagClickListener);
                holder.bodyView.setText(OwnerLinkSpanFactory.withSpans(text, true, false, new LinkActionAdapter() {
                    @Override
                    public void onOwnerClick(int ownerId) {
                        mAttachmentsActionCallback.onOpenOwner(ownerId);
                    }
                }));

                holder.ivAvatar.setOnClickListener(v -> mAttachmentsActionCallback.onOpenOwner(copy.getAuthorId()));
                ViewUtils.displayAvatar(holder.ivAvatar, mAvatarTransformation, copy.getAuthorPhoto(), Constants.PICASSO_TAG);

                holder.tvShowMore.setVisibility(reduce && safeLenghtOf(copy.getText()) > 400 ? View.VISIBLE : View.GONE);
                holder.ownerName.setText(copy.getAuthorName());
                holder.buttonDots.setTag(copy);

                displayAttachments(copy.getAttachments(), holder.attachmentsHolder, false);
            } else {
                postViewGroup.setVisibility(View.GONE);
            }
        }
    }

    public void displayForwards(List<Message> fwds, final ViewGroup fwdContainer, final Context context, boolean postsAsLinks) {
        fwdContainer.setVisibility(safeIsEmpty(fwds) ? View.GONE : View.VISIBLE);
        if (safeIsEmpty(fwds)) {
            return;
        }

        final int i = fwds.size() - fwdContainer.getChildCount();
        for (int j = 0; j < i; j++) {
            View localView = LayoutInflater.from(context).inflate(R.layout.item_forward_message, fwdContainer, false);
            fwdContainer.addView(localView);
        }

        for (int g = 0; g < fwdContainer.getChildCount(); g++) {
            final ViewGroup itemView = (ViewGroup) fwdContainer.getChildAt(g);
            if (g < fwds.size()) {
                final Message message = fwds.get(g);
                itemView.setVisibility(View.VISIBLE);
                itemView.setTag(message);

                TextView tvBody = itemView.findViewById(R.id.item_fwd_message_text);
                tvBody.setText(message.getBody());
                tvBody.setVisibility(message.getBody() == null || message.getBody().length() == 0 ? View.GONE : View.VISIBLE);

                ((TextView) itemView.findViewById(R.id.item_fwd_message_username)).setText(message.getSender().getFullName());
                ((TextView) itemView.findViewById(R.id.item_fwd_message_time)).setText(AppTextUtils.getDateFromUnixTime(message.getDate()));
                TextView tvFwds = itemView.findViewById(R.id.item_forward_message_fwds);
                tvFwds.setVisibility(message.getForwardMessagesCount() > 0 ? View.VISIBLE : View.GONE);

                tvFwds.setOnClickListener(v -> mAttachmentsActionCallback.onForwardMessagesOpen(message.getFwd()));

                final ImageView ivAvatar = itemView.findViewById(R.id.item_fwd_message_avatar);

                String senderPhotoUrl = message.getSender() == null ? null : message.getSender().getMaxSquareAvatar();
                ViewUtils.displayAvatar(ivAvatar, mAvatarTransformation, senderPhotoUrl, Constants.PICASSO_TAG);

                ivAvatar.setOnClickListener(v -> mAttachmentsActionCallback.onOpenOwner(message.getSenderId()));

                AttachmentsHolder attachmentContainers = new AttachmentsHolder();
                attachmentContainers.setVgAudios(itemView.findViewById(R.id.audio_attachments)).
                        setVgDocs(itemView.findViewById(R.id.docs_attachments)).
                        setVgPhotos(itemView.findViewById(R.id.photo_attachments)).
                        setVgPosts(itemView.findViewById(R.id.posts_attachments)).
                        setVgStickers(itemView.findViewById(R.id.stickers_attachments)).
                        setVoiceMessageRoot(itemView.findViewById(R.id.voice_message_attachments));

                displayAttachments(message.getAttachments(), attachmentContainers, postsAsLinks);
            } else {
                itemView.setVisibility(View.GONE);
                itemView.setTag(null);
            }
        }
    }

    private void displayDocs(List<DocLink> docs, ViewGroup root) {
        root.setVisibility(safeIsEmpty(docs) ? View.GONE : View.VISIBLE);
        if (safeIsEmpty(docs)) {
            return;
        }

        int i = docs.size() - root.getChildCount();
        for (int j = 0; j < i; j++) {
            root.addView(LayoutInflater.from(mContext).inflate(R.layout.item_document, root, false));
        }

        for (int g = 0; g < root.getChildCount(); g++) {
            ViewGroup itemView = (ViewGroup) root.getChildAt(g);
            if (g < docs.size()) {
                final DocLink doc = docs.get(g);
                itemView.setVisibility(View.VISIBLE);
                itemView.setTag(doc);

                TextView tvTitle = itemView.findViewById(R.id.item_document_title);
                TextView tvDetails = itemView.findViewById(R.id.item_document_ext_size);
                ImageView ivPhoto = itemView.findViewById(R.id.item_document_image);
                ImageView ivType = itemView.findViewById(R.id.item_document_type);

                String title = doc.getTitle(mContext);
                String details = doc.getSecondaryText(mContext);

                String imageUrl = doc.getImageUrl();
                String ext = doc.getExt() == null ? "" : doc.getExt() + ", ";

                String subtitle = ext + details;

                tvTitle.setText(title);
                tvDetails.setText(subtitle);

                itemView.setOnClickListener(v -> openDocLink(doc));

                switch (doc.getType()) {
                    case Types.DOC:
                        if (imageUrl != null) {
                            ivType.setVisibility(View.GONE);
                            ivPhoto.setVisibility(View.VISIBLE);
                            PicassoInstance.with()
                                    .load(imageUrl)
                                    .into(ivPhoto);
                        } else {
                            ivType.setVisibility(View.VISIBLE);
                            ivPhoto.setVisibility(View.GONE);
                            ivType.getBackground().setColorFilter(CurrentTheme.getColorPrimary(mContext), PorterDuff.Mode.MULTIPLY);
                            ivType.setImageResource(R.drawable.file);
                        }
                        break;
                    case Types.POST:
                        if (imageUrl != null) {
                            ivType.setVisibility(View.GONE);
                            ivPhoto.setVisibility(View.VISIBLE);
                            PicassoInstance.with()
                                    .load(imageUrl)
                                    .into(ivPhoto);
                        } else {
                            ivPhoto.setVisibility(View.GONE);
                            ivType.setVisibility(View.GONE);
                        }
                        break;
                    case Types.LINK:
                    case Types.WIKI_PAGE:
                        ivType.setVisibility(View.VISIBLE);
                        ivPhoto.setVisibility(View.GONE);
                        ivType.getBackground().setColorFilter(CurrentTheme.getColorPrimary(mContext), PorterDuff.Mode.MULTIPLY);
                        ivType.setImageResource(R.drawable.share);
                        break;
                    case Types.POLL:
                        ivType.setVisibility(View.VISIBLE);
                        ivPhoto.setVisibility(View.GONE);
                        ivType.getBackground().setColorFilter(CurrentTheme.getColorPrimary(mContext), PorterDuff.Mode.MULTIPLY);
                        ivType.setImageResource(R.drawable.chart_bar);
                        break;
                    default:
                        ivType.setVisibility(View.GONE);
                        ivPhoto.setVisibility(View.GONE);
                        break;
                }

            } else {
                itemView.setVisibility(View.GONE);
                itemView.setTag(null);
            }
        }
    }

    private void openSticker(Sticker sticker) {

    }

    public interface VoiceActionListener extends EventListener {
        void onVoiceHolderBinded(int voiceMessageId, int voiceHolderId);

        void onVoicePlayButtonClick(int voiceHolderId, int voiceMessageId, @NonNull VoiceMessage voiceMessage);
    }

    private void openDocLink(DocLink link) {
        switch (link.getType()) {
            case Types.DOC:
                mAttachmentsActionCallback.onDocPreviewOpen((Document) link.attachment);
                break;
            case Types.POST:
                mAttachmentsActionCallback.onPostOpen((Post) link.attachment);
                break;
            case Types.LINK:
                mAttachmentsActionCallback.onLinkOpen((Link) link.attachment);
                break;
            case Types.POLL:
                mAttachmentsActionCallback.onPollOpen((Poll) link.attachment);
                break;
            case Types.WIKI_PAGE:
                mAttachmentsActionCallback.onWikiPageOpen((WikiPage) link.attachment);
                break;
        }
    }

    /**
     * Отображение аудиозаписей
     *
     * @param audios    аудиозаписи
     * @param container контейнер для аудиозаписей
     */
    private void displayAudios(final ArrayList<Audio> audios, ViewGroup container) {
        container.setVisibility(safeIsEmpty(audios) ? View.GONE : View.VISIBLE);
        if (safeIsEmpty(audios)) {
            return;
        }

        int i = audios.size() - container.getChildCount();
        for (int j = 0; j < i; j++) {
            container.addView(LayoutInflater.from(mContext).inflate(R.layout.item_small_audio, container, false));
        }

        for (int g = 0; g < container.getChildCount(); g++) {
            ViewGroup root = (ViewGroup) container.getChildAt(g);
            if (g < audios.size()) {
                final Audio audio = audios.get(g);

                AudioHolder holder = new AudioHolder(root);

                holder.tvTitle.setText(audio.getArtist());
                holder.tvSubtitle.setText(audio.getTitle());

                int finalG = g;
                holder.ibPlay.setOnClickListener(v -> mAttachmentsActionCallback.onAudioPlay(finalG, audios));

                root.setVisibility(View.VISIBLE);
                root.setTag(audio);
            } else {
                root.setVisibility(View.GONE);
                root.setTag(null);
            }
        }
    }

    private static final class CopyHolder {

        final ViewGroup itemView;
        final ImageView ivAvatar;
        final TextView ownerName;
        final EmojiconTextView bodyView;
        final View tvShowMore;
        final View buttonDots;
        final AttachmentsHolder attachmentsHolder;

        CopyHolder(ViewGroup itemView, OnAttachmentsActionCallback callback) {
            this.itemView = itemView;
            this.bodyView = itemView.findViewById(R.id.item_post_copy_text);
            this.ivAvatar = itemView.findViewById(R.id.item_copy_history_post_avatar);
            this.tvShowMore = itemView.findViewById(R.id.item_post_copy_show_more);
            this.ownerName = itemView.findViewById(R.id.item_post_copy_owner_name);
            this.buttonDots = itemView.findViewById(R.id.item_copy_history_post_dots);
            this.attachmentsHolder = AttachmentsHolder.forCopyPost(itemView);
            this.callback = callback;

            this.buttonDots.setOnClickListener(v -> showDotsMenu());
        }

        final OnAttachmentsActionCallback callback;

        void showDotsMenu() {
            PopupMenu menu = new PopupMenu(itemView.getContext(), buttonDots);
            menu.getMenu().add(R.string.open_post).setOnMenuItemClickListener(item -> {
                Post copy = (Post) buttonDots.getTag();
                callback.onPostOpen(copy);
                return true;
            });

            menu.show();
        }
    }

    public interface OnAttachmentsActionCallback {
        void onPollOpen(@NonNull Poll poll);

        void onVideoPlay(@NonNull Video video);

        void onAudioPlay(int position, @NonNull ArrayList<Audio> audios);

        void onForwardMessagesOpen(@NonNull ArrayList<Message> messages);

        void onOpenOwner(int userId);

        void onDocPreviewOpen(@NonNull Document document);

        void onPostOpen(@NonNull Post post);

        void onLinkOpen(@NonNull Link link);

        void onWikiPageOpen(@NonNull WikiPage page);

        void onStickerOpen(@NonNull Sticker sticker);

        void onPhotosOpen(@NonNull ArrayList<Photo> photos, int index);
    }

    private static class AudioHolder {

        TextView tvTitle;
        TextView tvSubtitle;
        ImageView ibPlay;

        AudioHolder(View root) {
            tvTitle = root.findViewById(R.id.dialog_message);
            tvSubtitle = root.findViewById(R.id.item_audio_subtitle);
            ibPlay = root.findViewById(R.id.item_audio_play);
        }
    }

    private class VoiceHolder implements IdentificableHolder {

        WaveFormView mWaveFormView;
        ImageView mButtonPlay;
        TextView mDurationText;

        VoiceHolder(View itemView) {
            mWaveFormView = itemView.findViewById(R.id.item_voice_wave_form_view);
            mWaveFormView.setActiveColor(mActiveWaveFormColor);
            mWaveFormView.setNoactiveColor(mNoactiveWaveFormColor);
            mWaveFormView.setSectionCount(Utils.isLandscape(itemView.getContext()) ? 128 : 64);
            mWaveFormView.setTag(generateHolderId());

            mButtonPlay = itemView.findViewById(R.id.item_voice_button_play);
            mButtonPlay.getBackground().setColorFilter(mActiveWaveFormColor, PorterDuff.Mode.MULTIPLY);

            mDurationText = itemView.findViewById(R.id.item_voice_duration);
        }

        @Override
        public int getHolderId() {
            return (int) mWaveFormView.getTag();
        }
    }
}
