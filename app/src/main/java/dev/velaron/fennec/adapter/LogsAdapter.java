package dev.velaron.fennec.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.EventListener;
import java.util.List;

import dev.velaron.fennec.R;
import dev.velaron.fennec.adapter.base.RecyclerBindableAdapter;
import dev.velaron.fennec.model.LogEvent;
import dev.velaron.fennec.model.LogEventWrapper;
import dev.velaron.fennec.util.AppTextUtils;

import static dev.velaron.fennec.util.Utils.safeLenghtOf;

/**
 * Created by Ruslan Kolbasa on 26.04.2017.
 * phoenix
 */
public class LogsAdapter extends RecyclerBindableAdapter<LogEventWrapper, LogsAdapter.Holder> {

    private final ActionListener actionListener;

    public LogsAdapter(List<LogEventWrapper> data, ActionListener actionListener) {
        super(data);
        this.actionListener = actionListener;
    }

    private static final int MAX_BODY_LENGHT = 400;

    @Override
    protected void onBindItemViewHolder(Holder holder, int position, int type) {
        LogEventWrapper wrapper = getItem(position);
        LogEvent event = wrapper.getEvent();

        holder.body.setText(event.getBody());
        holder.tag.setText(event.getTag());

        long unixtime = event.getDate() / 1000;
        holder.datetime.setText(AppTextUtils.getDateFromUnixTime(unixtime));

        holder.buttonShare.setOnClickListener(v -> actionListener.onShareClick(wrapper));

        holder.bodyRoot.setOnClickListener(v -> {
            if(!canReduce(event.getBody())){
                return;
            }

            wrapper.setExpanded(!wrapper.isExpanded());
            notifyItemChanged(position + getHeadersCount());
            //setupBodyRoot(holder, wrapper);
        });

        setupBodyRoot(holder, wrapper);
    }

    private boolean canReduce(String body){
        return safeLenghtOf(body) > MAX_BODY_LENGHT;
    }

    private void setupBodyRoot(Holder holder, LogEventWrapper wrapper){
        String body = wrapper.getEvent().getBody();

        boolean canReduce = canReduce(body);

        if(!canReduce || wrapper.isExpanded()){
            holder.buttonExpand.setVisibility(View.GONE);
            holder.body.setText(body);
        } else {
            holder.buttonExpand.setVisibility(View.VISIBLE);
            holder.body.setText(AppTextUtils.reduceText(body, MAX_BODY_LENGHT));
        }
    }

    @Override
    protected Holder viewHolder(View view, int type) {
        return new Holder(view);
    }

    @Override
    protected int layoutId(int type) {
        return R.layout.item_log;
    }

    public interface ActionListener extends EventListener {
        void onShareClick(LogEventWrapper wrapper);
    }

    static class Holder extends RecyclerView.ViewHolder {

        TextView tag;
        TextView datetime;
        TextView body;

        View buttonShare;

        View bodyRoot;
        View buttonExpand;

        Holder(View itemView) {
            super(itemView);
            this.tag = itemView.findViewById(R.id.log_tag);
            this.datetime = itemView.findViewById(R.id.log_datetime);
            this.body = itemView.findViewById(R.id.log_body);

            this.buttonShare = itemView.findViewById(R.id.log_button_share);

            this.bodyRoot = itemView.findViewById(R.id.log_body_root);
            this.buttonExpand = itemView.findViewById(R.id.log_button_expand);
        }
    }

}
