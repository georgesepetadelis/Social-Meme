package com.george.socialmeme.ViewHolders;

import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CommentViewHolder extends RecyclerView.ViewHolder {

    ImageButton closeDialogBtn;
    RecyclerView commentsRecyclerView;

    public CommentViewHolder(@NonNull View itemView) {
        super(itemView);

    }

}
