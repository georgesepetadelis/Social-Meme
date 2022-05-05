package com.george.socialmeme.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.george.socialmeme.R;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.potyvideo.library.AndExoPlayerView;

import maes.tech.intentanim.CustomIntent;

public class FullScreenVideoActivity extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        CustomIntent.customType(FullScreenVideoActivity.this, "right-to-left");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_video);

        Bundle extras = getIntent().getExtras();
        String videoURL = extras.getString("video_url");

        ImageButton backBtn = findViewById(R.id.imageButton21);
        backBtn.setOnClickListener(view -> onBackPressed());

        StyledPlayerView exoPlayerView = findViewById(R.id.full_screen_player);
        ExoPlayer player = new ExoPlayer.Builder(FullScreenVideoActivity.this).build();
        MediaItem mediaItem = MediaItem.fromUri(videoURL);
        player.setMediaItem(mediaItem);
        player.prepare();
        exoPlayerView.setPlayer(player);

        player.addAnalyticsListener(new AnalyticsListener() {
            @Override
            public void onPlayerError(@NonNull EventTime eventTime, @NonNull PlaybackException error) {
                Toast.makeText(FullScreenVideoActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.i("EXOPLAYER_ERROR", "" + error.getMessage());
            }
        });

    }
}