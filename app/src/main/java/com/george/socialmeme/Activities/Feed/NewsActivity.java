package com.george.socialmeme.Activities.Feed;

import static com.george.socialmeme.Helpers.AppHelper.isNightModeEnabled;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import com.george.socialmeme.R;

import maes.tech.intentanim.CustomIntent;

public class NewsActivity extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        CustomIntent.customType(NewsActivity.this, "fadein-to-fadeout");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (isNightModeEnabled(NewsActivity.this)) {
            setTheme(R.style.AppTheme_Base_Night);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        Button button = findViewById(R.id.button6);
        button.setOnClickListener(v -> onBackPressed());

    }
}