package com.george.socialmeme.Activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.george.socialmeme.BuildConfig;
import com.george.socialmeme.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

    boolean isNightModeEnabled() {
        SharedPreferences sharedPref = getSharedPreferences("dark_mode", MODE_PRIVATE);
        return sharedPref.getBoolean("dark_mode", false);
    }

    void userHasTheLatestVersion() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (isNightModeEnabled()) {
            Resources.Theme theme = super.getTheme();
            theme.applyStyle(R.style.AppTheme_Base_Night, true);
            // Update status bar color
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.GRAY);
        }else {
            // Update status bar color
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.WHITE);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {

            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser user = auth.getCurrentUser();

            if (checkInternetConnection()) {
                if (user == null) {
                    startActivity(new Intent(SplashScreenActivity.this, WelcomeActivity.class));
                } else {
                    // Check if user has the latest version
                    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                    rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            int appVersionCode = BuildConfig.VERSION_CODE;
                            int latestAppVersion = Integer.parseInt(snapshot.child("latest_version_code").getValue(String.class));
                            if (appVersionCode < latestAppVersion) {
                                new AlertDialog.Builder(SplashScreenActivity.this)
                                        .setTitle("Update required.")
                                        .setCancelable(false)
                                        .setMessage("For security reasons having the latest version is required to use Social Meme")
                                        .setPositiveButton("Update", (dialogInterface, i) -> {
                                            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.george.socialmeme");
                                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                            startActivity(intent);
                                        }).show();
                            }else {
                                finish();
                                initializeNightModeSharedPref();
                                HomeActivity.showLoadingScreen = true;
                                startActivity(new Intent(SplashScreenActivity.this, HomeActivity.class));
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(SplashScreenActivity.this, "Error checking for latest version: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                CustomIntent.customType(SplashScreenActivity.this, "fadein-to-fadeout");

            }else {
                new AlertDialog.Builder(SplashScreenActivity.this)
                        .setTitle("Whoops!")
                        .setMessage("No internet connection. Please check your internet connection and try again")
                        .setCancelable(false)
                        .setPositiveButton("Retry", (dialog, which) -> {
                            // Restart activity
                            Intent intent = getIntent();
                            finish();
                            startActivity(intent);
                            CustomIntent.customType(SplashScreenActivity.this, "fadein-to-fadeout");
                        }).show();
            }
        }, 1000);
    }
}