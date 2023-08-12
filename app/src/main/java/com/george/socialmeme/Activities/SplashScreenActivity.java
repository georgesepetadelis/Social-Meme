package com.george.socialmeme.Activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
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

import org.w3c.dom.Text;

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
            window.setStatusBarColor(Color.BLACK);
        } else {
            // Update status bar color
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);
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
                    Intent intent = new Intent(SplashScreenActivity.this, HomeActivity.class);

                    if (getIntent().getExtras() != null) {
                        Bundle extras = getIntent().getExtras();
                        intent.putExtra("user_id", extras.getString("userID"));
                        intent.putExtra("post_id", extras.getString("postID"));
                    }

                    startActivity(intent);
                    CustomIntent.customType(SplashScreenActivity.this, "fadein-to-fadeout");
                    finish();

                    HomeActivity.openNotification = true;

                }
            } else {
                startActivity(new Intent(SplashScreenActivity.this, NoInternetConnectionActivity.class));
                CustomIntent.customType(SplashScreenActivity.this, "fadein-to-fadeout");
            }
        }, 800);
    }
}