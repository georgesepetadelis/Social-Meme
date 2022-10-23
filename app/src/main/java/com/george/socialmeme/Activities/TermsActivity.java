package com.george.socialmeme.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.george.socialmeme.R;

import maes.tech.intentanim.CustomIntent;

public class TermsActivity extends AppCompatActivity {

    @Override
    public void onBackPressed() {
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