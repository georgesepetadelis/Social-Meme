package com.george.socialmeme.Activities.Profile;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import com.george.socialmeme.R;
import com.george.socialmeme.ViewHolders.VideoItemViewHolder;


import maes.tech.intentanim.CustomIntent;

public class FullScreenVideoActivity extends AppCompatActivity {

    ExoPlayer player;
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        CustomIntent.customType(FullScreenVideoActivity.this, "right-to-left");
        player.release();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_full_screen_video);

        PlayerView exoPlayerView = findViewById(R.id.full_screen_player);
        Bundle extras = getIntent().getExtras();
        String videoURL = extras.getString("video_url");


        player = new ExoPlayer.Builder(FullScreenVideoActivity.this).build();
        MediaItem mediaItem = MediaItem.fromUri(videoURL);
        player.setMediaItem(mediaItem);
        player.prepare();
        exoPlayerView.setPlayer(player);
        player.play();

    }
}
