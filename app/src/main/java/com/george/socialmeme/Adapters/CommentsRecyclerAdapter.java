package com.george.socialmeme.Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.george.socialmeme.Activities.Profile.UserProfileActivity;
import com.george.socialmeme.Helpers.PostHelper;
import com.george.socialmeme.Models.CommentModel;
import com.george.socialmeme.R;
import com.george.socialmeme.ViewHolders.CommentItemViewHolder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
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
        return new CommentItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        CommentItemViewHolder viewHolder = (CommentItemViewHolder) holder;

        // Load profile picture
        String profilePictureUrl = commentsList.get(position).getAuthorProfilePictureURL();
        if (profilePictureUrl != null) {
            if (!profilePictureUrl.equals("none")) {
                Glide.with(context).load(profilePictureUrl).into(viewHolder.profilePicture);
            }
        } else {
            viewHolder.profilePicture.setImageResource(R.drawable.user);
        }

        viewHolder.usernameTV.setText(commentsList.get(position).getAuthorUsername());
        viewHolder.commentContent.setText(commentsList.get(position).getCommentText());
        viewHolder.userID = commentsList.get(position).getAuthor();
        viewHolder.mentionedUsers = commentsList.get(position).getMentionedUsers();

        HashMap<String, String> mentionedUsers = commentsList.get(position).getMentionedUsers();

        if (context != null) {
            viewHolder.context = this.context;
        }

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
                                    } else {
                                        Toast.makeText(context, "Failed to delete comment", Toast.LENGTH_SHORT).show();
                                    }
                                    dialogInterface.dismiss();
                                });

                    })
                    .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss())
                    .show();

        });

        // check for mentioned users
        if (mentionedUsers != null && viewHolder.commentContent.getText().toString().contains("@")) {

            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
            ArrayList<String> mentionUsersToString = new ArrayList<>();
            ArrayList<String> ids = new ArrayList<>();
            SpannableStringBuilder spannable = new SpannableStringBuilder(viewHolder.commentContent.getText().toString());

            mentionedUsers.forEach((uid, unmame) -> {
                mentionUsersToString.add(unmame);
                ids.add(uid);
            });

            HashMap<Integer, Integer> mentionIndexes = PostHelper.findNameIndices(mentionUsersToString,
                    viewHolder.commentContent.getText().toString());

            final int[] i = {0};
            if (!mentionIndexes.isEmpty()) {
                mentionIndexes.forEach((start, end) -> {
                    final int currentIndex = i[0]; // Capture the current index
                    i[0] = i[0] + 1;
                    rootRef.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.child(ids.get(currentIndex)).exists()) {
                                spannable.setSpan(
                                        new ForegroundColorSpan(Color.BLUE),
                                        start,
                                        end,
                                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                                );

                                ClickableSpan clickableSpan = new ClickableSpan() {
                                    @Override
                                    public void onClick(@NonNull View widget) {
                                        Intent intent = new Intent(context, UserProfileActivity.class);
                                        intent.putExtra("user_id", ids.get(currentIndex));
                                        context.startActivity(intent);
                                    }

                                    @Override
                                    public void updateDrawState(TextPaint ds) {
                                        super.updateDrawState(ds);
                                        ds.setUnderlineText(false);
                                    }
                                };
                                spannable.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                viewHolder.commentContent.setText(spannable);
                                viewHolder.commentContent.setMovementMethod(LinkMovementMethod.getInstance());
                                viewHolder.commentContent.setHighlightColor(Color.TRANSPARENT);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle error
                        }
                    });
                });
            }

        }
    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }

}