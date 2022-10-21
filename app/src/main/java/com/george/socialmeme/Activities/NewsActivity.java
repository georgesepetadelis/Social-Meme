package com.george.socialmeme.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        Button button = findViewById(R.id.button6);

        button.setOnClickListener(v -> onBackPressed());

    }
}