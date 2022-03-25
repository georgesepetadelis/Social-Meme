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
import com.george.socialmeme.ViewHolders.AudioItemViewHolder;
import com.george.socialmeme.ViewHolders.ImageItemViewHolder;
import com.george.socialmeme.ViewHolders.PostTextItemViewHolder;
import com.george.socialmeme.ViewHolders.PostsOfTheMonthItemViewHolder;
import com.george.socialmeme.ViewHolders.VideoItemViewHolder;
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
import com.hugomatilla.audioplayerview.AudioPlayerView;
import com.squareup.picasso.Callback;
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
        }

        if (postList.get(position).getPostType().equals("postsOfTheMonth")) {
            return 2;
        }

        if (postList.get(position).getPostType().equals("audio")) {
            return 3;
        }

        if (postList.get(position).getPostType().equals("text")) {
            return 4;
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
            return new VideoItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.video_post_item, parent, false));
        }
        if (viewType == 2) {
            return new PostsOfTheMonthItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.posts_of_the_month_view_item, parent, false));
        }
        if (viewType == 3) {
            return new AudioItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.audio_post_item, parent, false));
        }
        if (viewType == 4) {
            return new PostTextItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.text_post_item, parent, false));
        }
        return new ImageItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.image_post_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        DatabaseReference likeRef = FirebaseDatabase.getInstance().getReference("likes");
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (postList.get(position).getPostType().equals("postsOfTheMonth")) {
            PostsOfTheMonthItemViewHolder viewHolder = (PostsOfTheMonthItemViewHolder) holder;
            viewHolder.setContext(context);
        }

        if (postList.get(position).getPostType().equals("text")) {

            // Bind audio view holder
            PostTextItemViewHolder textItemViewHolder = (PostTextItemViewHolder) holder;
            textItemViewHolder.setContext(context);

            if (HomeActivity.anonymous) {
                textItemViewHolder.commentsCount.setVisibility(View.GONE);
                textItemViewHolder.postOptionsButton.setVisibility(View.GONE);
                textItemViewHolder.openCommentsView.setVisibility(View.GONE);
                textItemViewHolder.shareBtn.setVisibility(View.GONE);
            }

            // check if post is liked or not
            likeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if (!HomeActivity.anonymous) {

                        // check if current post is liked from this user
                        if (snapshot.child(textItemViewHolder.postID).hasChild(user.getUid())) {
                            // post is liked form this user
                            textItemViewHolder.like_btn.setImageResource(R.drawable.ic_like_filled);

                        } else {
                            // post is not liked from this user
                            textItemViewHolder.like_btn.setImageResource(R.drawable.ic_like);
                        }
                    } else {
                        textItemViewHolder.like_btn.setEnabled(false);
                    }

                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    Toast.makeText(context, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            });

            textItemViewHolder.postID = postList.get(position).getId();
            textItemViewHolder.username.setText(postList.get(position).getName());
            textItemViewHolder.userID = postList.get(position).getAuthorID();
            textItemViewHolder.like_counter_tv.setText(postList.get(position).getLikes());
            textItemViewHolder.commentsCount.setText(postList.get(position).getCommentsCount());

            // Load Title and content for the post
            textItemViewHolder.postTitle.setText(postList.get(position).getPostTitle());
            textItemViewHolder.postContentText.setText(postList.get(position).getPostContentText());

            // Load profile picture
            String profilePictureUrl = postList.get(position).getProfileImgUrl();
            if (profilePictureUrl != null) {
                if (!profilePictureUrl.equals("none")) {
                    Glide.with(context).load(profilePictureUrl).into(textItemViewHolder.profilePicture);
                }
            }

            textItemViewHolder.shareBtn.setOnClickListener(view -> {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("post_url", postList.get(position).getImgUrl());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(context, "Audio URL copied to clipboard", Toast.LENGTH_SHORT).show();
            });

        }

        if (postList.get(position).getPostType().equals("audio")) {

            // Bind audio view holder
            AudioItemViewHolder audioViewHolder = (AudioItemViewHolder) holder;
            audioViewHolder.setContext(context);

            if (HomeActivity.anonymous) {
                audioViewHolder.commentsCounter.setVisibility(View.GONE);
                audioViewHolder.postOptionsBtn.setVisibility(View.GONE);
                audioViewHolder.openCommentsView.setVisibility(View.GONE);
                audioViewHolder.shareBtn.setVisibility(View.GONE);
            }

            // check if post is liked or not
            likeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if (!HomeActivity.anonymous) {

                        // check if current post is liked from this user
                        if (snapshot.child(audioViewHolder.postID).hasChild(user.getUid())) {
                            // post is liked form this user
                            audioViewHolder.likeBtn.setImageResource(R.drawable.ic_like_filled);

                        } else {
                            // post is not liked from this user
                            audioViewHolder.likeBtn.setImageResource(R.drawable.ic_like);
                        }
                    } else {
                        // disable like button
                        audioViewHolder.likeBtn.setEnabled(false);
                    }

                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    Toast.makeText(context, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            });

            // Get current post id set username, authorID, profile picture URL, postType, likes and post image URL
            audioViewHolder.postID = postList.get(position).getId();
            audioViewHolder.usernameTV.setText(postList.get(position).getName());
            audioViewHolder.authorID = postList.get(position).getAuthorID();
            audioViewHolder.likesCounter.setText(postList.get(position).getLikes());
            audioViewHolder.audioURL = postList.get(position).getImgUrl();
            audioViewHolder.audioName.setText(postList.get(position).getAudioName());
            audioViewHolder.commentsCounter.setText(postList.get(position).getCommentsCount());

            // Load audio source
            audioViewHolder.audioPlayerView.withUrl(postList.get(position).getImgUrl());

            // Load profile picture
            String profilePictureUrl = postList.get(position).getProfileImgUrl();
            if (profilePictureUrl != null) {
                if (!profilePictureUrl.equals("none")) {
                    Glide.with(context).load(profilePictureUrl).into(audioViewHolder.profilePicture);
                }
            }

            audioViewHolder.shareBtn.setOnClickListener(view -> {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("post_url", postList.get(position).getImgUrl());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(context, "Audio URL copied to clipboard", Toast.LENGTH_SHORT).show();
            });

        }

        if (postList.get(position).getPostType().equals("video")) {

            // bind video view holder
            VideoItemViewHolder videoViewHolder = (VideoItemViewHolder) holder;
            videoViewHolder.setContext(context);

            if (HomeActivity.anonymous) {
                videoViewHolder.commentsCount.setVisibility(View.GONE);
                videoViewHolder.postOptionsButton.setVisibility(View.GONE);
                videoViewHolder.commentsBtn.setVisibility(View.GONE);
                videoViewHolder.shareBtn.setVisibility(View.GONE);
            }

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

        }

        if (postList.get(position).getPostType().equals("image")) {

            // bind image view holder
            ImageItemViewHolder imageViewHolder = (ImageItemViewHolder) holder;
            imageViewHolder.setContext(context);

            if (HomeActivity.anonymous) {
                imageViewHolder.commentsCount.setVisibility(View.GONE);
                imageViewHolder.show_comments_btn.setVisibility(View.GONE);
                imageViewHolder.showPostOptionsButton.setVisibility(View.GONE);
                imageViewHolder.shareBtn.setVisibility(View.GONE);
            }

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

            Picasso.get().load(postList.get(position).getImgUrl()).into(imageViewHolder.postImg, new Callback() {
                @Override
                public void onSuccess() {
                    imageViewHolder.loadingProgressBar.setVisibility(View.GONE);
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(context, "Can't load this image: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            });
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
