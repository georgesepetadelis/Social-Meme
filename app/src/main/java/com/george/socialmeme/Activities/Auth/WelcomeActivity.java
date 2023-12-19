package com.george.socialmeme.Activities.Auth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.george.socialmeme.Activities.Feed.HomeActivity;
import com.george.socialmeme.R;

import maes.tech.intentanim.CustomIntent;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        HomeActivity.singedInAnonymously = false;
        finish();
        CustomIntent.customType(WelcomeActivity.this, "right-to-left");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Button login = findViewById(R.id.button2);
        Button login_anonymously = findViewById(R.id.button3);

        login_anonymously.setOnClickListener(view -> {
            HomeActivity.singedInAnonymously = true;
            startActivity(new Intent(WelcomeActivity.this, HomeActivity.class));
            CustomIntent.customType(WelcomeActivity.this, "fadein-to-fadeout");
        });

        login.setOnClickListener(view -> {
            startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
            CustomIntent.customType(WelcomeActivity.this, "left-to-right");
        });

    }
}