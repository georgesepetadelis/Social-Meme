package com.george.socialmeme.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageButton;

import com.george.socialmeme.R;

import maes.tech.intentanim.CustomIntent;

public class PrivacyPolicyActivity extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        CustomIntent.customType(PrivacyPolicyActivity.this, "right-to-left");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        ImageButton backBtn = findViewById(R.id.imageButton4);

        backBtn.setOnClickListener(view -> onBackPressed());

    }
}