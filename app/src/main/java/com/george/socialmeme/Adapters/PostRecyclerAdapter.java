package com.george.socialmeme.Adapters;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.george.socialmeme.Activities.HomeActivity;
import com.george.socialmeme.Models.PostModel;
import com.george.socialmeme.R;
import com.george.socialmeme.ViewHolders.ImageViewHolder;
import com.george.socialmeme.ViewHolders.PostsOfTheMonthViewHolder;
import com.george.socialmeme.ViewHolders.VideoViewHolder;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PostRecyclerAdapter extends RecyclerView.Adapter {

    List<PostModel> postList;
    Context context;
    Activity activity;

    @Override
    public int getItemViewType(int position) {
        if (postList.get(position).getPostType().equals("video")) {
            return 1;
        } if (postList.get(position).getPostType().equals("postsOfTheMonth")) {
            return 2;
        }
        return 0;
    }

    public PostRecyclerAdapter(ArrayList<PostModel> postModelArrayList, Context context, Activity activity) {
        this.postList = postModelArrayList;
        this.context = context;
        this.activity = activity;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1) {
            return new VideoViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.video_post_item, parent, false));
        }
        if (viewType == 2) {
            return new PostsOfTheMonthViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.posts_of_the_month_view_item, parent, false));
        }
        return new ImageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.image_post_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        DatabaseReference likeRef = FirebaseDatabase.getInstance().getReference("likes");
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (postList.get(position).getPostType().equals("postsOfTheMonth")) {
            PostsOfTheMonthViewHolder viewHolder = (PostsOfTheMonthViewHolder) holder;
            viewHolder.setContext(context);
        }
        if (postList.get(position).getPostType().equals("video")) {
            // bind video view holder
            VideoViewHolder videoViewHolder = (VideoViewHolder) holder;
            videoViewHolder.setContext(context);

            // check if post is liked or not
            likeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if (!HomeActivity.anonymous) {

                        // check if current post is liked from this user
                        if (snapshot.child(videoViewHolder.postID).hasChild(user.getUid())) {
                            // post is liked form this user
                            videoViewHolder.like_btn.setImageResource(R.drawable.ic_like_filled);

                        } else {
                            // post is not liked from this user
                            videoViewHolder.like_btn.setImageResource(R.drawable.ic_like);
                        }
                    } else {
                        // disable like button
                        videoViewHolder.like_btn.setEnabled(false);
                    }

                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    Toast.makeText(context, "error: " + error, Toast.LENGTH_SHORT).show();
                }
            });

            // Get current post id set username, authorID, profile picture URL, postType, likes and post image URL
            videoViewHolder.postID = postList.get(position).getId();
            videoViewHolder.username.setText(postList.get(position).getName());
            videoViewHolder.userID = postList.get(position).getAuthorID();
            videoViewHolder.like_counter_tv.setText(postList.get(position).getLikes());
            videoViewHolder.videoURL = postList.get(position).getImgUrl();
            videoViewHolder.commentsCount.setText(postList.get(position).getCommentsCount());

            // Load video source
            ExoPlayer player = new ExoPlayer.Builder(context).build();
            MediaItem mediaItem = MediaItem.fromUri(postList.get(position).getImgUrl());
            player.setMediaItem(mediaItem);
            player.prepare();
            videoViewHolder.andExoPlayerView.setPlayer(player);

            // Load profile picture
            String profilePictureUrl = postList.get(position).getProfileImgUrl();
            if (profilePictureUrl != null) {
                if (!profilePictureUrl.equals("none")) {
                    Glide.with(context).load(profilePictureUrl).into(videoViewHolder.profilePicture);
                }
            }

            videoViewHolder.shareBtn.setOnClickListener(view -> {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("post_url", postList.get(position).getImgUrl());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(context, "Video URL copied to clipboard", Toast.LENGTH_SHORT).show();
            });

        } if (postList.get(position).getPostType().equals("image")) {

            // bind image view holder
            ImageViewHolder imageViewHolder = (ImageViewHolder) holder;
            imageViewHolder.setContext(context);

            // check if post is liked or not
            likeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if (!HomeActivity.anonymous) {

                        // check if current post is liked from this user
                        if (snapshot.child(imageViewHolder.postID).hasChild(user.getUid())) {
                            // post is liked form this user
                            imageViewHolder.like_btn.setImageResource(R.drawable.ic_like_filled);

                        } else {
                            // post is not liked from this user
                            imageViewHolder.like_btn.setImageResource(R.drawable.ic_like);
                        }
                    } else {
                        imageViewHolder.like_btn.setEnabled(false);
                    }

                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    Toast.makeText(context, "error: " + error, Toast.LENGTH_SHORT).show();
                }
            });


            // Get current post id set username, authorID, profile picture URL, postType, likes and post image URL
            imageViewHolder.postID = postList.get(position).getId();
            imageViewHolder.username.setText(postList.get(position).getName());
            imageViewHolder.userID = postList.get(position).getAuthorID();
            imageViewHolder.like_counter_tv.setText(postList.get(position).getLikes());
            imageViewHolder.commentsCount.setText(postList.get(position).getCommentsCount());

            Picasso.get().load(postList.get(position).getImgUrl()).into(imageViewHolder.postImg);
            imageViewHolder.postImageURL = postList.get(position).getImgUrl();

            // Load profile picture
            String profilePictureUrl = postList.get(position).getProfileImgUrl();
            if (profilePictureUrl != null) {
                if (!profilePictureUrl.equals("none")) {
                    Glide.with(context).load(profilePictureUrl).into(imageViewHolder.profileImage);
                }
            }

            imageViewHolder.shareBtn.setOnClickListener(view -> {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("post_url", postList.get(position).getImgUrl());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(context, "Image URL copied to clipboard", Toast.LENGTH_SHORT).show();
            });

        }

    }


    @Override
    public int getItemCount() {
        return postList.size();
    }

}
