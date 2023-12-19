package com.george.socialmeme.ViewHolders;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.george.socialmeme.Activities.Feed.PostsOfTheMonthActivity;
import com.george.socialmeme.R;

import maes.tech.intentanim.CustomIntent;

public class PostsOfTheMonthItemViewHolder extends RecyclerView.ViewHolder {
    View itemContainer;
    Context context;

    public void setContext(Context context) {
        this.context = context;
    }

    public PostsOfTheMonthItemViewHolder(@NonNull View itemView) {
        super(itemView);
        itemContainer = itemView.findViewById(R.id.posts_of_the_month_btn);

        itemContainer.setOnClickListener(view -> {
            Intent intent = new Intent(context, PostsOfTheMonthActivity.class);
            context.startActivity(intent);
            CustomIntent.customType(context, "left-to-right");
        });

    }

}
