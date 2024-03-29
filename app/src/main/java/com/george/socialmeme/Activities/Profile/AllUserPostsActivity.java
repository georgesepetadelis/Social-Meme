package com.george.socialmeme.Activities.Profile;

import static com.george.socialmeme.Helpers.AppHelper.isNightModeEnabled;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.george.socialmeme.Adapters.PostRecyclerAdapter;
import com.george.socialmeme.Fragments.HomeFragment;
import com.george.socialmeme.Models.PostModel;
import com.george.socialmeme.R;

import java.util.ArrayList;
import java.util.Collections;

import maes.tech.intentanim.CustomIntent;

public class AllUserPostsActivity extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        CustomIntent.customType(AllUserPostsActivity.this, "right-to-left");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (isNightModeEnabled(AllUserPostsActivity.this)) {
            setTheme(R.style.AppTheme_Base_Night);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_user_posts);

        ImageButton backBtn = findViewById(R.id.imageButton20);
        backBtn.setOnClickListener(v -> onBackPressed());

        Bundle extras = getIntent().getExtras();
        String userID = extras.getString("userID");
        boolean reverseList = extras.getBoolean("reverse_list");

        ArrayList<PostModel> postModelArrayList = new ArrayList<>();
        PostRecyclerAdapter adapter = new PostRecyclerAdapter(postModelArrayList, AllUserPostsActivity.this, AllUserPostsActivity.this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(AllUserPostsActivity.this);
        //layoutManager.setStackFromEnd(true);
        //layoutManager.setReverseLayout(true);

        RecyclerView recyclerView = findViewById(R.id.allUserPostsRecyclerView);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);

        ArrayList<String> loadedPostsID = new ArrayList<>();

        for (PostModel post : HomeFragment.postModelArrayList) {
            if (post.getAuthorID() != null) {
                if (post.getAuthorID().equals(userID) && !loadedPostsID.contains(post.getId())) {
                    postModelArrayList.add(post);
                    loadedPostsID.add(post.getId());
                }
            }
        }

        if (reverseList) {
            Collections.reverse(postModelArrayList);
        }

    }
}