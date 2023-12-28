package com.george.socialmeme.Activities.Account;

import static com.george.socialmeme.Helpers.AppHelper.isNightModeEnabled;

import com.george.socialmeme.Helpers.AppHelper;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.george.socialmeme.R;

public class AppearanceSettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);

        if (isNightModeEnabled(this)) {
            setTheme(R.style.AppTheme_Base_Night);
        }
        setContentView(R.layout.activity_appearance_settings);
    }
}
