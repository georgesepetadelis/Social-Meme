package com.george.socialmeme.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.george.socialmeme.Activities.HomeActivity;
import com.george.socialmeme.Activities.UserProfileActivity;
import com.george.socialmeme.Models.UserModel;
import com.george.socialmeme.R;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.List;

import maes.tech.intentanim.CustomIntent;

public class FollowerInfoRecyclerAdapter extends RecyclerView.Adapter<FollowerInfoRecyclerAdapter.ViewHolder> {

    public static Context context;
    List<UserModel> usersArrayList;

    public FollowerInfoRecyclerAdapter(Context context, List<UserModel> usersArrayList) {
        this.context = context;
        this.usersArrayList = usersArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if (usersArrayList.get(position).getProfilePictureURL().equals("none")) {
            holder.profilePicture.setImageResource(R.drawable.user);
        } else {
            Picasso.get().load(usersArrayList.get(position).getProfilePictureURL()).into(holder.profilePicture);
        }

        holder.username.setText(usersArrayList.get(position).getUsername());
        holder.followers.setText(usersArrayList.get(position).getFollowers());
        holder.userID = usersArrayList.get(position).getUserID();

    }

    @Override
    public int getItemCount() {
        return usersArrayList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        String userID;
        CardView card;
        ImageView profilePicture;
        TextView username, followers;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profilePicture = itemView.findViewById(R.id.circleImageView);
            username = itemView.findViewById(R.id.textView23);
            followers = itemView.findViewById(R.id.textView25);
            card = itemView.findViewById(R.id.user_item_cont);

            card.setOnClickListener(view -> {
                Intent intent = new Intent(FollowerInfoRecyclerAdapter.context, UserProfileActivity.class);
                intent.putExtra("user_id", userID);
                intent.putExtra("username", username.getText().toString());
                intent.putExtra("allPosts", new Gson().toJson(HomeActivity.savedPostsArrayList));
                FollowerInfoRecyclerAdapter.context.startActivity(intent);
                CustomIntent.customType(FollowerInfoRecyclerAdapter.context, "left-to-right");
            });

        }
    }

}
