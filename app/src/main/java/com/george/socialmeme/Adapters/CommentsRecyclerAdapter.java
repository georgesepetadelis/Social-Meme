package com.george.socialmeme.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.george.socialmeme.Models.CommentModel;
import com.george.socialmeme.Models.PostModel;
import com.george.socialmeme.R;
import com.george.socialmeme.ViewHolders.CommentViewHolder;
import com.george.socialmeme.ViewHolders.PostsOfTheMonthViewHolder;

import java.util.List;

public class CommentsRecyclerAdapter extends RecyclerView.Adapter {

    List<CommentModel> commentsList;
    Context context;
    Activity activity;

    public CommentsRecyclerAdapter(List<CommentModel> commentsList, Context context, Activity activity) {
        this.commentsList = commentsList;
        this.context = context;
        this.activity = activity;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        CommentViewHolder viewHolder = (CommentViewHolder) holder;

        // Load profile picture
        String profilePictureUrl = commentsList.get(position).getAuthorProfilePictureURL();
        if (profilePictureUrl != null) {
            if (!profilePictureUrl.equals("none")) {
                Glide.with(context).load(profilePictureUrl).into(viewHolder.profilePicture);
            }
        }else {
            viewHolder.profilePicture.setImageResource(R.drawable.user);
        }

        viewHolder.usernameTV.setText(commentsList.get(position).getAuthorUsername());
        viewHolder.commentContent.setText(commentsList.get(position).getCommentText());

    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }
}