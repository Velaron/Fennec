package dev.velaron.fennec.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Transformation;

import java.util.List;

import dev.velaron.fennec.Constants;
import dev.velaron.fennec.R;
import dev.velaron.fennec.activity.SelectionUtils;
import dev.velaron.fennec.api.PicassoInstance;
import dev.velaron.fennec.fragment.UserInfoResolveUtil;
import dev.velaron.fennec.model.Community;
import dev.velaron.fennec.model.Owner;
import dev.velaron.fennec.model.User;
import dev.velaron.fennec.settings.CurrentTheme;
import dev.velaron.fennec.util.ViewUtils;

import static dev.velaron.fennec.util.Objects.nonNull;

public class PeopleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<? extends Owner> mData;
    private Transformation transformation;

    public PeopleAdapter(Context context, List<? extends Owner> data) {
        this.mContext = context;
        this.mData = data;
        this.transformation = CurrentTheme.createTransformationForAvatar(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType){
            case TYPE_USER:
                return new PeopleHolder(LayoutInflater.from(mContext).inflate(R.layout.item_people, parent, false));
            case TYPE_COMMUNITY:
                return new CommunityHolder(LayoutInflater.from(mContext).inflate(R.layout.item_group, parent, false));
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)){
            case TYPE_USER:
                bindUserHolder((PeopleHolder) holder, (User) mData.get(position));
                break;
            case TYPE_COMMUNITY:
                bindCommunityHolder((CommunityHolder) holder, (Community) mData.get(position));
                break;
        }
    }

    private void bindCommunityHolder(CommunityHolder holder, final Community community){
        holder.tvName.setText(community.getName());
        String status = "@" + community.getScreenName();
        holder.tvStatus.setText(status);

        PicassoInstance.with()
                .load(community.getMaxSquareAvatar())
                .tag(Constants.PICASSO_TAG)
                .transform(transformation)
                .into(holder.ivAvatar);

        holder.itemView.setOnClickListener(v -> {
            if(mClickListener != null){
                mClickListener.onOwnerClick(community);
            }
        });
    }

    private static final int STATUS_COLOR_OFFLINE = Color.parseColor("#999999");

    private void bindUserHolder(PeopleHolder holder, final User user){
        holder.name.setText(user.getFullName());

        holder.subtitle.setText(UserInfoResolveUtil.getUserActivityLine(mContext, user));
        holder.subtitle.setTextColor(user.isOnline() ? CurrentTheme.getColorPrimary(mContext) : STATUS_COLOR_OFFLINE);

        holder.online.setVisibility(user.isOnline() ? View.VISIBLE : View.GONE);
        Integer onlineIcon = ViewUtils.getOnlineIcon(user.isOnline(), user.isOnlineMobile(), user.getPlatform(), user.getOnlineApp());
        if(onlineIcon != null){
            holder.online.setImageResource(onlineIcon);
        } else {
            holder.online.setImageDrawable(null);
        }

        String avaUrl = user.getMaxSquareAvatar();
        ViewUtils.displayAvatar(holder.avatar, transformation, avaUrl, Constants.PICASSO_TAG);

        holder.itemView.setOnClickListener(v -> {
            if(mClickListener != null){
                mClickListener.onOwnerClick(user);
            }
        });

        holder.itemView.setOnLongClickListener(v -> nonNull(longClickListener) && longClickListener.onOwnerLongClick(user));

        SelectionUtils.addSelectionProfileSupport(mContext, holder.avatarRoot, user);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setItems(List<? extends Owner> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    private class PeopleHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView subtitle;
        ImageView avatar;
        ImageView online;

        ViewGroup avatarRoot;

        PeopleHolder(View itemView) {
            super(itemView);
            avatarRoot = itemView.findViewById(R.id.avatar_root);
            name = itemView.findViewById(R.id.item_people_name);
            subtitle = itemView.findViewById(R.id.item_people_subtitle);
            avatar = itemView.findViewById(R.id.item_people_avatar);
            online = itemView.findViewById(R.id.item_people_online);
            online.setColorFilter(CurrentTheme.getColorPrimary(mContext), PorterDuff.Mode.MULTIPLY);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mData.get(position) instanceof User ? TYPE_USER : TYPE_COMMUNITY;
    }

    private static final int TYPE_USER = 0;
    private static final int TYPE_COMMUNITY = 1;

    private ClickListener mClickListener;

    private LongClickListener longClickListener;

    public PeopleAdapter setLongClickListener(LongClickListener longClickListener) {
        this.longClickListener = longClickListener;
        return this;
    }

    public void setClickListener(ClickListener clickListener) {
        this.mClickListener = clickListener;
    }

    public interface ClickListener {
        void onOwnerClick(Owner owner);
    }

    public interface LongClickListener {
        boolean onOwnerLongClick(Owner owner);
    }

    private static class CommunityHolder extends RecyclerView.ViewHolder {

        private TextView tvName;
        private TextView tvStatus;
        private ImageView ivAvatar;

        CommunityHolder(View root){
            super(root);
            tvName = root.findViewById(R.id.item_group_name);
            tvStatus = root.findViewById(R.id.item_group_status);
            ivAvatar = root.findViewById(R.id.item_group_avatar);
        }
    }
}