package com.george.socialmeme.ViewHolders;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.george.socialmeme.Activities.HomeActivity;
import com.george.socialmeme.Activities.UserProfileActivity;
import com.george.socialmeme.Dialogs.PostOptionsDialog;
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

import de.hdodenhof.circleimageview.CircleImageView;
import maes.tech.intentanim.CustomIntent;

public class VideoViewHolder extends RecyclerView.ViewHolder {

    public Context context;
    public AndExoPlayerView andExoPlayerView;
    public String id, userID, videoURL;
    public View openProfileView;
    public TextView username, like_counter_tv;
    public CircleImageView profilePicture;
    public ImageButton like_btn;
    public ConstraintLayout sContainer;
    public boolean isLiked = false;

    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = auth.getCurrentUser();

    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
    DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("posts");
    DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference("likes");

    public void setContext(Context context) {
        this.context = context;
    }

    public VideoViewHolder(@NonNull View itemView) {
        super(itemView);

        andExoPlayerView = itemView.findViewById(R.id.andExoPlayerView);
        profilePicture = itemView.findViewById(R.id.circleImageView2);
        username = itemView.findViewById(R.id.textView27);
        like_btn = itemView.findViewById(R.id.imageButton8);
        like_counter_tv = itemView.findViewById(R.id.textView36);
        sContainer = itemView.findViewById(R.id.sContainer);
        openProfileView = itemView.findViewById(R.id.view5);

        like_btn.setOnClickListener(v -> {

            // Animate like button when clicked
            YoYo.with(Techniques.Shake)
                    .duration(500)
                    .repeat(0)
                    .playOn(like_btn);

            isLiked = true;

            likesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if (isLiked) {
                        if (snapshot.child(id).hasChild(user.getUid())) {
                            // Post is liked from this user, so user wants to unlike this post
                            like_btn.setImageResource(R.drawable.ic_thump_up_outline);
                            likesRef.child(id).child(user.getUid()).removeValue();
                            isLiked = false;

                            // Update likes to DB
                            updateLikes(id, false);
                        } else {
                            // Post is not liked from ths user, so the user wants to like this post
                            like_btn.setImageResource(R.drawable.ic_thumb_up_filled);
                            likesRef.child(id).child(user.getUid()).setValue("true");

                            // Update likes to DB
                            updateLikes(id, true);

                            sendLikeNotificationToUser();

                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    Toast.makeText(itemView.getContext(), "Error: " + error, Toast.LENGTH_LONG).show();
                }
            });
        });

        openProfileView.setOnClickListener(v -> {

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
        postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                String currentLikesToString = snapshot.child(postID).child("likes").getValue().toString();
                int currentLikesToInt = Integer.parseInt(currentLikesToString);

                if (isNotLiked) {

                    int newCurrentLikes = currentLikesToInt + 1;
                    String newCurrentLikesToString = Integer.toString(newCurrentLikes);

                    // Update likes on Real-time DB
                    postsRef.child(postID).child("likes").setValue(newCurrentLikesToString);

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
                    postsRef.child(postID).child("likes").setValue(newCurrentLikesToString);

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
            optionsDialog.setPostType("video");
            optionsDialog.setAuthor(username.getText().toString().equals(user.getDisplayName()));
            optionsDialog.setAuthorName(username.getText().toString());
            optionsDialog.show(manager, "options");
        }
    }

    void sendLikeNotificationToUser() {

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