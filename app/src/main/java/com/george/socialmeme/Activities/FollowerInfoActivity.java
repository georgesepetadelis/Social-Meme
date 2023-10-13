package com.george.socialmeme.Activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.developer.kalert.KAlertDialog;
import com.george.socialmeme.Adapters.UserRecyclerAdapter;
import com.george.socialmeme.Models.UserModel;
import com.george.socialmeme.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

import maes.tech.intentanim.CustomIntent;

public class FollowerInfoActivity extends AppCompatActivity {

    public KAlertDialog progressDialog;
    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        finish();
        CustomIntent.customType(FollowerInfoActivity.this, "right-to-left");
    }

    boolean isNightModeEnabled() {
        SharedPreferences sharedPref = getSharedPreferences("dark_mode", MODE_PRIVATE);
        return sharedPref.getBoolean("dark_mode", false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (isNightModeEnabled()) {
            setTheme(R.style.AppTheme_Base_Night);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers_and_following);

        Bundle extras = getIntent().getExtras();
        String userID = extras.getString("userID");
        boolean displayFollowers = extras.getBoolean("display_followers");

        ImageButton backBtn = findViewById(R.id.imageButton5);
        TextView title = findViewById(R.id.textView26);

        backBtn.setOnClickListener(v -> onBackPressed());

        if (displayFollowers) {
            title.setText("Followers");
        } else {
            title.setText("Following");
        }

        final RecyclerView recyclerView = findViewById(R.id.recyclerView);
        final ArrayList<UserModel> usersArrayList = new ArrayList<>();
        final LinearLayoutManager layoutManager = new LinearLayoutManager(FollowerInfoActivity.this);
        final RecyclerView.Adapter recyclerAdapter = new UserRecyclerAdapter(null,FollowerInfoActivity.this, usersArrayList);

        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        progressDialog = new KAlertDialog(FollowerInfoActivity.this, KAlertDialog.PROGRESS_TYPE);
        progressDialog.getProgressHelper().setBarColor(R.color.main);
        progressDialog.setTitleText("Loading users...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                progressDialog.hide();

                if (displayFollowers) {

                    for (DataSnapshot followerSnapshot : snapshot.child(userID).child("followers").getChildren()) {

                        int followersCount = (int) snapshot.child(followerSnapshot.getValue(String.class)).child("followers").getChildrenCount();
                        UserModel userModel = new UserModel();

                        switch (followersCount) {
                            case 0:
                                userModel.setFollowers("No followers");
                                break;
                            case 1:
                                userModel.setFollowers("1 follower");
                                break;
                            default:
                                userModel.setFollowers(followersCount + " followers");
                        }

                        for (DataSnapshot usersSnapshot : snapshot.getChildren()) {

                            // Confirm that the user exists
                            if (snapshot.child(followerSnapshot.getValue(String.class)).exists()) {

                                userModel.setUserID(followerSnapshot.getValue(String.class));

                                if (Objects.equals(usersSnapshot.child("id").getValue(String.class), followerSnapshot.getValue(String.class))) {
                                    userModel.setUsername(snapshot.child(usersSnapshot.child("id").getValue(String.class)).child("name").getValue(String.class));

                                    if (snapshot.child(usersSnapshot.child("id").getValue(String.class)).child("profileImgUrl").exists()) {
                                        userModel.setProfilePictureURL(snapshot.child(usersSnapshot.child("id").getValue(String.class)).child("profileImgUrl").getValue(String.class));
                                    } else if (snapshot.child(usersSnapshot.child("id").getValue(String.class)).child("profileImgUrl").getValue(String.class) == null) {
                                        userModel.setProfilePictureURL("none");
                                    }

                                    usersArrayList.add(userModel);

                                }

                            } else {
                                // Delete current user node if the user does not exists
                                usersRef.child(userID).child("followers").child(followerSnapshot.getValue(String.class)).removeValue();
                            }

                        }

                    }

                }else {

                    for (DataSnapshot followingUserSnapshot : snapshot.child(userID).child("following").getChildren()) {

                        int followersCount = (int) snapshot.child(followingUserSnapshot.getValue(String.class)).child("followers").getChildrenCount();
                        UserModel userModel = new UserModel();

                        switch (followersCount) {
                            case 0:
                                userModel.setFollowers("No followers");
                                break;
                            case 1:
                                userModel.setFollowers("1 follower");
                                break;
                            default:
                                userModel.setFollowers(followersCount + " followers");
                        }

                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            // Confirm that the user exists
                            if (snapshot.child(followingUserSnapshot.getValue(String.class)).exists()) {
                                userModel.setUserID(followingUserSnapshot.getValue(String.class));
                                if (Objects.equals(snapshot1.child("id").getValue(String.class), followingUserSnapshot.getValue(String.class))) {
                                    userModel.setUsername(snapshot.child(snapshot1.child("id").getValue(String.class)).child("name").getValue(String.class));
                                    if (snapshot.child(snapshot1.child("id").getValue(String.class)).child("profileImgUrl").exists()) {
                                        userModel.setProfilePictureURL(snapshot.child(snapshot1.child("id").getValue(String.class)).child("profileImgUrl").getValue(String.class));
                                    } else {
                                        userModel.setProfilePictureURL("none");
                                    }
                                    usersArrayList.add(userModel);
                                }

                            } else {
                                // Delete current user node if the user not exists
                                usersRef.child(userID).child("followers").child(followingUserSnapshot.getValue(String.class)).removeValue();
                            }

                        }

                    }

                }
                
                recyclerAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FollowerInfoActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}