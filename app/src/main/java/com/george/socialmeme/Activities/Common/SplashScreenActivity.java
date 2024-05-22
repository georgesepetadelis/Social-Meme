package com.george.socialmeme.Activities.Common;

import static com.george.socialmeme.Helpers.AppHelper.isNightModeEnabled;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
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
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.george.socialmeme.Activities.Auth.WelcomeActivity;
import com.george.socialmeme.Activities.Feed.HomeActivity;
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

    private boolean startedURL = false;
    private boolean isUserDisabled = false;

    public DatabaseReference mDatabaseReference;
    @Override
    protected void onResume() {
        super.onResume();
        if (startedURL) {
            startActivity(new Intent(this, SplashScreenActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeUI();
        checkUserStatus();
        delayedAction();
    }

    public void showUnavailableServiceDialog(){


    }
    private void initializeUI() {
        if (isNightModeEnabled(this)) {
            applyNightMode();
        } else {
            updateStatusBarColor();
        }

        setContentView(R.layout.activity_splash);

        TextView appLogo = findViewById(R.id.textView16);
        YoYo.with(Techniques.FadeIn).duration(1200).repeat(0).playOn(appLogo);
    }

    private void applyNightMode() {
        Resources.Theme theme = super.getTheme();
        theme.applyStyle(R.style.AppTheme_Base_Night, true);
        updateStatusBarColor();
    }

    private void updateStatusBarColor() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.BLACK);
    }

    private void checkUserStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.reload().addOnFailureListener(e -> {
                if (e.getMessage().contains("disabled")) {
                    showDisabledUserDialog();
                }
            });
        }
    }

    private void showDisabledUserDialog() {
        isUserDisabled = true;
        new AlertDialog.Builder(this)
                .setTitle("It's panic time!")
                .setMessage("Your account is disabled for violating Terms Of Service!\n\nPlease go and host a party for it, just remember to invite us!")
                .setCancelable(false)
                .setPositiveButton("OK", (dialogInterface, i) -> {
                    startActivity(new Intent(this, WelcomeActivity.class));
                })
                .show();
    }

    private void delayedAction() {
        new Handler().postDelayed(() -> {
            if (isInternetConnectionAvailable()) {
                handleInternetConnection();
            } else {
                startActivity(new Intent(this, NoInternetConnectionActivity.class));
                CustomIntent.customType(this, "fadein-to-fadeout");
            }
        }, 2200);
    }

    private boolean IsSMAvailable(){
        mDatabaseReference.child("active_data").child("serviceActive").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(boolean.class) == false){
                    new AlertDialog.Builder(SplashScreenActivity.this)
                            .setTitle("Social Meme is not available - Check for Updates.")
                            .setMessage("Social Meme is not available at the moment. \n\nPlease check for any available updates and try again later.")
                            .setCancelable(false)
                            .setPositiveButton("OK", (dialogInterface, i) -> {
                                Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.george.socialmeme");
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                startActivity(intent);
                                finish();
                            }).show();

                }
                else if (dataSnapshot.getValue(boolean.class) == true){
                    initializeNightModeSharedPref();
                    navigateToHomeActivity();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
        return true;
    }


    private boolean isInternetConnectionAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void handleInternetConnection() {
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        IsSMAvailable();
    }

    private void initializeNightModeSharedPref() {
        SharedPreferences sharedPref = getSharedPreferences("dark_mode", Context.MODE_PRIVATE);
        if (!sharedPref.contains("dark_mode")) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("dark_mode", false);
            editor.apply();
        }
    }

    private void navigateToHomeActivity() {
        HomeActivity.showLoadingScreen = true;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Intent intent = new Intent(this, HomeActivity.class);
            if (getIntent().getExtras() != null) {
                Bundle extras = getIntent().getExtras();
                String notificationURL = extras.getString("url");

                if (notificationURL != null) {
                    Uri uri = Uri.parse(notificationURL);
                    Intent intent1 = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent1);
                    startedURL = true;
                } else {

                    Intent intent2 = getIntent();
                    String action = intent2.getAction();
                    String type = intent2.getType();

                    if (Intent.ACTION_SEND.equals(action) && type != null) {
                        if (type.startsWith("video/")) {
                            HomeActivity.IncomingPostType = "video";
                            HomeActivity.UploadNewPost = true;
                            HomeActivity.fileUri = intent2.getParcelableExtra(Intent.EXTRA_STREAM);
                        } else if (type.startsWith("image/")) {
                            HomeActivity.IncomingPostType = "image";
                            HomeActivity.UploadNewPost = true;
                            HomeActivity.fileUri = intent2.getParcelableExtra(Intent.EXTRA_STREAM);
                        }
                    }

                    startActivity(intent);
                    CustomIntent.customType(SplashScreenActivity.this, "fadein-to-fadeout");
                    finish();
                }
            } else {
                startActivity(intent);
                CustomIntent.customType(SplashScreenActivity.this, "fadein-to-fadeout");
                finish();
            }

            startActivity(intent);
            CustomIntent.customType(this, "fadein-to-fadeout");
            finish();
        } else {
            Intent intent = new Intent(this, WelcomeActivity.class);
            startActivity(intent);
        }
    }
}
