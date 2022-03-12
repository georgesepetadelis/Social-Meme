package com.george.socialmeme.ViewHolders;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.george.socialmeme.Adapters.CommentsRecyclerAdapter;
import com.george.socialmeme.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentItemViewHolder extends RecyclerView.ViewHolder {

    public CircleImageView profilePicture;
    public TextView usernameTV, commentContent;
    public ImageButton deleteCommentBtn;

    public CommentItemViewHolder(@NonNull View itemView) {
        super(itemView);
        profilePicture = itemView.findViewById(R.id.circleImageView3);
        usernameTV = itemView.findViewById(R.id.textView65);
        commentContent = itemView.findViewById(R.id.textView69);
        deleteCommentBtn = itemView.findViewById(R.id.delete_comment_btn);
    }

}
