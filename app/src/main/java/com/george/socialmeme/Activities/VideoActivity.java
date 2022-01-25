package com.george.socialmeme.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.george.socialmeme.R;
import com.potyvideo.library.AndExoPlayerView;

import java.util.HashMap;

public class VideoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        AndExoPlayerView andExoPlayerView = findViewById(R.id.andExoPlayerView);

        HashMap<String , String> extraHeaders = new HashMap<>();
        extraHeaders.put("foo","bar");
        andExoPlayerView.setSource("https://firebasestorage.googleapis.com/v0/b/social-meme-c0164.appspot.com/o/karlniilo-20220113-0001.mp4?alt=media&token=7829ce99-0758-42e2-afa1-7d0cfe352a65", extraHeaders);


    }
}