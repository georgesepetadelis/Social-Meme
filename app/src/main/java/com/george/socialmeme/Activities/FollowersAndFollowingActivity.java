package com.george.socialmeme.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.george.socialmeme.Adapters.FollowingAndFollowersRecyclerAdapter;
import com.george.socialmeme.Adapters.PostRecyclerAdapter;
import com.george.socialmeme.Models.PostModel;
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

public class FollowersAndFollowingActivity extends AppCompatActivity {

    LoadingDialog progressDialog;
    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

    // if this equals false adapter will search for following users
    public static boolean displayFollowers;
    public static String userID;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        CustomIntent.customType(FollowersAndFollowingActivity.this, "right-to-left");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers_and_following);

        ImageButton backBtn = findViewById(R.id.imageButton5);
        TextView title = findViewById(R.id.textView26);

        backBtn.setOnClickListener(v -> onBackPressed());

        title.setOnClickListener(v -> {
            if (displayFollowers) {
                title.setText("Followers");
            } else {
                title.setText("Following");
            }
        });

        final RecyclerView recyclerView = findViewById(R.id.recyclerView);
        final ArrayList<UserModel> usersArrayList = new ArrayList<>();
        final LinearLayoutManager layoutManager = new LinearLayoutManager(FollowersAndFollowingActivity.this);
        final RecyclerView.Adapter recyclerAdapter = new FollowingAndFollowersRecyclerAdapter(FollowersAndFollowingActivity.this, usersArrayList);

        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        progressDialog = LoadingDialog.Companion.get(FollowersAndFollowingActivity.this);

        progressDialog.show();

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // hide progress bar
                progressDialog.hide();

                if (displayFollowers) {

                    for (DataSnapshot snap : snapshot.child(userID).child("followers").getChildren()) {

                        UserModel userModel = new UserModel();
                        userModel.setFollowers(String.valueOf(snapshot.child(userID).child("followers").getChildrenCount()));
                        userModel.setUserID(snap.getValue(String.class));


                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {

                            if (snapshot1.child("id").getValue(String.class).equals(snap.getValue(String.class))) {
                                userModel.setUsername(snapshot.child(snapshot1.child("id").getValue(String.class)).child("name").getValue(String.class));

                                if (snapshot.child(snapshot1.child("id").getValue(String.class)).child("profileImgUrl").exists()) {
                                    userModel.setProfilePictureURL(snapshot.child(snapshot1.child("id").getValue(String.class)).child("profileImgUrl").getValue(String.class));
                                } else if (snapshot.child(snapshot1.child("id").getValue(String.class)).child("profileImgUrl").getValue(String.class) == null) {
                                    userModel.setProfilePictureURL("none");
                                }

                            }

                        }

                        usersArrayList.add(userModel);

                    }

                }else {

                    for (DataSnapshot snap : snapshot.child(userID).child("following").getChildren()) {

                        UserModel userModel = new UserModel();
                        userModel.setFollowers(String.valueOf(snapshot.child(userID).child("following").getChildrenCount()));
                        userModel.setUserID(snap.getValue(String.class));

                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {

                            if (snapshot1.child("id").getValue(String.class).equals(snap.getValue(String.class))) {
                                userModel.setUsername(snapshot.child(snapshot1.child("id").getValue(String.class)).child("name").getValue(String.class));

                                if (snapshot.child(snapshot1.child("id").getValue(String.class)).child("profileImgUrl").exists()) {
                                    userModel.setProfilePictureURL(snapshot.child(snapshot1.child("id").getValue(String.class)).child("profileImgUrl").getValue(String.class));
                                } else {
                                    userModel.setProfilePictureURL("none");
                                }
                            }

                        }

                        usersArrayList.add(userModel);

                    }

                }
                
                recyclerAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FollowersAndFollowingActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}