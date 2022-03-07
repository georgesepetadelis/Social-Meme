package com.george.socialmeme.ViewHolders;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.george.socialmeme.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentViewHolder extends RecyclerView.ViewHolder {

    public CircleImageView profilePicture;
    public TextView usernameTV, commentContent;

    public CommentViewHolder(@NonNull View itemView) {
        super(itemView);
        profilePicture = itemView.findViewById(R.id.circleImageView3);
        usernameTV = itemView.findViewById(R.id.textView65);
        commentContent = itemView.findViewById(R.id.textView69);
    }

}
