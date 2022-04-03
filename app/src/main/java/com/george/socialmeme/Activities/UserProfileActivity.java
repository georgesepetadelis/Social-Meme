package com.george.socialmeme.Activities;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.esc861.screenshotlistener.ScreenshotListener;
import com.george.socialmeme.Adapters.PostRecyclerAdapter;
import com.george.socialmeme.Models.PostModel;
import com.george.socialmeme.Models.UserModel;
import com.george.socialmeme.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import maes.tech.intentanim.CustomIntent;

public class UserProfileActivity extends AppCompatActivity {

    public static String userID;
    public static String username;
    public static boolean currentUserFollowsThisUser;
    public int followers = 0;
    public int following = 0;
    public KAlertDialog progressDialog;
    private final DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
    ScreenshotListener listener;

    void sendNotificationToUser(String notificationType) {

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        final String[] notification_message = {"none"};
        final String[] notification_title = {"none"};

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String notificationID = usersRef.push().getKey();
                String currentDate = String.valueOf(android.text.format.DateFormat.format("dd-MM-yyyy", new java.util.Date()));

                Calendar calendar = Calendar.getInstance();
                int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                int currentMinutes = calendar.get(Calendar.MINUTE);

                if (notificationType.equals("profile_visit")) {

                    notification_title[0] = "New profile visitor";
                    notification_message[0] = user.getDisplayName() + " visited your profile";

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

                if (notificationType.equals("follow")) {

                    notification_title[0] = "New follower";
                    notification_message[0] = user.getDisplayName() + " started following you";

                    for (DataSnapshot snap : snapshot.getChildren()) {
                        if (snap.child("name").getValue().toString().equals(username)) {
                            String postAuthorID = snap.child("id").getValue().toString();
                            usersRef.child(postAuthorID).child("notifications").child(notificationID).child("title").setValue(notification_title[0]);
                            usersRef.child(postAuthorID).child("notifications").child(notificationID).child("type").setValue("new_follower");
                            usersRef.child(postAuthorID).child("notifications").child(notificationID).child("date").setValue(currentDate + "  " + currentHour + ":" + currentMinutes);
                            usersRef.child(postAuthorID).child("notifications").child(notificationID).child("message").setValue(notification_message[0]);
                            break;
                        }
                    }
                }

                if (notificationType.equals("screenshot")) {

                    notification_title[0] = "Profile screenshot";
                    notification_message[0] = user.getDisplayName() + " screenshotted your profile";

                    for (DataSnapshot snap : snapshot.getChildren()) {

                        if (snap.child("name").getValue().toString().equals(username)) {
                            String postAuthorID = snap.child("id").getValue().toString();
                            usersRef.child(postAuthorID).child("notifications").child(notificationID).child("title").setValue(notification_title[0]);
                            usersRef.child(postAuthorID).child("notifications").child(notificationID).child("type").setValue("profile_screenshot");
                            usersRef.child(postAuthorID).child("notifications").child(notificationID).child("date").setValue(currentDate + "  " + currentHour + ":" + currentMinutes);
                            usersRef.child(postAuthorID).child("notifications").child(notificationID).child("message").setValue(notification_message[0]);
                            break;
                        }
                    }
                } else if (notificationType.equals("unfollow")) {

                    notification_title[0] = "You lost a follower :(";
                    notification_message[0] = user.getDisplayName() + " unfollowed you";

                    for (DataSnapshot snap : snapshot.getChildren()) {

                        if (snap.child("name").getValue().toString().equals(username)) {
                            String postAuthorID = snap.child("id").getValue().toString();
                            usersRef.child(postAuthorID).child("notifications").child(notificationID).child("title").setValue(notification_title[0]);
                            usersRef.child(postAuthorID).child("notifications").child(notificationID).child("type").setValue("unfollow");
                            usersRef.child(postAuthorID).child("notifications").child(notificationID).child("date").setValue(currentDate + "  " + currentHour + ":" + currentMinutes);
                            usersRef.child(postAuthorID).child("notifications").child(notificationID).child("message").setValue(notification_message[0]);
                            break;
                        }
                    }
                }

                // Find user token from DB
                // and add notification to Firestore
                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    if (userSnap.child("name").getValue(String.class).equals(username)) {

                        if (userSnap.child("fcm_token").exists()) {
                            // add notification to Firestore to send
                            // push notification from back-end
                            String firestoreNotificationID = usersRef.push().getKey();
                            Map<String, Object> notification = new HashMap<>();
                            notification.put("token", userSnap.child("fcm_token").getValue(String.class));
                            notification.put("title", notification_title[0]);
                            notification.put("message", notification_message[0]);
                            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                            firestore.collection("notifications")
                                    .document(firestoreNotificationID).set(notification);
                            break;
                        }

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserProfileActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        listener.startListening();
    }

    @Override
    public void onPause() {
        super.onPause();
        listener.stopListening();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        userID = null;
        username = null;
        listener.stopListening();
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

    public interface FirebaseCallback {
        void onComplete(String profilePictureURL, boolean userFollowsLoggedInUser, boolean followingCurrentUser);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (isNightModeEnabled()) {
            setTheme(R.style.AppTheme_Base_Night);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        Bundle extras = getIntent().getExtras();
        userID = extras.getString("user_id");
        username = extras.getString("username");

        progressDialog = new KAlertDialog(UserProfileActivity.this, KAlertDialog.PROGRESS_TYPE);
        progressDialog.getProgressHelper().setBarColor(R.color.main);
        progressDialog.setTitleText("Loading user profile...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        listener = new ScreenshotListener() {
            @Override
            public void onScreenshotDetected(String path) {
                Toast.makeText(UserProfileActivity.this, "screenshot", Toast.LENGTH_SHORT).show();
            }
        };
        listener.startListening();

        ImageButton backBtn = findViewById(R.id.imageButton2);
        ImageButton postsOfTheMonthInfo = findViewById(R.id.imageButton9);
        CircleImageView profilePicture = findViewById(R.id.my_profile_image2);
        TextView username_tv = findViewById(R.id.textView12);
        Button followBtn = findViewById(R.id.follow_btn);
        Button showAllPostsButton = findViewById(R.id.showAllUserPosts);

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

        ArrayList<PostModel> postModelArrayList = new ArrayList<>();

        RecyclerView.Adapter recyclerAdapter = new PostRecyclerAdapter(postModelArrayList, UserProfileActivity.this, UserProfileActivity.this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(UserProfileActivity.this);

        RecyclerView recyclerView = findViewById(R.id.recyclerView_user_profile);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        backBtn.setOnClickListener(v -> onBackPressed());

        postsOfTheMonthInfo.setOnClickListener(view -> {
            Intent intent = new Intent(UserProfileActivity.this, PostsOfTheMonthActivity.class);
            startActivity(intent);
            CustomIntent.customType(UserProfileActivity.this, "left-to-right");
        });

        username_tv.setOnLongClickListener(view -> {
            copyUsernameToClipboard();
            return false;
        });

        showAllPostsButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfileActivity.this, AllUserPostsActivity.class);
            intent.putExtra("userID", userID);
            startActivity(intent);
            CustomIntent.customType(UserProfileActivity.this, "left-to-right");
        });

        showFollowersView.setOnClickListener(v -> {
            if (followers != 0) {
                Intent intent = new Intent(UserProfileActivity.this, FollowerInfoActivity.class);
                intent.putExtra("userID", userID);
                intent.putExtra("display_followers", true);
                startActivity(intent);
                CustomIntent.customType(UserProfileActivity.this, "left-to-right");
            }
        });

        showFollowingUsersView.setOnClickListener(v -> {
            if (following != 0) {
                Intent intent = new Intent(UserProfileActivity.this, FollowerInfoActivity.class);
                intent.putExtra("userID", userID);
                intent.putExtra("display_followers", false);
                startActivity(intent);
                CustomIntent.customType(UserProfileActivity.this, "left-to-right");
            }
        });

        // Load user posts
        if (!HomeActivity.savedPostsArrayList.isEmpty()) {

            ArrayList<PostModel> allPostsRevered = new ArrayList<>(HomeActivity.savedPostsArrayList);
            Collections.reverse(allPostsRevered);

            int totalLoadedPosts = 0;
            for (PostModel post : allPostsRevered) {
                if (totalLoadedPosts < 5) {
                    if (post.getAuthorID() != null) {
                        if (post.getAuthorID().equals(userID)) {
                            postModelArrayList.add(post);
                            totalLoadedPosts+=1;
                        }
                    }
                }
            }

            // Load total likes
            int totalLikes = 0;
            for (PostModel postModel : postModelArrayList) {
                int likesToInt = Integer.parseInt(postModel.getLikes());
                totalLikes += likesToInt;
            }
            totalLikesCounter.setText(String.valueOf(totalLikes));

        }

        // Load user info
        if (HomeActivity.savedUserProfiles != null && !HomeActivity.savedUserProfiles.isEmpty()) {
            // Load saved user data
            for (int userIndex = 1; userIndex < HomeActivity.savedUserProfiles.size() - 1; userIndex++) {
                if (HomeActivity.savedUserProfiles.get(userIndex).getUsername().equals(username)) {

                    UserModel userModel = HomeActivity.savedUserProfiles.get(userIndex);
                    
                    if (!userID.equals(user.getUid())) {
                        Toolbar toolbar = findViewById(R.id.user_profile_toolbar);
                        toolbar.setTitle("");
                        setSupportActionBar(toolbar);
                    }
                    
                    if (userModel.currentUserFollowLoggedInUser()) {
                        userFollowsCurrentUserTextView.setVisibility(View.GONE);
                    } else {
                        userFollowsCurrentUserTextView.setVisibility(View.VISIBLE);
                    }

                    if (userModel.isFollowingCurrentUser()) {
                        followBtn.setText("Unfollow");
                    } else {
                        followBtn.setText("Follow");
                    }

                    username_tv.setText(userModel.getUsername());
                    Glide.with(this).load(userModel.getProfilePictureURL()).into(profilePicture);
                    totalLikesCounter.setText(userModel.getTotalLikes());
                    followersCounter.setText(userModel.getFollowers());
                    followingCounter.setText(userModel.getFollowing());
                    goldTrophiesCount.setText(userModel.getGoldTrophiesCounter());
                    silverTrophiesCount.setText(userModel.getSilverTrophiesCounter());
                    bronzeTrophiesCount.setText(userModel.getBronzeTrophiesCounter());

                    progressDialog.hide();
                    
                    break;
                }
            }

        } else {

            FirebaseCallback firebaseCallback = (profilePictureURL, userFollowsLoggedInUser, followingCurrentUser) -> {
                // Save current user data to avoid
                // loading data from the same user again later
                UserModel userModel = new UserModel();
                userModel.setUserID(userID);
                userModel.setFollowers(followersCounter.getText().toString());
                userModel.setFollowing(followingCounter.getText().toString());
                userModel.setUsername(username);
                userModel.setProfilePictureURL(profilePictureURL);
                userModel.setGoldTrophiesCounter(goldTrophiesCount.getText().toString());
                userModel.setSilverTrophiesCounter(silverTrophiesCount.getText().toString());
                userModel.setBronzeTrophiesCounter(bronzeTrophiesCount.getText().toString());
                userModel.setTotalLikes(userModel.getTotalLikes());
                userModel.setUserFollowingLoggedInUser(followingCurrentUser);
                userModel.setFollowingCurrentUser(followingCurrentUser);
                HomeActivity.savedUserProfiles.add(userModel);
                sendNotificationToUser("profile_visit");
            };

            // Load user data from DB
            rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    String profilePictureURL = "none";
                    String username_from_db = snapshot.child("users").child(userID).child("name").getValue(String.class);
                    username_tv.setText(username_from_db);

                    boolean followingCurrentUser = false;
                    boolean userFollowsLoggedInUser = false;

                    // Check if logged-in user has
                    // blocked current user to show an alert
                    if (snapshot.child("users").child(user.getUid()).child("blockedUsers").child(userID).exists()) {

                        new AlertDialog.Builder(UserProfileActivity.this)
                                .setTitle("Blocked user")
                                .setMessage("You have blocked this user. You want to unblock this user?")
                                .setPositiveButton("Yes", (dialogInterface, i) ->
                                        usersRef.child(user.getUid())
                                                .child("blockedUsers").child(userID)
                                                .removeValue().addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                dialogInterface.dismiss();
                                                Toast.makeText(UserProfileActivity.this, "User unblocked", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(UserProfileActivity.this, "Can't unblock user", Toast.LENGTH_SHORT).show();
                                                onBackPressed();
                                            }
                                        })).setNegativeButton("No", (dialogInterface, i) -> {
                            dialogInterface.dismiss();
                            onBackPressed();
                        }).setCancelable(false).show();

                    }

                    // Show report/block options only
                    // if this profile is not the current
                    // logged-in user profile
                    if (!userID.equals(user.getUid())) {
                        Toolbar toolbar = findViewById(R.id.user_profile_toolbar);
                        toolbar.setTitle("");
                        setSupportActionBar(toolbar);
                    }

                    if (snapshot.child("users").child(userID).child("name").getValue(String.class).equals(user.getDisplayName())) {
                        followBtn.setVisibility(View.GONE);
                        userFollowsCurrentUserTextView.setVisibility(View.GONE);
                    }

                    if (snapshot.child("users").child(userID).child("profileImgUrl").exists()) {
                        profilePictureURL = snapshot.child("users").child(userID).child("profileImgUrl").getValue(String.class);
                    }

                    if (!profilePictureURL.equals("none")) {
                        Glide.with(getApplicationContext()).load(profilePictureURL).into(profilePicture);
                    }

                    if (!snapshot.child("users").child(userID).child("following").child(user.getUid()).exists()) {
                        userFollowsCurrentUserTextView.setVisibility(View.VISIBLE);
                    } else {
                        userFollowsCurrentUserTextView.setVisibility(View.GONE);
                        userFollowsLoggedInUser = true;
                    }

                    // check logged in user follows this user
                    if (snapshot.child("users").child(user.getUid()).child("following").exists()) {

                        if (snapshot.child("users").child(user.getUid()).child("following").child(userID).exists()) {
                            currentUserFollowsThisUser = true;
                            followingCurrentUser = true;
                            followBtn.setText("Unfollow");
                        } else {
                            followingCurrentUser = false;
                            currentUserFollowsThisUser = false;
                        }
                    }

                    // set followers and following counter values
                    if (snapshot.child("users").child(userID).child("following").exists()) {
                        following = (int) snapshot.child("users").child(userID).child("following").getChildrenCount();
                        followingCounter.setText(String.format("%d", following));
                    }

                    if (snapshot.child("users").child(userID).child("followers").exists()) {
                        followers = (int) snapshot.child("users").child(userID).child("followers").getChildrenCount();
                        followersCounter.setText(String.format("%d", followers));
                    }

                    // Load user trophies
                    if (snapshot.child("users").child(userID).child("trophies").exists()) {

                        String goldTrophies = snapshot.child("users").child(userID).child("trophies").child("gold").getValue(String.class);
                        String silverTrophies = snapshot.child("users").child(userID).child("trophies").child("silver").getValue(String.class);
                        String bronzeTrophies = snapshot.child("users").child(userID).child("trophies").child("bronze").getValue(String.class);

                        goldTrophiesCount.setText(goldTrophies);
                        silverTrophiesCount.setText(silverTrophies);
                        bronzeTrophiesCount.setText(bronzeTrophies);

                    }

                    recyclerAdapter.notifyDataSetChanged();
                    progressDialog.hide();
                    firebaseCallback.onComplete(profilePictureURL, userFollowsLoggedInUser, followingCurrentUser);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(UserProfileActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        

        followBtn.setOnClickListener(v -> {

            progressDialog.show();
            progressDialog.setTitleText("Loading...");

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
                        progressDialog.hide();

                    } else if (followBtn.getText().toString().equals("Follow")) {
                        // follow this user
                        usersRef.child(userID).child("followers").child(user.getUid()).setValue(user.getUid());
                        usersRef.child(user.getUid()).child("following").child(userID).setValue(userID);
                        followers = followers + 1;
                        followersCounter.setText(String.valueOf(followers));
                        followBtn.setText("Unfollow");
                        sendNotificationToUser("follow");
                        progressDialog.hide();
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
            } else {
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
            } else {
                Toast.makeText(UserProfileActivity.this, "Error: Can't block this user", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openUsersList() {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        CustomIntent.customType(UserProfileActivity.this, "right-to-left");
    }

}