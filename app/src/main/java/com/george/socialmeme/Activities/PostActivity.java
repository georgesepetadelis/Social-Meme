package com.george.socialmeme.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.george.socialmeme.Adapters.PostRecyclerAdapter;
import com.george.socialmeme.Models.PostModel;
import com.george.socialmeme.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import maes.tech.intentanim.CustomIntent;

public class PostActivity extends AppCompatActivity {

    public static String postID;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // if bottomNavBar is null that means
        // that the app starter from a notification
        if (HomeActivity.bottomNavBar == null) {
            startActivity(new Intent(PostActivity.this, HomeActivity.class));
        }
        finish();
        CustomIntent.customType(PostActivity.this, "right-to-left");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
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
        //String postID;
        ArrayList<PostModel> postList = new ArrayList<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        PostRecyclerAdapter recyclerAdapter = new PostRecyclerAdapter(postList, this, PostActivity.this);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(layoutManager);

        backBtn.setOnClickListener(v -> onBackPressed());

        if (extras != null && extras.getString("post_id") != null) {
            if (postID == null) postID = extras.getString("post_id");

            DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("posts");
            postsRef.child(postID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot postSnapshot) {

                    if (postSnapshot.child("id").getValue(String.class) != null && postSnapshot.child("id").getValue(String.class).equals(postID)) {

                        if (postSnapshot.child("name").getValue(String.class) != null) {

                            PostModel postModel = postSnapshot.getValue(PostModel.class);

                            if (postModel.getComments() != null) {
                                postModel.setCommentsCount(String.valueOf(postSnapshot.child("comments").getChildrenCount()));
                            } else {
                                postModel.setCommentsCount("0");
                            }

                            postList.add(postModel);
                            recyclerAdapter.notifyItemInserted(postList.size() - 1);
                            recyclerAdapter.notifyDataSetChanged();
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