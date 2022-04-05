package com.george.socialmeme.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import com.developer.kalert.KAlertDialog;
import com.george.socialmeme.Adapters.PostRecyclerAdapter;
import com.george.socialmeme.Models.PostModel;
import com.george.socialmeme.R;
import com.github.loadingview.LoadingDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import maes.tech.intentanim.CustomIntent;

public class PostsOfTheMonthActivity extends AppCompatActivity {

    public KAlertDialog loadingDialog;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        CustomIntent.customType(PostsOfTheMonthActivity.this, "right-to-left");
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
        setContentView(R.layout.activity_posts_of_the_month);

        loadingDialog = new KAlertDialog(PostsOfTheMonthActivity.this, KAlertDialog.PROGRESS_TYPE);
        loadingDialog.getProgressHelper().setBarColor(R.color.main);
        loadingDialog.setTitleText("Loading posts...");
        loadingDialog.setCancelable(false);
        loadingDialog.show();

        final RecyclerView firstRecyclerView = findViewById(R.id.first_post_recycler_view);
        final RecyclerView secondRecyclerView = findViewById(R.id.second_post_recycler_view);
        final RecyclerView thirdRecyclerView = findViewById(R.id.third_post_recycler_view);

        final ImageButton backBtn = findViewById(R.id.imageButton3);

        final LinearLayoutManager goldPostLayoutManager = new LinearLayoutManager(PostsOfTheMonthActivity.this);
        final LinearLayoutManager silverPostLayoutManager = new LinearLayoutManager(PostsOfTheMonthActivity.this);
        final LinearLayoutManager bronzePostLayoutManager = new LinearLayoutManager(PostsOfTheMonthActivity.this);

        final ArrayList<PostModel> goldPostModelArrayList = new ArrayList<>();
        final ArrayList<PostModel> silverPostModelArrayList = new ArrayList<>();
        final ArrayList<PostModel> bronzePostModelArrayList = new ArrayList<>();

        final RecyclerView.Adapter firstPostRecyclerAdapter = new PostRecyclerAdapter(goldPostModelArrayList, PostsOfTheMonthActivity.this, PostsOfTheMonthActivity.this);
        final RecyclerView.Adapter secondPostRecyclerAdapter = new PostRecyclerAdapter(silverPostModelArrayList, PostsOfTheMonthActivity.this, PostsOfTheMonthActivity.this);
        final RecyclerView.Adapter thirdPostRecyclerAdapter = new PostRecyclerAdapter(bronzePostModelArrayList, PostsOfTheMonthActivity.this, PostsOfTheMonthActivity.this);

        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        goldPostLayoutManager.setReverseLayout(true);
        goldPostLayoutManager.setStackFromEnd(true);

        silverPostLayoutManager.setReverseLayout(true);
        silverPostLayoutManager.setStackFromEnd(true);

        bronzePostLayoutManager.setReverseLayout(true);
        bronzePostLayoutManager.setStackFromEnd(true);

        firstRecyclerView.setAdapter(firstPostRecyclerAdapter);
        firstRecyclerView.setHasFixedSize(true);
        firstRecyclerView.setLayoutManager(goldPostLayoutManager);

        secondRecyclerView.setAdapter(secondPostRecyclerAdapter);
        secondRecyclerView.setHasFixedSize(true);
        secondRecyclerView.setLayoutManager(silverPostLayoutManager);

        thirdRecyclerView.setAdapter(thirdPostRecyclerAdapter);
        thirdRecyclerView.setHasFixedSize(true);
        thirdRecyclerView.setLayoutManager(bronzePostLayoutManager);

        backBtn.setOnClickListener(view -> onBackPressed());

        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String goldPostID = snapshot.child("top_posts").child("gold").getValue(String.class);
                String silverPostID = snapshot.child("top_posts").child("silver").getValue(String.class);
                String bronzePostID = snapshot.child("top_posts").child("bronze").getValue(String.class);

                // Add gold post to array list
                PostModel goldPostModel = new PostModel();
                goldPostModel.setPostType(snapshot.child("posts").child(goldPostID).child("postType").getValue(String.class));
                goldPostModel.setId(goldPostID);
                goldPostModel.setImgUrl(snapshot.child("posts").child(goldPostID).child("authorProfilePictureURL").getValue(String.class));
                goldPostModel.setLikes(snapshot.child("posts").child(goldPostID).child("likes").getValue(String.class));
                goldPostModel.setName(snapshot.child("posts").child(goldPostID).child("name").getValue(String.class));
                goldPostModel.setImgUrl(snapshot.child("posts").child(goldPostID).child("imgUrl").getValue(String.class));

                for (DataSnapshot userSnap : snapshot.child("users").getChildren()) {
                    if (userSnap.child("name").getValue(String.class).equals(snapshot.child("posts").child(goldPostID).child("name").getValue(String.class))) {
                        goldPostModel.setAuthorID(userSnap.child("id").getValue(String.class));
                        break;
                    }
                }

                if (snapshot.child("comments").exists()) {
                    goldPostModel.setCommentsCount(String.valueOf(snapshot.child("comments").getChildrenCount()));
                }else {
                    goldPostModel.setCommentsCount("0");
                }

                // Add silver post to array list
                PostModel silverPostModel = new PostModel();
                silverPostModel.setPostType(snapshot.child("posts").child(silverPostID).child("postType").getValue(String.class));
                silverPostModel.setId(silverPostID);
                silverPostModel.setImgUrl(snapshot.child("posts").child(silverPostID).child("authorProfilePictureURL").getValue(String.class));
                silverPostModel.setLikes(snapshot.child("posts").child(silverPostID).child("likes").getValue(String.class));
                silverPostModel.setName(snapshot.child("posts").child(silverPostID).child("name").getValue(String.class));
                silverPostModel.setImgUrl(snapshot.child("posts").child(silverPostID).child("imgUrl").getValue(String.class));

                for (DataSnapshot userSnap : snapshot.child("users").getChildren()) {
                    if (userSnap.child("name").getValue(String.class).equals(snapshot.child("posts").child(silverPostID).child("name").getValue(String.class))) {
                        silverPostModel.setAuthorID(userSnap.child("id").getValue(String.class));
                        break;
                    }
                }

                if (snapshot.child("comments").exists()) {
                    silverPostModel.setCommentsCount(String.valueOf(snapshot.child("comments").getChildrenCount()));
                }else {
                    silverPostModel.setCommentsCount("0");
                }

                // Add bronze post to array list
                PostModel bronzePostModel = new PostModel();
                bronzePostModel.setPostType(snapshot.child("posts").child(bronzePostID).child("postType").getValue(String.class));
                bronzePostModel.setId(bronzePostID);
                bronzePostModel.setImgUrl(snapshot.child("posts").child(bronzePostID).child("authorProfilePictureURL").getValue(String.class));
                bronzePostModel.setLikes(snapshot.child("posts").child(bronzePostID).child("likes").getValue(String.class));
                bronzePostModel.setName(snapshot.child("posts").child(bronzePostID).child("name").getValue(String.class));
                bronzePostModel.setImgUrl(snapshot.child("posts").child(bronzePostID).child("imgUrl").getValue(String.class));

                for (DataSnapshot userSnap : snapshot.child("users").getChildren()) {
                    if (userSnap.child("name").getValue(String.class).equals(snapshot.child("posts").child(bronzePostID).child("name").getValue(String.class))) {
                        bronzePostModel.setAuthorID(userSnap.child("id").getValue(String.class));
                        break;
                    }
                }

                if (snapshot.child("comments").exists()) {
                    bronzePostModel.setCommentsCount(String.valueOf(snapshot.child("comments").getChildrenCount()));
                }else {
                    bronzePostModel.setCommentsCount("0");
                }

                goldPostModelArrayList.add(goldPostModel);
                silverPostModelArrayList.add(silverPostModel);
                bronzePostModelArrayList.add(bronzePostModel);

                firstPostRecyclerAdapter.notifyDataSetChanged();
                secondPostRecyclerAdapter.notifyDataSetChanged();
                thirdPostRecyclerAdapter.notifyDataSetChanged();

                loadingDialog.hide();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PostsOfTheMonthActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}