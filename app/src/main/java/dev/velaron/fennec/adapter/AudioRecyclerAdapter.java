package dev.velaron.fennec.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.velaron.fennec.R;
import dev.velaron.fennec.model.Audio;
import dev.velaron.fennec.util.AppTextUtils;

public class AudioRecyclerAdapter extends RecyclerView.Adapter<AudioRecyclerAdapter.AudioHolder>{

    private Context mContext;
    private List<Audio> mData;

    public AudioRecyclerAdapter(Context context, List<Audio> data) {
        this.mContext = context;
        this.mData = data;
    }

    @Override
    public AudioHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AudioHolder(LayoutInflater.from(mContext).inflate(R.layout.item_audio, parent, false));
    }

    @Override
    public void onBindViewHolder(final AudioHolder holder, int position) {
        final Audio item = mData.get(position);

        holder.artist.setText(item.getArtist());
        holder.title.setText(item.getTitle());
        holder.time.setText(AppTextUtils.getDurationString(item.getDuration()));
        holder.hq.setVisibility(item.isHq() ? View.VISIBLE : View.INVISIBLE);

//        holder.play.setImageResource(MusicUtils.isNowPlayingOrPreparing(item) ? R.drawable.pause : R.drawable.play);

        holder.play.setOnClickListener(v -> {
            if(mClickListener != null){
                mClickListener.onClick(holder.getAdapterPosition(), item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setData(List<Audio> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    class AudioHolder extends RecyclerView.ViewHolder {

        TextView artist;
        TextView title;
        ImageView play;
        TextView time;
        ImageView hq;

        AudioHolder(View itemView) {
            super(itemView);
            artist = itemView.findViewById(R.id.dialog_title);
            title = itemView.findViewById(R.id.dialog_message);
            play = itemView.findViewById(R.id.item_audio_play);
            time = itemView.findViewById(R.id.item_audio_time);
            hq = itemView.findViewById(R.id.hq);
        }
    }

    private ClickListener mClickListener;

    public void setClickListener(ClickListener clickListener) {
        this.mClickListener = clickListener;
    }

    public interface ClickListener {
        void onClick(int position, Audio audio);
    }
}
