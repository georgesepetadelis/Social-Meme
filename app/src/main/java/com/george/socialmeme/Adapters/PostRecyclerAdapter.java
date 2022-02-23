package com.george.socialmeme.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.george.socialmeme.Activities.HomeActivity;
import com.george.socialmeme.Models.PostModel;
import com.george.socialmeme.R;
import com.george.socialmeme.ViewHolders.ImageViewHolder;
import com.george.socialmeme.ViewHolders.VideoViewHolder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.potyvideo.library.globalEnums.EnumAspectRatio;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PostRecyclerAdapter extends RecyclerView.Adapter {

    List<PostModel> postList;
    Context context;
    Activity activity;

    @Override
    public int getItemViewType(int position) {
        if (postList.get(position).getPostType().equals("video")) {
            return 1;
        }
        return 0;
    }

    public void addAll(List<PostModel> newPosts) {
        int initSize = postList.size();
        postList.addAll(newPosts);
        notifyItemRangeChanged(initSize, postList.size());
    }

    public String getLastItemID() {
        return postList.get(postList.size() - 1).getId();
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
            return new VideoViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.post_video_item, parent, false));
        }
        return new ImageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        DatabaseReference likeRef = FirebaseDatabase.getInstance().getReference("likes");
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

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
                        if (snapshot.child(videoViewHolder.id).hasChild(user.getUid())) {
                            // post is liked form this user
                            videoViewHolder.like_btn.setImageResource(R.drawable.ic_thumb_up_filled);

                        } else {
                            // post is not liked from this user
                            videoViewHolder.like_btn.setImageResource(R.drawable.ic_thump_up_outline);
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
            videoViewHolder.id = postList.get(position).getId();
            videoViewHolder.username.setText(postList.get(position).getName());
            videoViewHolder.userID = postList.get(position).getAuthorID();
            videoViewHolder.like_counter_tv.setText(postList.get(position).getLikes());
            videoViewHolder.videoURL = postList.get(position).getImgUrl();

            // Load video source
            HashMap<String, String> extraHeaders = new HashMap<>();
            extraHeaders.put("foo", "bar");
            videoViewHolder.andExoPlayerView.setSource(postList.get(position).getImgUrl(), extraHeaders);
            videoViewHolder.andExoPlayerView.setPlayWhenReady(false);
            videoViewHolder.andExoPlayerView.setAspectRatio(EnumAspectRatio.ASPECT_16_9);

            // Load profile picture
            String profilePictureUrl = postList.get(position).getProfileImgUrl();
            if (profilePictureUrl != null) {
                if (!profilePictureUrl.equals("none")) {
                    Glide.with(context).load(profilePictureUrl).into(videoViewHolder.profilePicture);
                }
            }


        } else {

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
                            imageViewHolder.like_btn.setImageResource(R.drawable.ic_thumb_up_filled);

                        } else {
                            // post is not liked from this user
                            imageViewHolder.like_btn.setImageResource(R.drawable.ic_thump_up_outline);
                        }
                    } else {
                        // disable like button
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

            Picasso.get().load(postList.get(position).getImgUrl()).into(imageViewHolder.postImg);
            imageViewHolder.postImageURL = postList.get(position).getImgUrl();

            // Load profile picture
            String profilePictureUrl = postList.get(position).getProfileImgUrl();
            if (profilePictureUrl != null) {
                if (!profilePictureUrl.equals("none")) {
                    Glide.with(context).load(profilePictureUrl).into(imageViewHolder.profileImage);
                }
            }
        }

    }


    @Override
    public int getItemCount() {
        return postList.size();
    }

}
