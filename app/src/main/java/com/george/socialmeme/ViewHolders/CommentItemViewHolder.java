package com.george.socialmeme.ViewHolders;

import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.george.socialmeme.Activities.HomeActivity;
import com.george.socialmeme.Activities.UserProfileActivity;
import com.george.socialmeme.Adapters.CommentsRecyclerAdapter;
import com.george.socialmeme.R;
import com.google.gson.Gson;

import de.hdodenhof.circleimageview.CircleImageView;
import maes.tech.intentanim.CustomIntent;

public class CommentItemViewHolder extends RecyclerView.ViewHolder {

    public String userID;
    public CardView cardView;
    public CircleImageView profilePicture;
    public TextView usernameTV, commentContent;
    public ImageButton deleteCommentBtn;

    public CommentItemViewHolder(@NonNull View itemView) {
        super(itemView);

        cardView = itemView.findViewById(R.id.comment_card);
        profilePicture = itemView.findViewById(R.id.circleImageView3);
        usernameTV = itemView.findViewById(R.id.textView65);
        commentContent = itemView.findViewById(R.id.textView69);
        deleteCommentBtn = itemView.findViewById(R.id.delete_comment_btn);

        cardView.setOnClickListener(v -> {

            if (userID != null) {
                Intent intent = new Intent(itemView.getContext(), UserProfileActivity.class);
                intent.putExtra("user_id", userID);
                intent.putExtra("username", usernameTV.getText().toString());
                intent.putExtra("allPosts", new Gson().toJson(HomeActivity.savedPostsArrayList));
                itemView.getContext().startActivity(intent);
                CustomIntent.customType(itemView.getContext(), "left-to-right");
            }

        });

    }

}
