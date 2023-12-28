package com.george.socialmeme.Activities.Account;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;

import com.george.socialmeme.Activities.Feed.HomeActivity;
import com.george.socialmeme.Activities.Legal.PrivacyPolicyActivity;
import com.george.socialmeme.Activities.Common.SplashScreenActivity;
import com.george.socialmeme.Activities.Legal.TermsActivity;
import com.george.socialmeme.Activities.Auth.WelcomeActivity;
import com.george.socialmeme.BuildConfig;
import com.george.socialmeme.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import maes.tech.intentanim.CustomIntent;

public class SettingsActivity extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        CustomIntent.customType(SettingsActivity.this, "right-to-left");
    }

    boolean isNightModeEnabled() {
        SharedPreferences sharedPref = getSharedPreferences("dark_mode", MODE_PRIVATE);
        return sharedPref.getBoolean("dark_mode", false);
    }

    void updateNightModeState(boolean restartNow) {

        SharedPreferences sharedPref = getSharedPreferences("dark_mode", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putBoolean("dark_mode", !isNightModeEnabled());
        editor.apply();

        if (restartNow) {
            finish();
            Intent intent = new Intent(SettingsActivity.this, SplashScreenActivity.class);
            startActivity(intent);
        }

    }

    void openURL(String URL) {
        Uri uri = Uri.parse(URL);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (isNightModeEnabled()) {
            setTheme(R.style.AppTheme_Base_Night);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        AdView mAdView = findViewById(R.id.adView3);
        AdRequest adRequest = new AdRequest.Builder().build();

        if (HomeActivity.show_banners) {
            mAdView.loadAd(adRequest);
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        DatabaseReference bugRef = FirebaseDatabase.getInstance().getReference("bugs");
        DatabaseReference feedbackRef = FirebaseDatabase.getInstance().getReference("feedback");
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        ImageButton backBtn = findViewById(R.id.imageButton);
        TextView username = findViewById(R.id.textView49);
        TextView website_tv = findViewById(R.id.textView3);
        TextView version = findViewById(R.id.textView33);
        version.setText("App version: " + BuildConfig.VERSION_NAME);
        CardView account_settings = findViewById(R.id.cardView2);
        Button logout = findViewById(R.id.button);
        CardView bugReport = findViewById(R.id.cardView3);
        CardView feedback = findViewById(R.id.cardView5);
        CardView privacyPolicy = findViewById(R.id.cardView6);
        CardView donateButton = findViewById(R.id.cardView10);
        CardView terms = findViewById(R.id.cardView11);
        ImageButton instagram = findViewById(R.id.instagram_button);
        ImageButton github = findViewById(R.id.github_button);
        CardView appearanceSettings = findViewById(R.id.cardView9);
        //nightModeSwitch.setChecked(isNightModeEnabled());

        website_tv.setOnClickListener(view -> openURL("https://sepetadelhs.rf.gd"));
        donateButton.setOnClickListener(view -> openURL("https://PayPal.me/GSepetadelis"));
        instagram.setOnClickListener(view -> openURL("https://www.instagram.com/sepetadelhsss/"));
        github.setOnClickListener(view -> openURL("https://github.com/georgesepetadelis/Social-Meme"));

        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("show_donate").getValue(String.class).equals("disabled")) {
                    donateButton.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        appearanceSettings.setOnClickListener(view -> {

            startActivity(new Intent(SettingsActivity.this, AppearanceSettingsActivity.class));
            /* new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle("Restart required")
                    .setMessage("You need to restart Social Meme to apply new settings")
                    .setPositiveButton("Restart now", (dialogInterface, i) -> updateNightModeState(true))
                    .setNegativeButton("Later", (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        updateNightModeState(false);
                    })
                    .show();
        */
        });

        terms.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, TermsActivity.class));
            CustomIntent.customType(SettingsActivity.this, "left-to-right");
        });

        privacyPolicy.setOnClickListener(view -> {
            startActivity(new Intent(SettingsActivity.this, PrivacyPolicyActivity.class));
            CustomIntent.customType(SettingsActivity.this, "left-to-right");
        });

        feedback.setOnClickListener(view -> {
            AlertDialog.Builder dialog = new AlertDialog.Builder(SettingsActivity.this)
                    .setTitle("Feedback");
            final EditText feedback_et = new EditText(SettingsActivity.this);
            feedback_et.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
            feedback_et.setHint("Tell us your opinion");
            dialog.setView(feedback_et);
            dialog.setPositiveButton("Okay", (dialogInterface, i) -> {
                if (!feedback_et.getText().toString().isEmpty()) {
                    feedbackRef.child(user.getUid()).child(feedback_et.getText().toString());
                }
                dialogInterface.dismiss();
                Toast.makeText(SettingsActivity.this, "Feedback report succeed!", Toast.LENGTH_SHORT).show();
            }).setNegativeButton("Cancel", (dialogInterface, i) -> {
                dialogInterface.dismiss();
            }).show();

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(550, FrameLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(50, 6, 30, 6);
            feedback_et.setLayoutParams(params);
        });

        bugReport.setOnClickListener(view -> {
            AlertDialog.Builder dialog = new AlertDialog.Builder(SettingsActivity.this)
                    .setTitle("Bug report");
            final EditText bug_et = new EditText(SettingsActivity.this);
            bug_et.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
            bug_et.setHint("Please describe the issue");
            dialog.setView(bug_et);
            dialog.setPositiveButton("Okay", (dialogInterface, i) -> {
                if (!bug_et.getText().toString().isEmpty()) {
                    bugRef.child(user.getUid()).child(bug_et.getText().toString());
                }
                dialogInterface.dismiss();
                Toast.makeText(SettingsActivity.this, "Bug report succeed!", Toast.LENGTH_SHORT).show();
            }).setNegativeButton("Cancel", (dialogInterface, i) -> {
                dialogInterface.dismiss();
            }).show();

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(550, FrameLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(50, 6, 30, 6);
            bug_et.setLayoutParams(params);

        });

        logout.setOnClickListener(view -> {
            new AlertDialog.Builder(SettingsActivity.this)
                    .setTitle("Are you sure?")
                    .setMessage("Are you sure you want to log out?")
                    .setIcon(R.drawable.ic_report)
                    .setPositiveButton("Yes", (dialogInterface, i) -> {
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
                        userRef.child("fcm_token").removeValue();
                        auth.signOut();
                        finish();
                        startActivity(new Intent(SettingsActivity.this, WelcomeActivity.class));
                        CustomIntent.customType(SettingsActivity.this, "fadein-to-fadeout");
                    }).setNegativeButton("No", (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    }).show();
        });

        username.setText(user.getDisplayName());

        account_settings.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, AccountSettingsActivity.class));
            CustomIntent.customType(SettingsActivity.this, "left-to-right");
        });

        backBtn.setOnClickListener(v -> onBackPressed());

    }
}