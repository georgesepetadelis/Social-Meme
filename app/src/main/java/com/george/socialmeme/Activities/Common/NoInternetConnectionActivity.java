package com.george.socialmeme.Activities.Common;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Button;

import com.george.socialmeme.Activities.Auth.WelcomeActivity;
import com.george.socialmeme.Activities.Feed.HomeActivity;
import com.george.socialmeme.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import maes.tech.intentanim.CustomIntent;

public class NoInternetConnectionActivity extends AppCompatActivity {

    boolean isInternetConnectionAvailable() {
        ConnectivityManager cm = (ConnectivityManager) NoInternetConnectionActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_internet_connection);

        Button tryAgainButton = findViewById(R.id.button4);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        tryAgainButton.setOnClickListener(view -> {
            tryAgainButton.setEnabled(false);
            tryAgainButton.setText("Trying again...");
            if (isInternetConnectionAvailable()) {
                if (user == null) {
                    startActivity(new Intent(NoInternetConnectionActivity.this, WelcomeActivity.class));
                } else {
                    HomeActivity.showLoadingScreen = true;
                    startActivity(new Intent(NoInternetConnectionActivity.this, HomeActivity.class));
                    CustomIntent.customType(NoInternetConnectionActivity.this, "fadein-to-fadeout");
                    finish();
                }
            }
            tryAgainButton.setEnabled(true);
            tryAgainButton.setText("Try again");
        });

    }
}