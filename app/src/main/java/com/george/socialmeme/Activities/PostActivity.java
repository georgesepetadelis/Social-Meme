package com.george.socialmeme.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.george.socialmeme.Adapters.PostRecyclerAdapter;
import com.george.socialmeme.Models.PostModel;
import com.george.socialmeme.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

import maes.tech.intentanim.CustomIntent;

public class PostActivity extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        CustomIntent.customType(PostActivity.this, "right-to-left");
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
        setContentView(R.layout.activity_post);

        RecyclerView recyclerView = findViewById(R.id.PostRecyclerView);
        ImageButton backBtn = findViewById(R.id.imageButton2);

        Bundle extras = getIntent().getExtras();
        String postID;
        ArrayList<PostModel> postList = new ArrayList();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        PostRecyclerAdapter recyclerAdapter = new PostRecyclerAdapter(postList, this, PostActivity.this);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(layoutManager);

        backBtn.setOnClickListener(v -> onBackPressed());

        if (extras != null) {
            postID = extras.getString("post_id");

            DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("posts");
            postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        if (postSnapshot.child("id").getValue(String.class).equals(postID)) {

                            if (postSnapshot.child("name").getValue(String.class) != null) {

                                PostModel postModel = new PostModel();
                                postModel.setId(postSnapshot.child("id").getValue(String.class));

                                if (postSnapshot.child("imgUrl").getValue(String.class) == null) {
                                    postModel.setImgUrl("none");
                                } else {
                                    postModel.setImgUrl(postSnapshot.child("imgUrl").getValue(String.class));
                                }

                                postModel.setLikes(postSnapshot.child("likes").getValue(String.class));
                                postModel.setName(postSnapshot.child("name").getValue(String.class));
                                postModel.setProfileImgUrl(postSnapshot.child("authorProfilePictureURL").getValue(String.class));
                                postModel.setPostType(postSnapshot.child("postType").getValue(String.class));

                                for (DataSnapshot user : snapshot.child("users").getChildren()) {
                                    if (Objects.equals(user.child("name").getValue(String.class), postSnapshot.child("name").getValue(String.class))) {
                                        postModel.setAuthorID(user.child("id").getValue(String.class));
                                    }
                                }

                                if (postSnapshot.child("postType").getValue(String.class).equals("text")) {
                                    postModel.setPostTitle(postSnapshot.child("joke_title").getValue(String.class));
                                    postModel.setPostContentText(postSnapshot.child("joke_content").getValue(String.class));
                                }

                                if (postSnapshot.child("postType").getValue(String.class).equals("audio")) {
                                    postModel.setAudioName(postSnapshot.child("audioName").getValue(String.class));
                                }

                                if (postSnapshot.child("comments").exists()) {
                                    postModel.setCommentsCount(String.valueOf(postSnapshot.child("comments").getChildrenCount()));
                                } else {
                                    postModel.setCommentsCount("0");
                                }

                                if (!HomeActivity.singedInAnonymously) {
                                    // Show post in recycler adapter only if the user is not blocked
                                    if (!snapshot.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .child("blockedUsers").child(postSnapshot.child("name").getValue(String.class)).exists()) {
                                        postList.add(postModel);

                                    }
                                } else {
                                    postList.add(postModel);
                                }
                                recyclerAdapter.notifyItemInserted(postList.size() - 1);
                                recyclerAdapter.notifyDataSetChanged();
                            }

                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(PostActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            onBackPressed();
        }



    }
}