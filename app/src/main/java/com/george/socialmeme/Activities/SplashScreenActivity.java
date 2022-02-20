package com.george.socialmeme.Activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.george.socialmeme.BuildConfig;
import com.george.socialmeme.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import maes.tech.intentanim.CustomIntent;

@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity extends AppCompatActivity {



    public boolean checkInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) SplashScreenActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    void saveCurrentAppVersionToSharedPrefs() {
        SharedPreferences sharedPref = getSharedPreferences("current_app_version", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        String versionName = BuildConfig.VERSION_NAME;
        editor.putString("current_app_version", versionName);
    }

    void initializeNightModeSharedPref() {
        SharedPreferences sharedPref = getSharedPreferences("dark_mode", Context.MODE_PRIVATE);
        if (!sharedPref.contains("dark_mode")) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("dark_mode", false);
            editor.apply();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.WHITE);

        new Handler().postDelayed(() -> {

            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser user = auth.getCurrentUser();

            if (checkInternetConnection()) {
                finish();

                if (user == null) {
                    startActivity(new Intent(SplashScreenActivity.this, WelcomeActivity.class));
                }else {
                    initializeNightModeSharedPref();
                    SharedPreferences sharedPref = getSharedPreferences("current_app_version", Context.MODE_PRIVATE);
                    if (!sharedPref.getString("current_app_version", "2.0.6").equals(BuildConfig.VERSION_NAME)) {
                        saveCurrentAppVersionToSharedPrefs();
                        HomeActivity.showWhatsNewMessage = true;
                    }

                    HomeActivity.showLoadingScreen = true;
                    startActivity(new Intent(SplashScreenActivity.this, HomeActivity.class));
                }
                CustomIntent.customType(SplashScreenActivity.this, "fadein-to-fadeout");

            }else {
                new AlertDialog.Builder(SplashScreenActivity.this)
                        .setTitle("Whoops!")
                        .setMessage("No internet connection. Please check your internet connection and try again")
                        .setCancelable(false)
                        .setPositiveButton("Retry", (dialog, which) -> {
                            // restart current activity
                            Intent intent = getIntent();
                            finish();
                            startActivity(intent);
                            CustomIntent.customType(SplashScreenActivity.this, "fadein-to-fadeout");
                        }).show();
            }

        }, 1000);
    }
}