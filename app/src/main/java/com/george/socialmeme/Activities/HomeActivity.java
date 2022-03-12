package com.george.socialmeme.Activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.george.socialmeme.Fragments.HomeFragment;
import com.george.socialmeme.Fragments.MyProfileFragment;
import com.george.socialmeme.Fragments.NewPostFragment;
import com.george.socialmeme.Models.PostModel;
import com.george.socialmeme.R;
import com.george.socialmeme.Receivers.DailyNotificationReceiver;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    public static boolean anonymous;
    public static boolean showLoadingScreen;
    public static boolean showWhatsNewMessage;
    public static ChipNavigationBar bottomNavBar;

    boolean isNightModeEnabled() {
        SharedPreferences sharedPref = getSharedPreferences("dark_mode", MODE_PRIVATE);
        return sharedPref.getBoolean("dark_mode", false);
    }

    private void registerDailyNotification() {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 18);
        calendar.set(Calendar.MINUTE, 30);
        calendar.set(Calendar.SECOND, 0);

        Intent intent = new Intent(getApplicationContext(), DailyNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 100,
                intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendingIntent);

        SharedPreferences sharedPref = getSharedPreferences("daily_notification_registered", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("daily_notification_registered", true);
        editor.apply();

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

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.BLUE);

        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        bottomNavBar = findViewById(R.id.bottom_nav);

        // Check if daily notification broadcast receiver has been registered
        SharedPreferences sharedPref = getSharedPreferences("daily_notification_registered", MODE_PRIVATE);
        boolean notificationBroadcastRegistered = sharedPref.getBoolean("daily_notification_registered", false);

        if (!notificationBroadcastRegistered) {
            registerDailyNotification();
        }

        // Detect if system night mode is enabled
        // to auto enable in-app night mode
        SharedPreferences askForNightModeSharedPref = getSharedPreferences("asked_night_mode_enable", MODE_PRIVATE);
        SharedPreferences.Editor askForNightModeSharedPrefEditor = askForNightModeSharedPref.edit();
        boolean askForEnableNightMode = askForNightModeSharedPref.getBoolean("asked_night_mode_enable", false);
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES && !isNightModeEnabled() && !askForEnableNightMode) {
            // System dark mode is enabled
            // ask user to enable in-app night mode
            new AlertDialog.Builder(HomeActivity.this)
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
                        new AlertDialog.Builder(HomeActivity.this)
                                .setTitle("Nigh mode")
                                .setIcon(R.drawable.moon)
                                .setMessage("Remember that you can always enable night mode in Social Meme settings")
                                .setNegativeButton("Ok", (dialogInterface1, i1) -> dialogInterface1.dismiss())
                                .show();
                    }).show();
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
            final FirebaseUser user = mAuth.getCurrentUser();
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