package com.george.socialmeme.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.george.socialmeme.Adapters.FollowingAndFollowersRecyclerAdapter;
import com.george.socialmeme.Models.UserModel;
import com.george.socialmeme.R;
import com.github.loadingview.LoadingDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import maes.tech.intentanim.CustomIntent;

public class FollowerInfoActivity extends AppCompatActivity {

    LoadingDialog progressDialog;
    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

    // if this equals false adapter will search for following users
    public static boolean displayFollowers;
    public static String userID;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
        final RecyclerView.Adapter recyclerAdapter = new FollowingAndFollowersRecyclerAdapter(FollowerInfoActivity.this, usersArrayList);

        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        progressDialog = LoadingDialog.Companion.get(FollowerInfoActivity.this);
        progressDialog.show();

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                progressDialog.hide();

                if (displayFollowers) {

                    for (DataSnapshot snap : snapshot.child(userID).child("followers").getChildren()) {

                        int followersCount = (int) snapshot.child(userID).child("followers").getChildrenCount();
                        UserModel userModel = new UserModel();
                        userModel.setFollowers(String.valueOf(followersCount));

                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {

                            // Confirm that the user exists
                            if (snapshot.child(snap.getValue(String.class)).exists()) {

                                userModel.setUserID(snap.getValue(String.class));

                                if (snapshot1.child("id").getValue(String.class).equals(snap.getValue(String.class))) {
                                    userModel.setUsername(snapshot.child(snapshot1.child("id").getValue(String.class)).child("name").getValue(String.class));

                                    if (snapshot.child(snapshot1.child("id").getValue(String.class)).child("profileImgUrl").exists()) {
                                        userModel.setProfilePictureURL(snapshot.child(snapshot1.child("id").getValue(String.class)).child("profileImgUrl").getValue(String.class));
                                    } else if (snapshot.child(snapshot1.child("id").getValue(String.class)).child("profileImgUrl").getValue(String.class) == null) {
                                        userModel.setProfilePictureURL("none");
                                    }

                                    usersArrayList.add(userModel);

                                }

                            } else {
                                // Delete current user node if the user does not exists
                                usersRef.child(userID).child("followers").child(snap.getValue(String.class)).removeValue();
                            }

                        }

                    }

                }else {

                    for (DataSnapshot snap : snapshot.child(userID).child("following").getChildren()) {

                        int followingUsersCount = (int) snapshot.child(userID).child("following").getChildrenCount();
                        UserModel userModel = new UserModel();
                        userModel.setFollowers(String.valueOf(followingUsersCount));

                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {

                            // Confirm that the user exists
                            if (snapshot.child(snap.getValue(String.class)).exists()) {

                                userModel.setUserID(snap.getValue(String.class));

                                if (snapshot1.child("id").getValue(String.class).equals(snap.getValue(String.class))) {
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
                                usersRef.child(userID).child("followers").child(snap.getValue(String.class)).removeValue();
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