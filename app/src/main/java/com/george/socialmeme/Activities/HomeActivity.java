package com.george.socialmeme.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.george.socialmeme.Fragments.HomeFragment;
import com.george.socialmeme.Fragments.MyProfileFragment;
import com.george.socialmeme.Fragments.NewPostFragment;
import com.george.socialmeme.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

public class HomeActivity extends AppCompatActivity {

    public static boolean anonymous;
    public static boolean showLoadingScreen;
    public static ChipNavigationBar bottomNavBar;

    boolean isNightModeEnabled() {
        SharedPreferences sharedPref = getSharedPreferences("dark_mode", MODE_PRIVATE);
        return sharedPref.getBoolean("dark_mode", false);
    }

    void enableNightMode() {
        SharedPreferences sharedPref = getSharedPreferences("dark_mode", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("dark_mode", true);
        editor.apply();
        Toast.makeText(this, "Night mode enabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (isNightModeEnabled()) {
            // Using force apply because HomeActivity contains fragments
            Resources.Theme theme = super.getTheme();
            theme.applyStyle(R.style.AppTheme_Base_Night, true);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Update token on real-time DB
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        root.child("latest_version_code").setValue("13");

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.BLUE);

        bottomNavBar = findViewById(R.id.bottom_nav);

        // Detect if system night mode is enabled
        // to auto enable in-app night mode
        SharedPreferences askForNightModeSharedPref = getSharedPreferences("asked_night_mode_enable", MODE_PRIVATE);
        SharedPreferences.Editor askForNightModeSharedPrefEditor = askForNightModeSharedPref.edit();
        boolean askForEnableNightMode = askForNightModeSharedPref.getBoolean("asked_night_mode_enable", false);
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES && !isNightModeEnabled() && !askForEnableNightMode) {

            // System dark mode is enabled
            // ask user to enable in-app night mode
            AlertDialog alertDialog = new AlertDialog.Builder(HomeActivity.this)
                    .setCancelable(false)
                    .setTitle("Enable app night mode?")
                    .setIcon(R.drawable.moon)
                    .setMessage("Social Meme detected that you have enabled night mode on your device. " +
                            "You want to enable night mode in Social Meme too?")
                    .setPositiveButton("Yes", (dialogInterface, i) -> {
                        enableNightMode();
                        finish();
                        startActivity(new Intent(HomeActivity.this, SplashScreenActivity.class));
                    })
                    .setNegativeButton("No, thanks", (dialogInterface, i) -> {
                        askForNightModeSharedPrefEditor.putBoolean("asked_night_mode_enable", true);
                        askForNightModeSharedPrefEditor.apply();
                        AlertDialog reminderDialog = new AlertDialog.Builder(HomeActivity.this)
                                .setTitle("Nigh mode")
                                .setIcon(R.drawable.moon)
                                .setMessage("Remember that you can always enable night mode in Social Meme settings")
                                .setNegativeButton("Ok", (dialogInterface1, i1) -> dialogInterface1.dismiss())
                                .create();

                        reminderDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        reminderDialog.getWindow().setBackgroundDrawableResource(R.drawable.custom_dialog_background);
                        reminderDialog.show();

                    }).create();

            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.custom_dialog_background);
            alertDialog.show();

        }

        // Set fcm token if not exists
        // inside real-time DB to be able
        // to send push notifications from back-end
        if (!HomeActivity.anonymous) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(user.getUid());
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Check if token exists
                    if (!snapshot.child("fcm_token").exists()) {
                        // Token does not exists
                        // add token to real-time DB
                        FirebaseMessaging.getInstance().getToken()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        userRef.child("fcm_token").setValue(task.getResult());
                                    }else {
                                        Toast.makeText(HomeActivity.this, "Unable to set user token", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(HomeActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        bottomNavBar.setOnItemSelectedListener(id -> {
            Fragment selectedFragment = new HomeFragment();

            switch (id) {
                case R.id.home_fragment:
                    selectedFragment = new HomeFragment();
                    break;
                case R.id.new_post_fragment:
                    selectedFragment = new NewPostFragment();
                    break;
                case R.id.my_profile_fragment:
                    selectedFragment = new MyProfileFragment();
                    break;
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();

        });

        // Load default fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        bottomNavBar.setItemSelected(R.id.home_fragment, true);

        if (!anonymous) {
            user.reload().addOnFailureListener(e -> new AlertDialog.Builder(HomeActivity.this)
                    .setTitle("Error")
                    .setMessage(e.getMessage())
                    .setCancelable(false)
                    .setPositiveButton("Ok", (dialog, which) -> {
                        finish();
                        startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                    }).show());
        }
    }
}