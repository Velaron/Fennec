package dev.velaron.fennec.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Transformation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;
import dev.velaron.fennec.Constants;
import dev.velaron.fennec.R;
import dev.velaron.fennec.model.Manager;
import dev.velaron.fennec.model.User;
import dev.velaron.fennec.settings.CurrentTheme;
import dev.velaron.fennec.util.Objects;
import dev.velaron.fennec.util.ViewUtils;
import dev.velaron.fennec.view.OnlineView;

/**
 * Created by admin on 14.06.2017.
 * phoenix
 */
public class CommunityManagersAdapter extends RecyclerView.Adapter<CommunityManagersAdapter.Holder> {

    private List<Manager> users;
    private Transformation transformation;

    public CommunityManagersAdapter(Context context, List<Manager> users) {
        this.users = users;
        this.transformation = CurrentTheme.createTransformationForAvatar(context);
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_community_manager, parent, false));
    }

    private static final Map<String, Integer> roleTextResources = new HashMap<>(4);
    static {
        roleTextResources.put("moderator", R.string.role_moderator);
        roleTextResources.put("editor", R.string.role_editor);
        roleTextResources.put("administrator", R.string.role_administrator);
        roleTextResources.put("creator", R.string.role_creator);
    }

    private ActionListener actionListener;

    public void setActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public interface ActionListener {
        void onManagerClick(Manager manager);
        void onManagerLongClick(Manager manager);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        Manager manager = users.get(position);
        User user = manager.getUser();

        holder.name.setText(user.getFullName());

        ViewUtils.displayAvatar(holder.avatar, transformation, user.getMaxSquareAvatar(), Constants.PICASSO_TAG);

        Integer onlineRes = ViewUtils.getOnlineIcon(user.isOnline(), user.isOnlineMobile(), user.getPlatform(), user.getOnlineApp());
        if(Objects.nonNull(onlineRes)){
            holder.onlineView.setIcon(onlineRes);
            holder.onlineView.setVisibility(View.VISIBLE);
        } else {
            holder.onlineView.setVisibility(View.GONE);
        }

        @StringRes
        Integer roleTextRes = roleTextResources.get(manager.getRole());

        if(Objects.isNull(roleTextRes)){
            roleTextRes = R.string.role_unknown;
        }

        holder.role.setText(roleTextRes);
        holder.itemView.setOnClickListener(v -> {
            if(Objects.nonNull(actionListener)){
                actionListener.onManagerClick(manager);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if("creator".equalsIgnoreCase(manager.getRole())){
                return false;
            }

            if(Objects.nonNull(actionListener)){
                actionListener.onManagerLongClick(manager);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void setData(List<Manager> data) {
        this.users = data;
        notifyDataSetChanged();
    }

    static class Holder extends RecyclerView.ViewHolder {

        ImageView avatar;
        OnlineView onlineView;
        TextView name;
        TextView role;

        Holder(View itemView) {
            super(itemView);
            this.avatar = itemView.findViewById(R.id.avatar);
            this.onlineView = itemView.findViewById(R.id.online);
            this.name = itemView.findViewById(R.id.name);
            this.role = itemView.findViewById(R.id.role);
        }
    }
}