package com.george.socialmeme.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.george.socialmeme.Activities.HomeActivity;
import com.george.socialmeme.Activities.PostActivity;
import com.george.socialmeme.Activities.UserProfileActivity;
import com.george.socialmeme.Models.NotificationModel;
import com.george.socialmeme.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.List;

import maes.tech.intentanim.CustomIntent;

public class NotificationRecyclerAdapter extends RecyclerView.Adapter<NotificationRecyclerAdapter.ViewHolder>{

    List<NotificationModel> postList;
    public Context context;

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
        holder.notificationID = postList.get(position).getId();

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

        String notificationID;
        CardView cardView;
        ImageView notificationIcon;
        TextView notificationTitle, notificationDate, notificationMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.notification_item_card);
            notificationIcon = itemView.findViewById(R.id.notification_icon);
            notificationTitle = itemView.findViewById(R.id.notification_title);
            notificationDate = itemView.findViewById(R.id.notification_date);
            notificationMessage = itemView.findViewById(R.id.notification_message);

            cardView.setOnClickListener(v -> {

                FirebaseAuth auth = FirebaseAuth.getInstance();
                FirebaseUser user = auth.getCurrentUser();
                DatabaseReference notificationRef = FirebaseDatabase.getInstance().getReference("users")
                        .child(user.getUid()).child("notifications").child(notificationID);

                notificationRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.child("user_id").getValue(String.class) == null &&
                                snapshot.child("post_id").getValue(String.class) != null) {

                            Intent intent = new Intent(itemView.getContext(), PostActivity.class);
                            intent.putExtra("post_id", snapshot.child("post_id").getValue(String.class));
                            itemView.getContext().startActivity(intent);
                            CustomIntent.customType(itemView.getContext(), "left-to-right");

                        } else if (snapshot.child("user_id").getValue(String.class) != null &&
                                snapshot.child("post_id").getValue(String.class) == null) {

                            if (HomeActivity.savedPostsArrayList != null && HomeActivity.savedPostsArrayList.size() != 0) {

                                Intent intent = new Intent(itemView.getContext(), UserProfileActivity.class);
                                intent.putExtra("user_id", snapshot.child("user_id").getValue(String.class));
                                intent.putExtra("username", HomeActivity.notiUsername);
                                //intent.putExtra("allPosts", new Gson().toJson(HomeActivity.savedPostsArrayList));
                                itemView.getContext().startActivity(intent);
                                CustomIntent.customType(itemView.getContext(), "left-to-right");

                            } else {
                                Toast.makeText(itemView.getContext(), "posts is null", Toast.LENGTH_SHORT).show();
                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(itemView.getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            });

        }
    }

}
