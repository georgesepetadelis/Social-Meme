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

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.george.socialmeme.Activities.Profile.UserProfileActivity;
import com.george.socialmeme.Models.UserModel;
import com.george.socialmeme.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import maes.tech.intentanim.CustomIntent;

public class UserRecyclerAdapter extends RecyclerView.Adapter<UserRecyclerAdapter.UserViewHolder> {

    public static Context context;
    List<UserModel> usersArrayList;

    public UserRecyclerAdapter(FirebaseRecyclerOptions<UserModel> options, Context context, List<UserModel> usersArrayList) {
        UserRecyclerAdapter.context = context;
        this.usersArrayList = usersArrayList;
    }

    public void setFilteredList(List<UserModel> filteredList) {
        this.usersArrayList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {

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

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        public String userID;
        public CardView card;
        public ImageView profilePicture;
        public TextView username, followers;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            profilePicture = itemView.findViewById(R.id.circleImageView);
            username = itemView.findViewById(R.id.textView23);
            followers = itemView.findViewById(R.id.textView25);
            card = itemView.findViewById(R.id.user_item_cont);

            card.setOnClickListener(view -> {
                Intent intent = new Intent(UserRecyclerAdapter.context, UserProfileActivity.class);
                intent.putExtra("user_id", userID);
                intent.putExtra("username", username.getText().toString());
                //intent.putExtra("allPosts", new Gson().toJson(HomeActivity.savedPostsArrayList));
                UserRecyclerAdapter.context.startActivity(intent);
                CustomIntent.customType(UserRecyclerAdapter.context, "left-to-right");
            });

        }
    }

}
