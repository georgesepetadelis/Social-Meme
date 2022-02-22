package com.george.socialmeme.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.george.socialmeme.Models.PostModel;

import java.util.List;

public class CommentsRecyclerAdapter extends RecyclerView.Adapter {

    List<PostModel> postList;
    Context context;
    Activity activity;

    public CommentsRecyclerAdapter(List<PostModel> postList, Context context, Activity activity) {
        this.postList = postList;
        this.context = context;
        this.activity = activity;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
