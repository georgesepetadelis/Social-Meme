package com.george.socialmeme.Activities;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.developer.kalert.KAlertDialog;
import com.george.socialmeme.Adapters.PostRecyclerAdapter;
import com.george.socialmeme.Models.PostModel;
import com.george.socialmeme.Observers.ScreenShotContentObserver;
import com.george.socialmeme.R;
import com.github.loadingview.LoadingDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
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
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;
import maes.tech.intentanim.CustomIntent;

public class UserProfileActivity extends AppCompatActivity {

    public static String userID;
    public static String username;
    public static boolean currentUserFollowsThisUser;
    public int followers = 0;
    public int following = 0;
    KAlertDialog loadingDialog;
    private ScreenShotContentObserver screenShotContentObserver;
    private DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

    void sendNotificationToUser(String notificationType) {

        final DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (notificationType.equals("profile_visit")) {

            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    String notificationID = usersRef.push().getKey();
                    String currentDate = String.valueOf(android.text.format.DateFormat.format("dd-MM-yyyy", new java.util.Date()));

                    Calendar calendar = Calendar.getInstance();
                    int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                    int currentMinutes = calendar.get(Calendar.MINUTE);

                    for (DataSnapshot snap : snapshot.getChildren()) {
                        if (snap.child("name").getValue().toString().equals(username)) {
                            String postAuthorID = snap.child("id").getValue().toString();
                            usersRef.child(postAuthorID).child("notifications").child(notificationID).child("title").setValue("New profile visitor");
                            usersRef.child(postAuthorID).child("notifications").child(notificationID).child("type").setValue("profile_visit");
                            usersRef.child(postAuthorID).child("notifications").child(notificationID).child("date").setValue(currentDate + "  " + currentHour + ":" + currentMinutes);
                            usersRef.child(postAuthorID).child("notifications").child(notificationID).child("message").setValue(user.getDisplayName() + " visited your profile");
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(UserProfileActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } if (notificationType.equals("follow")) {

            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    String notificationID = usersRef.push().getKey();
                    String currentDate = String.valueOf(android.text.format.DateFormat.format("dd-MM-yyyy", new java.util.Date()));

                    Calendar calendar = Calendar.getInstance();
                    int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                    int currentMinutes = calendar.get(Calendar.MINUTE);

                    for (DataSnapshot snap : snapshot.getChildren()) {

                        if (snap.child("name").getValue().toString().equals(username)) {
                            String postAuthorID = snap.child("id").getValue().toString();
                            usersRef.child(postAuthorID).child("notifications").child(notificationID).child("title").setValue("New follower");
                            usersRef.child(postAuthorID).child("notifications").child(notificationID).child("type").setValue("new_follower");
                            usersRef.child(postAuthorID).child("notifications").child(notificationID).child("date").setValue(currentDate + "  " + currentHour + ":" + currentMinutes);
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

                    Calendar calendar = Calendar.getInstance();
                    int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                    int currentMinutes = calendar.get(Calendar.MINUTE);

                    for (DataSnapshot snap : snapshot.getChildren()) {

                        if (snap.child("name").getValue().toString().equals(username)) {
                            String postAuthorID = snap.child("id").getValue().toString();
                            usersRef.child(postAuthorID).child("notifications").child(notificationID).child("title").setValue("Profile screenshot");
                            usersRef.child(postAuthorID).child("notifications").child(notificationID).child("type").setValue("profile_screenshot");
                            usersRef.child(postAuthorID).child("notifications").child(notificationID).child("date").setValue(currentDate + "  " + currentHour + ":" + currentMinutes);
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

                    Calendar calendar = Calendar.getInstance();
                    int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                    int currentMinutes = calendar.get(Calendar.MINUTE);

                    for (DataSnapshot snap : snapshot.getChildren()) {

                        if (snap.child("name").getValue().toString().equals(username)) {
                            String postAuthorID = snap.child("id").getValue().toString();
                            usersRef.child(postAuthorID).child("notifications").child(notificationID).child("title").setValue("Unfollow");
                            usersRef.child(postAuthorID).child("notifications").child(notificationID).child("type").setValue("unfollow");
                            usersRef.child(postAuthorID).child("notifications").child(notificationID).child("date").setValue(currentDate + "  " + currentHour + ":" + currentMinutes);
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
        UserProfileActivity.userID = null;
        UserProfileActivity.username = null;
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
        UserProfileActivity.userID = null;
        UserProfileActivity.username = null;
        try {
            getContentResolver().unregisterContentObserver(screenShotContentObserver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void copyUsernameToClipboard() {

        ClipboardManager clipboard = (ClipboardManager) UserProfileActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("username", username);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(UserProfileActivity.this, "Username copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_profile_options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    boolean isNightModeEnabled() {
        SharedPreferences sharedPref = getSharedPreferences("dark_mode", MODE_PRIVATE);
        return sharedPref.getBoolean("dark_mode", false);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.report_user_item_menu:
                reportUser();
                break;
            case R.id.block_user_item_menu:
                new AlertDialog.Builder(UserProfileActivity.this)
                        .setTitle("Are you sure?")
                        .setMessage("Are you sure you want to block " + username + " ?.\nIf you block this user you won't be able to see any memes from this user.")
                        .setPositiveButton("Yes", (dialogInterface, i) -> blockUser())
                        .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss())
                        .show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (isNightModeEnabled()) {
            setTheme(R.style.AppTheme_Base_Night);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        sendNotificationToUser("profile_visit");

        HandlerThread handlerThread = new HandlerThread("content_observer");
        handlerThread.start();
        final Handler handler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        };

        if (username == null || userID == null) {
            onBackPressed();
        }

        screenShotContentObserver = new ScreenShotContentObserver(handler, this) {
            @Override
            protected void onScreenShot(String path, String fileName) {
                File file = new File(path); // this is the file of screenshot image
                sendNotificationToUser("screenshot");
            }
        };

        loadingDialog = new KAlertDialog(UserProfileActivity.this, KAlertDialog.PROGRESS_TYPE);
        loadingDialog.getProgressHelper().setBarColor(R.color.main);
        loadingDialog.setTitleText("Loading user profile...");
        loadingDialog.setCancelable(false);

        ImageButton backBtn = findViewById(R.id.imageButton2);
        ImageButton postsOfTheMonthInfo = findViewById(R.id.imageButton9);
        CircleImageView profilePicture = findViewById(R.id.my_profile_image2);
        TextView username_tv = findViewById(R.id.textView12);
        Button followBtn = findViewById(R.id.follow_btn);
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        TextView followersCounter = findViewById(R.id.followers_my_profile3);
        TextView followingCounter = findViewById(R.id.following_my_profile3);
        TextView totalLikesCounter = findViewById(R.id.textView74);

        TextView goldTrophiesCount = findViewById(R.id.gold_trophies_count);
        TextView silverTrophiesCount = findViewById(R.id.silver_trophies_count);
        TextView bronzeTrophiesCount = findViewById(R.id.bronze_trophies_count);

        TextView userFollowsCurrentUserTextView = findViewById(R.id.textView28);
        View showFollowersView = findViewById(R.id.view11);
        View showFollowingUsersView = findViewById(R.id.view12);

        AdView mAdView = findViewById(R.id.adView4);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

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

        postsOfTheMonthInfo.setOnClickListener(view -> {
            Intent intent = new Intent(UserProfileActivity.this, PostsOfTheMonthActivity.class);
            startActivity(intent);
            CustomIntent.customType(UserProfileActivity.this, "left-to-right");
        });

        username_tv.setOnLongClickListener(view -> {
            copyUsernameToClipboard();
            return false;
        });

        // Show loading dialog
        loadingDialog.show();

        backBtn.setOnClickListener(v -> onBackPressed());

        showFollowersView.setOnClickListener(v -> {
            if (followers != 0) {
                FollowerInfoActivity.userID = userID;
                FollowerInfoActivity.displayFollowers = true;
                openUsersList();
            }
        });

        showFollowingUsersView.setOnClickListener(v -> {
            if (following != 0) {
                FollowerInfoActivity.userID = userID;
                FollowerInfoActivity.displayFollowers = false;
                openUsersList();
            }
        });

        // Load profile data
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // Check if logged-in user has blocked current user
                if (snapshot.child(user.getUid()).child("blockedUsers").child(userID).exists()) {
                    new AlertDialog.Builder(UserProfileActivity.this)
                            .setTitle("Blocked user")
                            .setMessage("You have blocked this user. You want to unblock this user?")
                            .setPositiveButton("Yes", (dialogInterface, i) ->
                                    usersRef.child(user.getUid()).child("blockedUsers").child(userID).removeValue().addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            dialogInterface.dismiss();
                                            Toast.makeText(UserProfileActivity.this, "User unblocked", Toast.LENGTH_SHORT).show();
                                        }else {
                                            Toast.makeText(UserProfileActivity.this, "Can't unblock user", Toast.LENGTH_SHORT).show();
                                            onBackPressed();
                                        }
                                    })).setNegativeButton("No", (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        onBackPressed();
                    })
                            .setCancelable(false)
                            .show();
                }

                String profilePictureURL = "none";

                // Show report/block options only
                // if this profile is not the current
                // logged-in user profile
                if (!userID.equals(user.getUid())) {
                    Toolbar toolbar = findViewById(R.id.user_profile_toolbar);
                    toolbar.setTitle("");
                    setSupportActionBar(toolbar);
                }

                if (snapshot.child(userID).child("name").getValue(String.class).equals(user.getDisplayName())) {
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

                // Load user trophies
                if (snapshot.child(userID).child("trophies").exists()) {

                    String goldTrophies = snapshot.child(userID).child("trophies").child("gold").getValue(String.class);
                    String silverTrophies = snapshot.child(userID).child("trophies").child("silver").getValue(String.class);
                    String bronzeTrophies = snapshot.child(userID).child("trophies").child("bronze").getValue(String.class);

                    goldTrophiesCount.setText(goldTrophies);
                    silverTrophiesCount.setText(silverTrophies);
                    bronzeTrophiesCount.setText(bronzeTrophies);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserProfileActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Load user posts and calculate total likes of the user
        postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                int totalLikes = 0;

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {

                    if (postSnapshot.child("name").getValue(String.class).equals(username)) {
                        PostModel postModel = new PostModel();
                        postModel.setId(postSnapshot.child("id").getValue(String.class));
                        postModel.setImgUrl(postSnapshot.child("imgUrl").getValue(String.class));
                        postModel.setLikes(postSnapshot.child("likes").getValue(String.class));
                        postModel.setName(postSnapshot.child("name").getValue(String.class));
                        postModel.setPostType(postSnapshot.child("postType").getValue(String.class));

                        if (postSnapshot.child("postType").getValue(String.class).equals("text")) {
                            postModel.setPostTitle(postSnapshot.child("joke_title").getValue(String.class));
                            postModel.setPostContentText(postSnapshot.child("joke_content").getValue(String.class));
                        }

                        totalLikes += Integer.parseInt(postSnapshot.child("likes").getValue(String.class));

                        if (postSnapshot.child("comments").exists()) {
                            postModel.setCommentsCount(String.valueOf(postSnapshot.child("comments").getChildrenCount()));
                        }else {
                            postModel.setCommentsCount("0");
                        }

                        postModelArrayList.add(postModel);

                    }

                }

                // Set total likes
                totalLikesCounter.setText(String.valueOf(totalLikes));

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

    private void reportUser() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        DatabaseReference reportsRef = FirebaseDatabase.getInstance().getReference("reports");
        reportsRef.child(user.getDisplayName()).setValue(username).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                new AlertDialog.Builder(UserProfileActivity.this)
                        .setTitle("User Reported")
                        .setMessage(username + " has been reported. Thanks for keeping Social Meme community safe")
                        .setPositiveButton("Okay", (dialogInterface, i) -> dialogInterface.dismiss())
                        .show();
            }else {
                Toast.makeText(this, "Error: Can't report user, try again later", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void blockUser() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        usersRef.child(user.getUid()).child("blockedUsers").child(username).setValue(userID).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                sendNotificationToUser("block");
                Toast.makeText(UserProfileActivity.this, "User blocked", Toast.LENGTH_SHORT).show();
                onBackPressed();
            }else {
                Toast.makeText(UserProfileActivity.this, "Error: Can't block this user", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openUsersList() {
        Intent intent = new Intent(UserProfileActivity.this, FollowerInfoActivity.class);
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