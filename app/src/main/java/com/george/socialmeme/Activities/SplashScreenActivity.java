package com.george.socialmeme.Activities;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {

            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser user = auth.getCurrentUser();

            if (checkInternetConnection()) {
                finish();

                if (user == null) {
                    startActivity(new Intent(SplashScreenActivity.this, WelcomeActivity.class));
                }else {
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