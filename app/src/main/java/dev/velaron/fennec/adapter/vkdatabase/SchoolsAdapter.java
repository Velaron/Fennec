package dev.velaron.fennec.adapter.vkdatabase;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import dev.velaron.fennec.R;
import dev.velaron.fennec.model.database.School;

public class SchoolsAdapter extends RecyclerView.Adapter<SchoolsAdapter.Holder> {

    private Context mContext;
    private List<School> mData;

    public SchoolsAdapter(Context mContext, List<School> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(mContext).inflate(R.layout.item_country, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        final School school = mData.get(position);
        holder.name.setText(school.getTitle());

        holder.itemView.setOnClickListener(v -> {
            if(mListener != null){
                mListener.onClick(school);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        TextView name;

        public Holder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
        }
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    private Listener mListener;

    public interface Listener {
        void onClick(School school);
    }
}
