package com.george.socialmeme.Activities.Feed;

import static com.george.socialmeme.Helpers.AppHelper.isNightModeEnabled;
import static com.george.socialmeme.Helpers.AppHelper.updateNightModeState;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.george.socialmeme.Activities.Auth.LoginActivity;
import com.george.socialmeme.Activities.Common.SplashScreenActivity;
import com.george.socialmeme.Fragments.HomeFragment;
import com.george.socialmeme.Fragments.MyProfileFragment;
import com.george.socialmeme.Fragments.NewPostFragment;
import com.george.socialmeme.Helpers.AppHelper;
import com.george.socialmeme.Helpers.SaverHelper;
import com.george.socialmeme.Models.PostModel;
import com.george.socialmeme.Models.UserModel;
import com.george.socialmeme.R;
import com.george.socialmeme.Services.UpdateService;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import maes.tech.intentanim.CustomIntent;

public class HomeActivity extends AppCompatActivity {

    public Activity activity; // Non-static activity to avoid StaticFieldLeak
    public static boolean signedInAnonymously = false;
    public static boolean showLoadingScreen;
    public static boolean appStarted;
    public static boolean watched_ad;
    public static boolean show_banners;
    public static ChipNavigationBar bottomNavBar;
    public static ArrayList<PostModel> savedPostsArrayList, noSuffledPostsList;
    public static ArrayList<UserModel> savedUserProfiles = null;
    public static UserModel savedUserData = null;
    public static ExtendedFloatingActionButton filtersBtn;

    public static boolean openNotification;
    public static String notiUserId;
    public static String notiPostId;
    public static String notiUsername;
    public static boolean userHasPosts = false;
    private ConsentInformation consentInformation;
    public static boolean UploadNewPost = false;
    public static String IncomingPostType;
    public static Uri fileUri;
    public static Fragment savedHomeFragmentInstance;
    public static int lastHomePosition = -1;
    public static ExtendedFloatingActionButton goUp;
    public static ArrayList<PostModel> postListToRestore, allPostsSaved;

    public static boolean isInstagramInstalled(PackageManager packageManager) {
        try {
            packageManager.getPackageInfo("com.instagram.android", 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static String prettyCount(Number number) {
        char[] suffix = {' ', 'k', 'M', 'B', 'T', 'P', 'E'};
        long numValue = number.longValue();
        int value = (int) Math.floor(Math.log10(numValue));
        int base = value / 3;
        if (value >= 3 && base < suffix.length) {
            return new DecimalFormat("#0.0").format(numValue / Math.pow(10, base * 3)) + suffix[base];
        } else {
            return new DecimalFormat("#,##0").format(numValue);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //onDestroy();
        //finish();
    }

    void followAppCreator() {

        String creatorUserId = "cFNlK7QLLjZgc7SQDp79PwESxbB2";
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (!user.getUid().equals(creatorUserId)) {
            // Add to logged-in user to followers list
            DatabaseReference creatorRef = FirebaseDatabase.getInstance().getReference("users").child(creatorUserId);
            creatorRef.child("followers").child(user.getUid()).setValue(user.getUid());

            // Add creator to logged-in user following list
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
            userRef.child("following").child(creatorUserId).setValue(creatorUserId);

            // Add notification to firestore
            creatorRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    String token = snapshot.child("fcm_token").getValue(String.class);

                    @NonNull String firestoreNotificationID = userRef.push().getKey();
                    if (firestoreNotificationID == null){ return; }
                    Map<String, Object> notification = new HashMap<>();
                    notification.put("token", token);
                    notification.put("userID", user.getUid());
                    notification.put("not_type", "follow");
                    notification.put("title", "New follower");
                    notification.put("message", user.getDisplayName() + " started following you");
                    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                    firestore.collection("notifications")
                            .document(firestoreNotificationID).set(notification);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

    }

    boolean newFeaturesViewed() {
        SharedPreferences sharedPref = getSharedPreferences("v2.6.6.6", MODE_PRIVATE);
        return sharedPref.getBoolean("v2.6.6.6", false);
    }

    void enableNightMode() {
        SharedPreferences sharedPref = getSharedPreferences("dark_mode", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("dark_mode", true);
        editor.apply();
        Toast.makeText(this, "Night mode enabled", Toast.LENGTH_SHORT).show();
    }

    boolean followedCreator() {
        SharedPreferences sharedPref = getSharedPreferences("follow_creator", Context.MODE_PRIVATE);
        return sharedPref.getBoolean("follow_creator", false);
    }

    public interface FirebaseCallback {
        void onCallback(boolean show);
    }

    FirebaseCallback firebaseCallback = show -> {

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (getSupportFragmentManager() != null) {
            show_banners = show;

            bottomNavBar.setOnItemSelectedListener(id -> {

                Fragment selectedFragment = new HomeFragment();

                switch (id) {
                    case R.id.home_fragment:
                        selectedFragment = new HomeFragment();
                        filtersBtn.setVisibility(View.VISIBLE);
                        goUp.setVisibility(View.VISIBLE);
                        break;
                    case R.id.new_post_fragment:
                        selectedFragment = new NewPostFragment();
                        filtersBtn.setVisibility(View.INVISIBLE);
                        goUp.setVisibility(View.INVISIBLE);
                        break;
                    case R.id.my_profile_fragment:
                        selectedFragment = new MyProfileFragment();
                        filtersBtn.setVisibility(View.INVISIBLE);
                        goUp.setVisibility(View.INVISIBLE);
                        break;
                }

                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                        .replace(R.id.fragment_container, selectedFragment)
                        .commitAllowingStateLoss();

            });

            // Load default fragment
            bottomNavBar.setItemSelected(R.id.home_fragment, true);

        }

        if (!signedInAnonymously && user != null) {
            user.reload().addOnFailureListener(e -> new AlertDialog.Builder(HomeActivity.this)
                    .setTitle("Error")
                    .setMessage(e.getMessage())
                    .setCancelable(false)
                    .setPositiveButton("Ok", (dialog, which) -> {
                        finish();
                        startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                    }).show());

            if (!followedCreator()) {
                followAppCreator();
                SharedPreferences sharedPref = getSharedPreferences("follow_creator", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("follow_creator", true);
                editor.apply();
            }

        }

        bottomNavBar.setVisibility(View.VISIBLE);
        filtersBtn.setVisibility(View.VISIBLE);

    };

    public static void showUpdateDialog() {
        if (filtersBtn.getContext() != null) {

            // Display update dialog from update service
            new android.app.AlertDialog.Builder(filtersBtn.getContext())
                    .setTitle("A new update just released!")
                    .setCancelable(false)
                    .setMessage("For security reasons having the latest version is required to use Social Meme")
                    .setPositiveButton("Update", (dialogInterface, i) -> {
                        Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.george.socialmeme");
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        filtersBtn.getContext().startActivity(intent);
                    }).show();
            Toast.makeText(filtersBtn.getContext(), "New update available", Toast.LENGTH_SHORT).show();
        }
    }
    public static void openURL(String url) {
        try {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            filtersBtn.getContext().startActivity(intent);
        } catch (Exception e) {
            //Toast.makeText(filtersBtn.getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void loadForm() {
        // Loads a consent form. Must be called on the main thread.
        UserMessagingPlatform.loadConsentForm(
                this,
                consentForm -> {
                    if (consentInformation.getConsentStatus() == ConsentInformation.ConsentStatus.REQUIRED) {
                        consentForm.show(
                                HomeActivity.this,
                                formError -> {
                                    if (consentInformation.getConsentStatus() == ConsentInformation.ConsentStatus.OBTAINED) {
                                        // App can start requesting ads.
                                    }

                                    // Handle dismissal by reloading form.
                                    loadForm();
                                });
                    }
                },
                formError -> {
                    //Toast.makeText(this, "Error(ad form): " + formError.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (isNightModeEnabled(HomeActivity.this)) {
            // Using force apply because HomeActivity contains fragments
            Resources.Theme theme = super.getTheme();
            theme.applyStyle(R.style.AppTheme_Base_Night, true);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        allPostsSaved = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.READ_MEDIA_VIDEO)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.READ_MEDIA_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.POST_NOTIFICATIONS,
                                Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_AUDIO,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        201);
            }
        }

        // Decide if we show AD banners based on DB variable
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("show_banners").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean showAdBanner = snapshot.getValue(Boolean.class);
                firebaseCallback.onCallback(showAdBanner);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        if (!newFeaturesViewed()) {
            SharedPreferences sharedPref = getSharedPreferences("v2.6.6.6", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("v2.6.6.6", true);
            editor.apply();
            startActivity(new Intent(HomeActivity.this, NewsActivity.class));
            CustomIntent.customType(HomeActivity.this, "fadein-to-fadeout");
        }

        goUp = findViewById(R.id.go_up);
        goUp.setOnClickListener(v -> {
            RecyclerView homeRecyclerView = HomeFragment.recyclerView;
            if (homeRecyclerView != null) {
                int totalItems = homeRecyclerView.getAdapter().getItemCount();
                homeRecyclerView.smoothScrollToPosition(totalItems);
            }
        });

        Bundle extras = getIntent().getExtras();

        if (extras != null) {

            openNotification = true;

            if (extras.getString("user_id") != null) {
                notiUserId = extras.getString("user_id");
            }

            if (extras.getString("post_id") != null) {
                notiPostId = extras.getString("post_id");
            }

            if (extras.getString("username") != null) {
                notiUsername = extras.getString("username");
            }

        }

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.BLUE);

        // Start update service
        Intent updateServiceIntent = new Intent(this, UpdateService.class);
        startService(updateServiceIntent);

        if (!signedInAnonymously) {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser user = auth.getCurrentUser();

            if (user != null && user.getPhotoUrl() != null) {
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference();
                userRef.child("users").child(user.getUid()).child("photo_url").setValue(user.getPhotoUrl().toString());
            }

        }

        if (signedInAnonymously) {
            String loginKey = ref.push().getKey();
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            ref.child("anonymous").child(loginKey)
                    .child("datetime").setValue(now.toString());
        }

        bottomNavBar = findViewById(R.id.bottom_nav);
        filtersBtn = findViewById(R.id.filters_btn);
        savedPostsArrayList = new ArrayList<>();
        savedUserProfiles = new ArrayList<>();
        noSuffledPostsList = new ArrayList<>();

        // Hide navBar and filterBtn while loading
        bottomNavBar.setVisibility(View.INVISIBLE);
        filtersBtn.setVisibility(View.INVISIBLE);

        // Detect if system night mode is enabled
        // to auto enable in-app night mode
        SharedPreferences askForNightModeSharedPref = getSharedPreferences("asked_night_mode_enable", MODE_PRIVATE);
        SharedPreferences.Editor askForNightModeSharedPrefEditor = askForNightModeSharedPref.edit();
        boolean askForEnableNightMode = askForNightModeSharedPref.getBoolean("asked_night_mode_enable", false);
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        ConsentRequestParameters params = new ConsentRequestParameters
                .Builder()
                .setTagForUnderAgeOfConsent(false)
                .build();

        consentInformation = UserMessagingPlatform.getConsentInformation(this);
        consentInformation.requestConsentInfoUpdate(
                this,
                params,
                () -> {
                    if (consentInformation.isConsentFormAvailable()) {
                        loadForm();
                    }
                },
                formError -> Toast.makeText(HomeActivity.this, "Error: " + formError.getMessage(), Toast.LENGTH_SHORT).show());

        SaverHelper saverHelper = new SaverHelper(HomeActivity.this, "theme_mode");
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES
                && saverHelper.getSaverValue("theme_mode", "none").equals("none")) {
            updateNightModeState(true, HomeActivity.this);
            saverHelper.setSaverValue("theme_mode", "System theme");
            // restart activity in order the theme to be applied
            finish();
            startActivity(new Intent(HomeActivity.this, SplashScreenActivity.class));

        }

    }
}