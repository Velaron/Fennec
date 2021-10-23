package dev.velaron.fennec.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Transformation;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import dev.velaron.fennec.Constants;
import dev.velaron.fennec.R;
import dev.velaron.fennec.model.Account;
import dev.velaron.fennec.model.Owner;
import dev.velaron.fennec.model.User;
import dev.velaron.fennec.settings.CurrentTheme;
import dev.velaron.fennec.settings.Settings;
import dev.velaron.fennec.util.Objects;
import dev.velaron.fennec.util.ViewUtils;

import static dev.velaron.fennec.util.Utils.nonEmpty;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.Holder> {

    private Context context;
    private List<Account> data;
    private Transformation transformation;
    private Callback callback;

    public AccountAdapter(Context context, List<Account> items, Callback callback) {
        this.context = context;
        this.data = items;
        this.transformation = CurrentTheme.createTransformationForAvatar(context);
        this.callback = callback;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_account, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        final Account account = data.get(position);

        Owner owner = account.getOwner();

        if(Objects.isNull(owner)){
            holder.firstName.setText(String.valueOf(account.getId()));
            ViewUtils.displayAvatar(holder.avatar, transformation, null, Constants.PICASSO_TAG);
        } else {
            holder.firstName.setText(owner.getFullName());
            ViewUtils.displayAvatar(holder.avatar, transformation, owner.getMaxSquareAvatar(), Constants.PICASSO_TAG);
        }

        if(account.getId() < 0){
            holder.lastName.setText("club" + Math.abs(account.getId()));
        } else {
            User user = (User) owner;

            if(Objects.nonNull(user) && nonEmpty(user.getDomain())){
                holder.lastName.setText("@" + user.getDomain());
            } else {
                holder.lastName.setText("@id" + account.getId());
            }
        }

        boolean isCurrent = account.getId() == Settings.get()
                .accounts()
                .getCurrent();

        holder.active.setVisibility(isCurrent ? View.VISIBLE : View.INVISIBLE);
        holder.itemView.setOnClickListener(v -> callback.onClick(account));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        TextView firstName;
        TextView lastName;
        ImageView avatar;
        ImageView active;

        public Holder(View itemView) {
            super(itemView);
            firstName = itemView.findViewById(R.id.first_name);
            lastName = itemView.findViewById(R.id.last_name);
            avatar = itemView.findViewById(R.id.avatar);
            active = itemView.findViewById(R.id.active);
        }
    }

    public interface Callback {
        void onClick(Account account);
    }
}