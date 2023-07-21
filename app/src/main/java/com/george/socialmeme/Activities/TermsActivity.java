package com.george.socialmeme.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.george.socialmeme.R;

import maes.tech.intentanim.CustomIntent;

public class TermsActivity extends AppCompatActivity {

    boolean isNightModeEnabled() {
        SharedPreferences sharedPref = getSharedPreferences("dark_mode", MODE_PRIVATE);
        return sharedPref.getBoolean("dark_mode", false);
    }

    @Override
    public void onBackPressed() {

        if (isNightModeEnabled()) {
            setTheme(R.style.AppTheme_Base_Night);
        }

        super.onBackPressed();
        finish();
        CustomIntent.customType(TermsActivity.this, "right-to-left");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);
    }
}