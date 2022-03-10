package com.george.socialmeme.Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class CommentsRecyclerAdapter extends RecyclerView.Adapter {

    public static List<CommentModel> commentsList;
    Context context;
    Activity activity;

    public CommentsRecyclerAdapter(List<CommentModel> commentsList, Context context, Activity activity) {
        CommentsRecyclerAdapter.commentsList = commentsList;
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
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
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

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("posts");

        // Hide delete button if current logged-in user is not the comment author
        if (!commentsList.get(position).getAuthor().equals(user.getUid())) {
            viewHolder.deleteCommentBtn.setVisibility(View.INVISIBLE);
        }
        
        viewHolder.deleteCommentBtn.setOnClickListener(view -> {

            new AlertDialog.Builder(context)
                    .setTitle("Are you sure?")
                    .setMessage("Are you sure you want to delete this comment?")
                    .setIcon(R.drawable.ic_report)
                    .setPositiveButton("Yes", (dialogInterface, i) -> {

                        // Remove comment from Real-Time DB
                        postsRef.child(commentsList.get(position).getPostID())
                                .child("comments")
                                .child(commentsList.get(position).getCommentID())
                                .removeValue()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        // Remove comment from RecyclerView
                                        commentsList.remove(viewHolder.getLayoutPosition());
                                        notifyItemRemoved(position);
                                        notifyItemRangeChanged(position, commentsList.size());
                                    }else {
                                        Toast.makeText(context, "Failed to delete comment", Toast.LENGTH_SHORT).show();
                                    }
                                    dialogInterface.dismiss();
                                });

                    }).setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss())
                    .show();

        });
        
    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }
    
}