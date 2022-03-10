package com.george.socialmeme.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.george.socialmeme.Models.NotificationModel;
import com.george.socialmeme.R;

import java.util.List;

public class NotificationRecyclerAdapter extends RecyclerView.Adapter<NotificationRecyclerAdapter.ViewHolder>{

    List<NotificationModel> postList;
    Context context;

    public NotificationRecyclerAdapter(List<NotificationModel> postList, Context context) {
        this.postList = postList;
        this.context = context;
    }

    @NonNull
    @Override
    public NotificationRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationRecyclerAdapter.ViewHolder holder, int position) {

        holder.notificationTitle.setText(postList.get(position).getTitle());
        holder.notificationDate.setText(postList.get(position).getDate());
        holder.notificationMessage.setText(postList.get(position).getMessage());

        switch (postList.get(position).getType()) {
            case "like":
                holder.notificationIcon.setImageResource(R.drawable.ic_like_modern);
                break;
            case "profile_visit":
            case "new_follower":
            case "unfollow":
                holder.notificationIcon.setImageResource(R.drawable.user);
                break;
            case "profile_screenshot":
            case "post_save":
                holder.notificationIcon.setImageResource(R.drawable.ic_camera_modern);
                break;
            case "comment_added":
                holder.notificationIcon.setImageResource(R.drawable.ic_comment);
                break;
        }

    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView notificationIcon;
        TextView notificationTitle, notificationDate, notificationMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            notificationIcon = itemView.findViewById(R.id.notification_icon);
            notificationTitle = itemView.findViewById(R.id.notification_title);
            notificationDate = itemView.findViewById(R.id.notification_date);
            notificationMessage = itemView.findViewById(R.id.notification_message);

        }
    }

}
