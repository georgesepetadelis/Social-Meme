package com.george.socialmeme.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.george.socialmeme.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import maes.tech.intentanim.CustomIntent;

public class SettingsActivity extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        CustomIntent.customType(SettingsActivity.this, "right-to-left");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        DatabaseReference bugRef = FirebaseDatabase.getInstance().getReference("bugs");
        DatabaseReference feedbackRef = FirebaseDatabase.getInstance().getReference("feedback");

        ImageButton backBtn = findViewById(R.id.imageButton);
        TextView username = findViewById(R.id.textView49);
        CardView account_settings = findViewById(R.id.cardView2);
        Button logout = findViewById(R.id.button);
        CardView bugReport = findViewById(R.id.cardView3);
        CardView feedback = findViewById(R.id.cardView5);
        CardView privacyPolicy = findViewById(R.id.cardView6);

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
                feedbackRef.child(user.getUid()).child(feedback_et.getText().toString());
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
                bugRef.child(user.getUid()).child(bug_et.getText().toString());
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
                    .setPositiveButton("Yes", (dialogInterface, i) -> {
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