package com.george.socialmeme.Adapters;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.george.socialmeme.Activities.HomeActivity;
import com.george.socialmeme.Activities.UserProfileActivity;
import com.george.socialmeme.Dialogs.PostOptionsDialog;
import com.george.socialmeme.Models.PostModel;
import com.george.socialmeme.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.potyvideo.library.AndExoPlayerView;
import com.potyvideo.library.globalEnums.EnumAspectRatio;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import maes.tech.intentanim.CustomIntent;

public class PostRecyclerAdapter extends RecyclerView.Adapter {

    List<PostModel> postList;
    Context context;
    Activity activity;

    final private DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("posts");
    final private DatabaseReference likeRef = FirebaseDatabase.getInstance().getReference("likes");
    final private DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");


    @Override
    public int getItemViewType(int position) {
        if (postList.get(position).getPostType().equals("video")) {
            return 1;
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
            return new VideoViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.post_video_item, parent, false));
        }
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (postList.get(position).getPostType().equals("video")) {
            // bind video view holder
            VideoViewHolder viewHolder = (VideoViewHolder) holder;

            // check if post is liked or not
            likeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if (!HomeActivity.anonymous) {

                        // check if current post is liked from this user
                        if (snapshot.child(viewHolder.id).hasChild(user.getUid())) {
                            // post is liked form this user
                            viewHolder.like_btn.setImageResource(R.drawable.ic_thumb_up_filled);

                        } else {
                            // post is not liked from this user
                            viewHolder.like_btn.setImageResource(R.drawable.ic_thump_up_outline);
                        }
                    } else {
                        // disable like button
                        viewHolder.like_btn.setEnabled(false);
                    }

                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    Toast.makeText(context, "error: " + error, Toast.LENGTH_SHORT).show();
                }
            });

            // Get current post id set username, authorID, profile picture URL, postType, likes and post image URL
            viewHolder.id = postList.get(position).getId();
            viewHolder.username.setText(postList.get(position).getName());
            viewHolder.userID = postList.get(position).getAuthorID();
            viewHolder.like_counter_tv.setText(postList.get(position).getLikes());
            viewHolder.videoURL = postList.get(position).getImgUrl();

            // Load video source
            HashMap<String, String> extraHeaders = new HashMap<>();
            extraHeaders.put("foo", "bar");
            viewHolder.andExoPlayerView.setSource(postList.get(position).getImgUrl(), extraHeaders);
            viewHolder.andExoPlayerView.setPlayWhenReady(false);
            viewHolder.andExoPlayerView.setAspectRatio(EnumAspectRatio.ASPECT_16_9);

            // Load profile picture
            String profilePictureUrl = postList.get(position).getProfileImgUrl();
            if (profilePictureUrl != null) {
                if (!profilePictureUrl.equals("none")) {
                    Glide.with(context).load(profilePictureUrl).into(viewHolder.profilePicture);
                }
            }


        } else {
            // bind image view holder
            ViewHolder viewHolder = (ViewHolder) holder;

            // check if post is liked or not
            likeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if (!HomeActivity.anonymous) {

                        // check if current post is liked from this user
                        if (snapshot.child(viewHolder.id).hasChild(user.getUid())) {
                            // post is liked form this user
                            viewHolder.like_btn.setImageResource(R.drawable.ic_thumb_up_filled);

                        } else {
                            // post is not liked from this user
                            viewHolder.like_btn.setImageResource(R.drawable.ic_thump_up_outline);
                        }
                    } else {
                        // disable like button
                        viewHolder.like_btn.setEnabled(false);
                    }

                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    Toast.makeText(context, "error: " + error, Toast.LENGTH_SHORT).show();
                }
            });


            // Get current post id set username, authorID, profile picture URL, postType, likes and post image URL
            viewHolder.id = postList.get(position).getId();
            viewHolder.username.setText(postList.get(position).getName());
            viewHolder.userID = postList.get(position).getAuthorID();
            viewHolder.like_counter_tv.setText(postList.get(position).getLikes());

            //Glide.with(context).load(postList.get(position).getImgUrl()).into(holder.postImg);
            Picasso.get().load(postList.get(position).getImgUrl()).into(viewHolder.postImg);
            viewHolder.postImageURL = postList.get(position).getImgUrl();

            // Load profile picture
            String profilePictureUrl = postList.get(position).getProfileImgUrl();
            if (profilePictureUrl != null) {
                if (!profilePictureUrl.equals("none")) {
                    Glide.with(context).load(profilePictureUrl).into(viewHolder.profileImage);
                }
            }
        }

    }


    @Override
    public int getItemCount() {
        return postList.size();
    }


    class VideoViewHolder extends RecyclerView.ViewHolder {

        AndExoPlayerView andExoPlayerView;
        String id, userID, videoURL;
        TextView username, like_counter_tv;
        CircleImageView profilePicture;
        ImageButton like_btn;
        ConstraintLayout sContainer;
        boolean isLiked = false;
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);

            andExoPlayerView = itemView.findViewById(R.id.andExoPlayerView);
            profilePicture = itemView.findViewById(R.id.circleImageView2);
            username = itemView.findViewById(R.id.textView27);
            like_btn = itemView.findViewById(R.id.imageButton8);
            like_counter_tv = itemView.findViewById(R.id.textView36);
            sContainer = itemView.findViewById(R.id.sContainer);

            like_btn.setOnClickListener(v -> {

                // Animate like button when clicked
                YoYo.with(Techniques.Shake)
                        .duration(500)
                        .repeat(0)
                        .playOn(like_btn);

                isLiked = true;

                likeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (isLiked) {
                            if (snapshot.child(id).hasChild(user.getUid())) {
                                // Post is liked from this user, so user wants to unlike this post
                                like_btn.setImageResource(R.drawable.ic_thump_up_outline);
                                likeRef.child(id).child(user.getUid()).removeValue();
                                isLiked = false;

                                // Update likes to DB
                                updateLikes(id, false);
                            } else {
                                // Post is not liked from ths user, so the user wants to like this post
                                like_btn.setImageResource(R.drawable.ic_thumb_up_filled);
                                likeRef.child(id).child(user.getUid()).setValue("true");

                                // Update likes to DB
                                updateLikes(id, true);

                                sendNotificationToUser("like");

                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                        Toast.makeText(itemView.getContext(), "Error: " + error, Toast.LENGTH_LONG).show();
                    }
                });
            });

            profilePicture.setOnClickListener(v -> {

                if (!HomeActivity.anonymous) {
                    Intent intent = new Intent(context, UserProfileActivity.class);
                    usersRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot snap : snapshot.getChildren()) {
                                if (snap.child("name").getValue().toString().equals(username.getText().toString())) {
                                    UserProfileActivity.username = username.getText().toString();
                                    UserProfileActivity.userID = snap.child("id").getValue().toString();
                                    break;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    context.startActivity(intent);
                    CustomIntent.customType(context, "left-to-right");

                    sendNotificationToUser("profileVisitor");

                }

            });

            sContainer.setOnLongClickListener(v -> {
                showPostDialog();
                return false;
            });

            andExoPlayerView.setOnLongClickListener(view -> {
                showPostDialog();
                return false;
            });

        }


        public void updateLikes(String postID, boolean isNotLiked) {
            postRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                    String currentLikesToString = snapshot.child(postID).child("likes").getValue().toString();
                    int currentLikesToInt = Integer.parseInt(currentLikesToString);

                    if (isNotLiked) {

                        int newCurrentLikes = currentLikesToInt + 1;
                        String newCurrentLikesToString = Integer.toString(newCurrentLikes);

                        // Update likes on Real-time DB
                        postRef.child(postID).child("likes").setValue(newCurrentLikesToString);

                        // update likes on TextView
                        like_counter_tv.setText(newCurrentLikesToString);

                        // Animate like counter TextView
                        YoYo.with(Techniques.FadeInUp)
                                .duration(500)
                                .repeat(0)
                                .playOn(like_counter_tv);

                    } else {

                        int newCurrentLikes = currentLikesToInt - 1;
                        String newCurrentLikesToString = Integer.toString(newCurrentLikes);

                        // Update likes on Real-time DB
                        postRef.child(postID).child("likes").setValue(newCurrentLikesToString);

                        // update likes on TextView
                        like_counter_tv.setText(newCurrentLikesToString);

                        // Animate like counter TextView
                        YoYo.with(Techniques.FadeInDown)
                                .duration(500)
                                .repeat(0)
                                .playOn(like_counter_tv);

                    }

                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    Toast.makeText(context, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }

        void showPostDialog() {
            if (!HomeActivity.anonymous) {
                FragmentManager manager = ((AppCompatActivity) context).getSupportFragmentManager();
                PostOptionsDialog optionsDialog = new PostOptionsDialog();
                optionsDialog.setPostId(id);
                optionsDialog.setPostSourceURL(videoURL);
                //optionsDialog.setPostImage(postImg);

                optionsDialog.setAuthor(username.getText().toString().equals(FirebaseAuth.getInstance().getCurrentUser().getDisplayName()));

                optionsDialog.show(manager, "options");
            }
        }

        void sendNotificationToUser(String type) {

            if (type.equals("profileVisitor")) {

                usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String notificationID = usersRef.push().getKey();
                        String currentDate = String.valueOf(android.text.format.DateFormat.format("dd-MM-yyyy", new java.util.Date()));

                        for (DataSnapshot snap : snapshot.getChildren()) {

                            if (snap.child("name").getValue().toString().equals(username.getText().toString())) {
                                String postAuthorID = snap.child("id").getValue().toString();
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("title").setValue("New profile visitor");
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("type").setValue("profile_visit");
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("date").setValue(currentDate);
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("message").setValue(user.getDisplayName() + " visited your profile");
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            } else {

                usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String notificationID = usersRef.push().getKey();
                        String currentDate = String.valueOf(android.text.format.DateFormat.format("dd-MM-yyyy", new java.util.Date()));

                        for (DataSnapshot snap : snapshot.getChildren()) {

                            if (snap.child("name").getValue().toString().equals(username.getText().toString())) {
                                String postAuthorID = snap.child("id").getValue().toString();
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("title").setValue("New like");
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("type").setValue("like");
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("date").setValue(currentDate);
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("message").setValue(user.getDisplayName() + " liked your post");
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }

        }


    }

    class ViewHolder extends RecyclerView.ViewHolder {

        CardView container;
        String id, postImageURL, userID;
        TextView username, like_counter_tv;
        ImageView postImg;
        ImageButton like_btn;
        CircleImageView profileImage;
        boolean isLiked = false;
        View openUserProfileView;
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        void sendNotificationToUser(String type) {

            if (type.equals("profileVisitor")) {

                usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String notificationID = usersRef.push().getKey();
                        String currentDate = String.valueOf(android.text.format.DateFormat.format("dd-MM-yyyy", new java.util.Date()));

                        for (DataSnapshot snap : snapshot.getChildren()) {

                            if (snap.child("name").getValue().toString().equals(username.getText().toString())) {
                                String postAuthorID = snap.child("id").getValue().toString();
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("title").setValue("New profile visitor");
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("type").setValue("profile_visit");
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("date").setValue(currentDate);
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("message").setValue(user.getDisplayName() + " visited your profile");
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            } else {

                usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String notificationID = usersRef.push().getKey();
                        String currentDate = String.valueOf(android.text.format.DateFormat.format("dd-MM-yyyy", new java.util.Date()));

                        for (DataSnapshot snap : snapshot.getChildren()) {

                            if (snap.child("name").getValue().toString().equals(username.getText().toString())) {
                                String postAuthorID = snap.child("id").getValue().toString();
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("title").setValue("New like");
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("type").setValue("like");
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("date").setValue(currentDate);
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("message").setValue(user.getDisplayName() + " liked your post");
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }

        }

        public Boolean verifyPermissions() {

            // This will return the current Status
            int permissionExternalMemory = ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permissionExternalMemory != PackageManager.PERMISSION_GRANTED) {

                String[] STORAGE_PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                // If permission not granted then ask for permission real time.
                ActivityCompat.requestPermissions(activity, STORAGE_PERMISSIONS, 1);
                return false;
            }

            return true;

        }

        void downloadImage(String imageURL) {

            if (!verifyPermissions()) {
                return;
            }

            ContextWrapper contextWrapper = new ContextWrapper(context);
            File imagesDir = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(imagesDir, System.currentTimeMillis() + ".png");

            Glide.with(context)
                    .load(imageURL)
                    .into(new CustomTarget<Drawable>() {

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);

                            Toast.makeText(context, "Failed to Download Image! Please try again later.", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable com.bumptech.glide.request.transition.Transition<? super Drawable> transition) {
                            Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
                            Toast.makeText(context, "Saving Image...", Toast.LENGTH_SHORT).show();
                            saveImage(bitmap, imagesDir, file.getName());
                        }
                    });

        }

        private void saveImage(Bitmap image, File storageDir, String imageFileName) {

            boolean successDirCreated = true;
            if (!storageDir.exists()) {
                try {
                    storageDir.mkdir();
                } catch (Exception e) {
                    Toast.makeText(activity, "Failed to make folder!" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }

            }
            if (successDirCreated) {
                File imageFile = new File(storageDir, imageFileName);
                String savedImagePath = imageFile.getAbsolutePath();
                try {
                    OutputStream fOut = new FileOutputStream(imageFile);
                    image.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                    fOut.close();
                    Toast.makeText(activity, "Image Saved in: " + savedImagePath, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(activity, "Error while saving image! " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

            } else {
                Toast.makeText(activity, "Failed to make folder!", Toast.LENGTH_SHORT).show();
            }
        }

        public void updateLikes(String postID, boolean isNotLiked) {
            postRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                    String currentLikesToString = snapshot.child(postID).child("likes").getValue().toString();
                    int currentLikesToInt = Integer.parseInt(currentLikesToString);

                    if (isNotLiked) {

                        int newCurrentLikes = currentLikesToInt + 1;
                        String newCurrentLikesToString = Integer.toString(newCurrentLikes);

                        // Update likes on Real-time DB
                        postRef.child(postID).child("likes").setValue(newCurrentLikesToString);

                        // update likes on TextView
                        like_counter_tv.setText(newCurrentLikesToString);

                        // Animate like counter TextView
                        YoYo.with(Techniques.FadeInUp)
                                .duration(500)
                                .repeat(0)
                                .playOn(like_counter_tv);

                    } else {

                        int newCurrentLikes = currentLikesToInt - 1;
                        String newCurrentLikesToString = Integer.toString(newCurrentLikes);

                        // Update likes on Real-time DB
                        postRef.child(postID).child("likes").setValue(newCurrentLikesToString);

                        // update likes on TextView
                        like_counter_tv.setText(newCurrentLikesToString);

                        // Animate like counter TextView
                        YoYo.with(Techniques.FadeInDown)
                                .duration(500)
                                .repeat(0)
                                .playOn(like_counter_tv);

                    }

                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    Toast.makeText(context, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            final FirebaseAuth mAuth = FirebaseAuth.getInstance();
            final FirebaseUser user = mAuth.getCurrentUser();
            final ConstraintLayout sContainer = itemView.findViewById(R.id.second_container);
            profileImage = itemView.findViewById(R.id.user_profile_image);
            container = itemView.findViewById(R.id.post_item_container);
            username = itemView.findViewById(R.id.post_username);
            postImg = itemView.findViewById(R.id.post_image);
            like_btn = itemView.findViewById(R.id.likeBtn);
            like_counter_tv = itemView.findViewById(R.id.like_counter);
            openUserProfileView = itemView.findViewById(R.id.view_profile);
            likeRef.keepSynced(true);


                /*sContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        downloadImage(postImageURL);
                    }
                });*/


            openUserProfileView.setOnClickListener(v -> {

                if (!HomeActivity.anonymous) {
                    Intent intent = new Intent(context, UserProfileActivity.class);
                    usersRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot snap : snapshot.getChildren()) {
                                if (snap.child("name").getValue().toString().equals(username.getText().toString())) {
                                    UserProfileActivity.username = username.getText().toString();
                                    UserProfileActivity.userID = snap.child("id").getValue().toString();
                                    break;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    context.startActivity(intent);
                    CustomIntent.customType(context, "left-to-right");

                    sendNotificationToUser("profileVisitor");

                }

            });

            sContainer.setOnLongClickListener(v -> {

                if (!HomeActivity.anonymous) {
                    FragmentManager manager = ((AppCompatActivity) context).getSupportFragmentManager();
                    PostOptionsDialog optionsDialog = new PostOptionsDialog();
                    optionsDialog.setPostId(id);
                    optionsDialog.setPostImage(postImg);
                    optionsDialog.setPostSourceURL(postImageURL);

                    optionsDialog.setAuthor(username.getText().toString().equals(user.getDisplayName()));

                    optionsDialog.show(manager, "options");
                }

                return false;
            });

            like_btn.setOnClickListener(v -> {

                // Animate like button when clicked
                YoYo.with(Techniques.Shake)
                        .duration(500)
                        .repeat(0)
                        .playOn(like_btn);

                isLiked = true;

                likeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (isLiked) {
                            if (snapshot.child(id).hasChild(user.getUid())) {
                                // Post is liked from this user, so user wants to unlike this post
                                like_btn.setImageResource(R.drawable.ic_thump_up_outline);
                                likeRef.child(id).child(user.getUid()).removeValue();
                                isLiked = false;

                                // Update likes to DB
                                updateLikes(id, false);
                            } else {
                                // Post is not liked from ths user, so the user wants to like this post
                                like_btn.setImageResource(R.drawable.ic_thumb_up_filled);
                                likeRef.child(id).child(user.getUid()).setValue("true");

                                // Update likes to DB
                                updateLikes(id, true);

                                sendNotificationToUser("like");

                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                        Toast.makeText(itemView.getContext(), "Error: " + error, Toast.LENGTH_LONG).show();
                    }
                });
            });


        }
    }

}
