package com.george.socialmeme.Activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.george.socialmeme.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import maes.tech.intentanim.CustomIntent;

@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity extends AppCompatActivity {

    public boolean isInternetConnectionAvailable() {
        ConnectivityManager cm = (ConnectivityManager) SplashScreenActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
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

        // Animate text
        TextView appLogo = findViewById(R.id.textView16);
        YoYo.with(Techniques.FadeIn).duration(1200).repeat(0).playOn(appLogo);

        new Handler().postDelayed(() -> {

            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser user = auth.getCurrentUser();

            if (isInternetConnectionAvailable()) {

                // Load ad banner and wait until ad is loaded
                AdView mAdView = findViewById(R.id.splash_Ad);
                AdRequest adRequest = new AdRequest.Builder().build();
                mAdView.loadAd(adRequest);

                mAdView.setAdListener(new AdListener() {

                    @Override
                    public void onAdImpression() {
                        super.onAdImpression();
                    }

                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        if (user == null) {
                            startActivity(new Intent(SplashScreenActivity.this, WelcomeActivity.class));
                            CustomIntent.customType(SplashScreenActivity.this, "fadein-to-fadeout");
                        } else {
                            initializeNightModeSharedPref();
                            HomeActivity.showLoadingScreen = true;
                            startActivity(new Intent(SplashScreenActivity.this, HomeActivity.class));
                            CustomIntent.customType(SplashScreenActivity.this, "fadein-to-fadeout");
                            finish();
                        }
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        //Toast.makeText(SplashScreenActivity.this, "Failed to load Ads", Toast.LENGTH_SHORT).show();
                        //Toast.makeText(SplashScreenActivity.this, loadAdError.getMessage(), Toast.LENGTH_SHORT).show();

                        if (user == null) {
                            startActivity(new Intent(SplashScreenActivity.this, WelcomeActivity.class));
                            CustomIntent.customType(SplashScreenActivity.this, "fadein-to-fadeout");
                        } else {

                            if (user.getDisplayName() == null || user.getDisplayName().isEmpty()) {
                                startActivity(new Intent(SplashScreenActivity.this, WelcomeActivity.class));
                                CustomIntent.customType(SplashScreenActivity.this, "fadein-to-fadeout");
                            }

                            initializeNightModeSharedPref();
                            HomeActivity.showLoadingScreen = true;
                            startActivity(new Intent(SplashScreenActivity.this, HomeActivity.class));
                            CustomIntent.customType(SplashScreenActivity.this, "fadein-to-fadeout");
                            finish();
                        }
                    }

                });
            }else {
                startActivity(new Intent(SplashScreenActivity.this, NoInternetConnectionActivity.class));
                CustomIntent.customType(SplashScreenActivity.this, "fadein-to-fadeout");
            }
        }, 800);
    }
}