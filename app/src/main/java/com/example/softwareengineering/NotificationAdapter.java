package com.example.softwareengineering;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.res.ColorStateList;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    List<NotificationModel> list;

    public NotificationAdapter(List<NotificationModel> list) {
        this.list = list;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateList(List<NotificationModel> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationModel n = list.get(position);

        if (position == list.size() - 1) {
            holder.itemDivider.setVisibility(View.GONE);
        } else {
            holder.itemDivider.setVisibility(View.VISIBLE);
        }

        if (n.isUnread()) {
            holder.itemLayout.setBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.my_tertiary)
            );
            holder.unreadDot.setVisibility(View.VISIBLE);
        } else {
            holder.itemLayout.setBackgroundColor(Color.TRANSPARENT);
            holder.unreadDot.setVisibility(View.GONE);
        }

        holder.title.setText(n.getTitle());
        holder.message.setText(n.getMessage());
        holder.time.setText(getTimeAgo(n.getTimestamp()));

        holder.unreadDot.setVisibility(n.isUnread() ? View.VISIBLE : View.INVISIBLE);

        holder.icon.setImageResource(n.getIcon());
        holder.icon.setColorFilter(n.getIconColor());
        holder.icon.setBackgroundTintList(ColorStateList.valueOf(n.getIconBgColor()));
    }

    private String getTimeAgo(long time) {
        long now = System.currentTimeMillis();
        long diff = now - time;

        if (diff < 60_000) { // less than 1 minute
            return "Just now";
        } else if (diff < 3_600_000) { // less than 1 hour
            long minutes = diff / 60_000;
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        } else if (diff < 86_400_000) { // less than 1 day
            long hours = diff / 3_600_000;
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else {
            long days = diff / 86_400_000;
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        }
    }



    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView icon;
        TextView title, message, time;
        View unreadDot;
        View itemDivider;
        LinearLayout itemLayout;

        ViewHolder(View itemView) {
            super(itemView);

            icon = itemView.findViewById(R.id.iconImage);
            title = itemView.findViewById(R.id.notificationTitle);
            message = itemView.findViewById(R.id.notificationMessage);
            time = itemView.findViewById(R.id.notificationTime);
            unreadDot = itemView.findViewById(R.id.unreadDot);
            itemDivider = itemView.findViewById(R.id.itemDivider);
            itemLayout = itemView.findViewById(R.id.mainItemLayout);
        }
    }
}

