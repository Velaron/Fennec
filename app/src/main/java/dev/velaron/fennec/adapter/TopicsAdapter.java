package dev.velaron.fennec.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Transformation;

import java.util.EventListener;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import dev.velaron.fennec.R;
import dev.velaron.fennec.adapter.base.RecyclerBindableAdapter;
import dev.velaron.fennec.api.PicassoInstance;
import dev.velaron.fennec.model.Topic;
import dev.velaron.fennec.settings.CurrentTheme;
import dev.velaron.fennec.util.AppTextUtils;
import dev.velaron.fennec.util.Objects;
import dev.velaron.fennec.util.Utils;

import static dev.velaron.fennec.util.Utils.isEmpty;

public class TopicsAdapter extends RecyclerBindableAdapter<Topic, TopicsAdapter.ViewHolder> {

    private Transformation transformation;
    private ActionListener mActionListener;
    private int firstLastPadding = 0;

    public TopicsAdapter(Context context, List<Topic> topics, @NonNull ActionListener actionListener){
        super(topics);
        this.mActionListener = actionListener;
        this.transformation = CurrentTheme.createTransformationForAvatar(context);

        if(Utils.is600dp(context)){
            firstLastPadding = (int) Utils.dpToPx(16, context);
        }
    }

    @Override
    protected void onBindItemViewHolder(ViewHolder holder, int position, int type) {
        final Topic item = getItem(position - getHeadersCount());
        Context context = holder.itemView.getContext();

        holder.title.setText(item.getTitle());
        holder.subtitle.setText(context.getString(R.string.topic_comments_counter,
                AppTextUtils.getDateFromUnixTime(item.getLastUpdateTime()), item.getCommentsCount()));

        String avaUrl = Objects.isNull(item.getCreator()) ? null : item.getCreator().getMaxSquareAvatar();

        if(isEmpty(avaUrl)){
            PicassoInstance.with()
                    .load(R.drawable.ic_avatar_unknown)
                    .transform(transformation)
                    .into(holder.creator);
        } else {
            PicassoInstance.with()
                    .load(avaUrl)
                    .transform(transformation)
                    .into(holder.creator);
        }

        holder.itemView.setPadding(holder.itemView.getPaddingLeft(),
                position == 0 ? firstLastPadding : 0,
                holder.itemView.getPaddingRight(),
                position == getItemCount() - 1 ? firstLastPadding : 0);

        holder.itemView.setOnClickListener(view -> mActionListener.onTopicClick(item));
    }

    public interface ActionListener extends EventListener {
        void onTopicClick(@NonNull Topic topic);
    }

    @Override
    protected ViewHolder viewHolder(View view, int type) {
        return new ViewHolder(view);
    }

    @Override
    protected int layoutId(int type) {
        return R.layout.item_topic;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private ViewHolder(View root){
            super(root);
            title = root.findViewById(R.id.item_topic_title);
            subtitle = root.findViewById(R.id.item_topic_subtitle);
            creator = root.findViewById(R.id.item_topicstarter_avatar);
        }

        private TextView title;
        private TextView subtitle;
        private ImageView creator;
    }
}
