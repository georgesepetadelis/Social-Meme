package com.george.socialmeme.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.george.socialmeme.Activities.HomeActivity;
import com.george.socialmeme.Models.PostModel;
import com.george.socialmeme.Models.UserModel;
import com.george.socialmeme.R;
import com.george.socialmeme.ViewHolders.CommentItemViewHolder;
import com.george.socialmeme.ViewHolders.RecommendedUserViewHolder;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class RecommendedUserRecyclerAdapter extends RecyclerView.Adapter {

    public Context context;
    public ArrayList<UserModel> userModelArrayList;

    public RecommendedUserRecyclerAdapter(Context context, ArrayList<UserModel> userModelArrayList) {
        this.context = context;
        this.userModelArrayList = userModelArrayList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_item_horizontal, parent, false);
        return new RecommendedUserViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        RecommendedUserViewHolder userViewHolder = (RecommendedUserViewHolder) holder;
        String userProfilePictureURL = userModelArrayList.get(position).getProfilePictureURL();

        if (userModelArrayList.get(position).getProfilePictureURL() != null) {
            if (!userProfilePictureURL.equals("none")) {
                Picasso.get().load(userProfilePictureURL).into(userViewHolder.profilePicture);
            } else {
                userViewHolder.profilePicture.setImageResource(R.drawable.user);
            }
        }

        userViewHolder.recommendedUserID = userModelArrayList.get(position).getUserID();
        userViewHolder.usernameTV.setText(userModelArrayList.get(position).getUsername());
        userViewHolder.context = context;

        int totalMemes = 0;
        if (HomeActivity.savedPostsArrayList != null) {
            for (PostModel postModel : HomeActivity.savedPostsArrayList) {
                if (postModel.getAuthorID() != null) {
                    if (postModel.getAuthorID().equals(userModelArrayList.get(position).getUserID())) {
                        totalMemes += 1;
                    }
                }
            }
        }

        switch (totalMemes) {
            case 0:
                userViewHolder.memesCounterTV.setText("No memes");
                break;
            case 1:
                userViewHolder.memesCounterTV.setText(totalMemes + " Meme");
                break;
            default:
                userViewHolder.memesCounterTV.setText(totalMemes + " Memes");
        }

    }

    @Override
    public int getItemCount() {
        return userModelArrayList.size();
    }
}
