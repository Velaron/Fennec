package dev.velaron.fennec.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Transformation;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import dev.velaron.fennec.Constants;
import dev.velaron.fennec.R;
import dev.velaron.fennec.model.Owner;
import dev.velaron.fennec.model.User;
import dev.velaron.fennec.settings.CurrentTheme;
import dev.velaron.fennec.util.ViewUtils;

public class OwnersListAdapter extends ArrayAdapter<Owner> {

    private ArrayList<Owner> data;
    private Transformation transformation;

    public OwnersListAdapter(Activity context, ArrayList<Owner> owners) {
        super(context, R.layout.item_simple_owner, owners);
        this.data = owners;
        this.transformation = CurrentTheme.createTransformationForAvatar(context);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Owner getItem(int position) {
        return data.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        final View view;
        if (convertView == null) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_simple_owner, parent, false);
            view.setTag(new ViewHolder(view));
        } else {
            view = convertView;
        }

        final ViewHolder holder = (ViewHolder) view.getTag();
        final Owner item = data.get(position);

        holder.tvName.setText(item.getFullName());
        ViewUtils.displayAvatar(holder.ivAvatar, transformation, item.getMaxSquareAvatar(), Constants.PICASSO_TAG);

        holder.subtitle.setText(item instanceof User ? R.string.profile : R.string.community);
        return view;
    }

    private static class ViewHolder {

        TextView tvName;
        ImageView ivAvatar;
        TextView subtitle;

        ViewHolder(View root) {
            tvName = root.findViewById(R.id.name);
            ivAvatar = root.findViewById(R.id.avatar);
            subtitle = root.findViewById(R.id.subtitle);
        }
    }
}