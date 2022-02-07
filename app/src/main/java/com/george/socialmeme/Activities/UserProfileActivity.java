package com.george.socialmeme.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.george.socialmeme.Adapters.PostRecyclerAdapter;
import com.george.socialmeme.Models.PostModel;
import com.george.socialmeme.Observers.ScreenShotContentObserver;
import com.george.socialmeme.R;
import com.github.loadingview.LoadingDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import maes.tech.intentanim.CustomIntent;

public class UserProfileActivity extends AppCompatActivity {

    public static String userID;
    public static String username;
    public static boolean currentUserFollowsThisUser;
    public int followers = 0;
    public int following = 0;
    LoadingDialog loadingDialog;
    private ScreenShotContentObserver screenShotContentObserver;

    void sendNotificationToUser(String notificationType) {

        final DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (notificationType.equals("follow")) {

            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    String notificationID = usersRef.push().getKey();
                    String currentDate = String.valueOf(android.text.format.DateFormat.format("dd-MM-yyyy", new java.util.Date()));

                    for (DataSnapshot snap : snapshot.getChildren()) {

                        if (snap.child("name").getValue().toString().equals(username)) {
                            String postAuthorID = snap.child("id").getValue().toString();
                            usersRef.child(postAuthorID).child("notifications").child(notificationID).child("title").setValue("New follower");
                            usersRef.child(postAuthorID).child("notifications").child(notificationID).child("notificationType").setValue("new_follower");
                            usersRef.child(postAuthorID).child("notifications").child(notificationID).child("date").setValue(currentDate);
                            usersRef.child(postAuthorID).child("notifications").child(notificationID).child("message").setValue(user.getDisplayName() + " started following you");
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(UserProfileActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } if (notificationType.equals("screenshot")) {

            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    String notificationID = usersRef.push().getKey();
                    String currentDate = String.valueOf(android.text.format.DateFormat.format("dd-MM-yyyy", new java.util.Date()));

                    for (DataSnapshot snap : snapshot.getChildren()) {

                        if (snap.child("name").getValue().toString().equals(username)) {
                            String postAuthorID = snap.child("id").getValue().toString();
                            usersRef.child(postAuthorID).child("notifications").child(notificationID).child("title").setValue("Profile screenshot");
                            usersRef.child(postAuthorID).child("notifications").child(notificationID).child("notificationType").setValue("profile_screenshot");
                            usersRef.child(postAuthorID).child("notifications").child(notificationID).child("date").setValue(currentDate);
                            usersRef.child(postAuthorID).child("notifications").child(notificationID).child("message").setValue(user.getDisplayName() + " screenshotted your profile");
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(UserProfileActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else if (notificationType.equals("unfollow")) {

            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    String notificationID = usersRef.push().getKey();
                    String currentDate = String.valueOf(android.text.format.DateFormat.format("dd-MM-yyyy", new java.util.Date()));

                    for (DataSnapshot snap : snapshot.getChildren()) {

                        if (snap.child("name").getValue().toString().equals(username)) {
                            String postAuthorID = snap.child("id").getValue().toString();
                            usersRef.child(postAuthorID).child("notifications").child(notificationID).child("title").setValue("Unfollow");
                            usersRef.child(postAuthorID).child("notifications").child(notificationID).child("notificationType").setValue("unfollow");
                            usersRef.child(postAuthorID).child("notifications").child(notificationID).child("date").setValue(currentDate);
                            usersRef.child(postAuthorID).child("notifications").child(notificationID).child("message").setValue(user.getDisplayName() + " unfollowed you");
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(UserProfileActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }

    }

    @Override
    public void onResume() {
        super.onResume();
        getContentResolver().registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                true,
                screenShotContentObserver
        );
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            getContentResolver().unregisterContentObserver(screenShotContentObserver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            getContentResolver().unregisterContentObserver(screenShotContentObserver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        HandlerThread handlerThread = new HandlerThread("content_observer");
        handlerThread.start();
        final Handler handler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        };

        screenShotContentObserver = new ScreenShotContentObserver(handler, this) {
            @Override
            protected void onScreenShot(String path, String fileName) {
                File file = new File(path); // this is the file of screenshot image
                sendNotificationToUser("screenshot");
            }
        };

        loadingDialog = LoadingDialog.Companion.get(UserProfileActivity.this);
        ImageButton backBtn = findViewById(R.id.imageButton2);
        CircleImageView profilePicture = findViewById(R.id.my_profile_image2);
        TextView username_tv = findViewById(R.id.textView12);
        Button followBtn = findViewById(R.id.follow_btn);
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        TextView followersCounter = findViewById(R.id.followers_my_profile3);
        TextView followingCounter = findViewById(R.id.following_my_profile3);
        TextView userFollowsCurrentUserTextView = findViewById(R.id.textView28);
        View showFollowersView = findViewById(R.id.view11);
        View showFollowingUsersView = findViewById(R.id.view12);

        RecyclerView recyclerView = findViewById(R.id.recyclerView_user_profile);
        ArrayList<PostModel> postModelArrayList = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(UserProfileActivity.this);
        RecyclerView.Adapter recyclerAdapter = new PostRecyclerAdapter(postModelArrayList, UserProfileActivity.this, UserProfileActivity.this);
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("posts");

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        // show loading dialog
        loadingDialog.show();

        backBtn.setOnClickListener(v -> onBackPressed());



        showFollowersView.setOnClickListener(v -> {
            FollowersAndFollowingActivity.userID = userID;
            FollowersAndFollowingActivity.displayFollowers = true;
            openUsersList();
        });

        showFollowingUsersView.setOnClickListener(v -> {
            FollowersAndFollowingActivity.userID = userID;
            FollowersAndFollowingActivity.displayFollowers = false;
            openUsersList();
        });

        // Load profile data
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String profilePictureURL = "none";

                if (snapshot.child(userID).child("id").getValue(String.class).equals(user.getUid())) {
                    followBtn.setVisibility(View.GONE);
                    userFollowsCurrentUserTextView.setVisibility(View.GONE);
                }

                if (snapshot.child(userID).child("profileImgUrl").exists()) {
                    profilePictureURL = snapshot.child(userID).child("profileImgUrl").getValue(String.class);
                }

                String username = snapshot.child(userID).child("name").getValue(String.class);

                if (!profilePictureURL.equals("none")) {
                    Glide.with(UserProfileActivity.this).load(profilePictureURL).into(profilePicture);
                }

                if (!snapshot.child(userID).child("following").child(user.getUid()).exists()) {
                    userFollowsCurrentUserTextView.setVisibility(View.VISIBLE);
                }else {
                    userFollowsCurrentUserTextView.setVisibility(View.GONE);
                }

                username_tv.setText(username);

                if (HomeActivity.anonymous) {
                    followBtn.setEnabled(false);
                }

                // check logged in user follows this user
                if (snapshot.child(user.getUid()).child("following").exists()) {

                    if (snapshot.child(user.getUid()).child("following").child(userID).exists()) {
                        currentUserFollowsThisUser = true;
                        followBtn.setText("Unfollow");
                    }else {
                        currentUserFollowsThisUser = false;
                    }
                }

                // set followers and following counter values
                if (snapshot.child(userID).child("following").exists()) {
                    following = (int) snapshot.child(userID).child("following").getChildrenCount();
                    followingCounter.setText(String.format("%d", following));
                }

                if (snapshot.child(userID).child("followers").exists()) {
                    followers = (int) snapshot.child(userID).child("followers").getChildrenCount();
                    followersCounter.setText(String.format("%d", followers));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserProfileActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                for (DataSnapshot snap : snapshot.getChildren()) {

                    if (snap.child("name").getValue(String.class).equals(username)) {
                        PostModel postModel = new PostModel();
                        postModel.setId(snap.child("id").getValue(String.class));
                        postModel.setImgUrl(snap.child("imgUrl").getValue(String.class));
                        postModel.setLikes(snap.child("likes").getValue(String.class));
                        postModel.setName(snap.child("name").getValue(String.class));
                        postModel.setPostType(snap.child("postType").getValue(String.class));

                        postModelArrayList.add(postModel);

                    }

                }

                recyclerAdapter.notifyDataSetChanged();

                loadingDialog.hide();

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(UserProfileActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        followBtn.setOnClickListener(v -> {
            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (followBtn.getText().toString().equals("Unfollow")) {

                        // unfollow this user
                        if (snapshot.child(userID).child("followers").exists()) {
                            usersRef.child(userID).child("followers").child(user.getUid()).removeValue();
                        }

                        if (snapshot.child(user.getUid()).child("following").exists()) {
                            usersRef.child(user.getUid()).child("following").child(userID).removeValue();
                        }

                        followBtn.setText("Follow");

                        followers = followers - 1;

                        followersCounter.setText(String.valueOf(followers));

                        currentUserFollowsThisUser = false;

                        sendNotificationToUser("unfollow");

                    } else if (followBtn.getText().toString().equals("Follow")) {

                        // follow this user
                        usersRef.child(userID).child("followers").child(user.getUid()).setValue(user.getUid());
                        usersRef.child(user.getUid()).child("following").child(userID).setValue(userID);

                        followers = followers + 1;

                        followersCounter.setText(String.valueOf(followers));

                        followBtn.setText("Unfollow");

                        sendNotificationToUser("follow");

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(UserProfileActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

    }

    private void openUsersList() {
        Intent intent = new Intent(UserProfileActivity.this, FollowersAndFollowingActivity.class);
        startActivity(intent);
        CustomIntent.customType(UserProfileActivity.this, "left-to-right");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        CustomIntent.customType(UserProfileActivity.this, "right-to-left");
    }

}