package com.george.socialmeme.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.george.socialmeme.Models.UserModel;
import com.george.socialmeme.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class FollowingAndFollowersRecyclerAdapter extends RecyclerView.Adapter<FollowingAndFollowersRecyclerAdapter.ViewHolder> {

    Context context;
    List<UserModel> usersArrayList;

    public FollowingAndFollowersRecyclerAdapter(Context context, List<UserModel> usersArrayList) {
        this.context = context;
        this.usersArrayList = usersArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new FollowingAndFollowersRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if (usersArrayList.get(position).getProfilePictureURL().equals("none")) {
            holder.profilePicture.setImageResource(R.drawable.profile_icon);
        }else {
            Picasso.get().load(usersArrayList.get(position).getProfilePictureURL()).into(holder.profilePicture);
        }

        holder.username.setText(usersArrayList.get(position).getUsername());
        holder.followers.setText(usersArrayList.get(position).getFollowers() + " Followers");

    }

    @Override
    public int getItemCount() {
        return usersArrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView profilePicture;
        TextView username, followers;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profilePicture = itemView.findViewById(R.id.circleImageView);
            username = itemView.findViewById(R.id.textView23);
            followers = itemView.findViewById(R.id.textView25);

        }
    }

}
