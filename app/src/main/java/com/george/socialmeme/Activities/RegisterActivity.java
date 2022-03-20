package com.george.socialmeme.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.claudylab.smartdialogbox.SmartDialogBox;
import com.developer.kalert.KAlertDialog;
import com.george.socialmeme.R;
import com.github.loadingview.LoadingDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import maes.tech.intentanim.CustomIntent;

public class RegisterActivity extends AppCompatActivity {

    KAlertDialog progressDialog;

    void addUserToRealTimeDB(String username, String email) {

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        String userID = user.getUid();

        usersRef.child(userID).child("id").setValue(userID);
        usersRef.child(userID).child("name").setValue(username);
        usersRef.child(userID).child("email").setValue(email);
        usersRef.child(userID).child("profileImgUrl").setValue("none").addOnCompleteListener(task -> {
            progressDialog.hide();
            finish();
            startActivity(new Intent(RegisterActivity.this, SelectProfileImageActivity.class));
            CustomIntent.customType(RegisterActivity.this, "left-to-right");
        });

    }

    void singUp(String username, String email, String password) {
        progressDialog.show();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword(email.trim(), password).addOnSuccessListener(authResult -> {
            FirebaseUser user = mAuth.getCurrentUser();
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(username).build();
            user.updateProfile(profileUpdates);
            addUserToRealTimeDB(username, email.replace(".", ","));
        }).addOnFailureListener(e -> {
            progressDialog.hide();
            SmartDialogBox.showErrorDialog(RegisterActivity.this, e.getLocalizedMessage(), "OK");
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        CustomIntent.customType(RegisterActivity.this, "right-to-left");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        progressDialog = new KAlertDialog(RegisterActivity.this, KAlertDialog.PROGRESS_TYPE);
        progressDialog.getProgressHelper().setBarColor(R.color.main);
        progressDialog.setTitleText("Creating account...");
        progressDialog.setCancelable(false);

        final EditText username_et = findViewById(R.id.username_register);
        final EditText email_et = findViewById(R.id.email_register);
        final EditText password_et = findViewById(R.id.password_register);
        final EditText confirm_password_et = findViewById(R.id.confirm_password_register);
        final Button submit = findViewById(R.id.submit_register);
        final TextView singIn_btn = findViewById(R.id.sign_in_register);
        final TextView privacyPolicyTV = findViewById(R.id.textView7);

        privacyPolicyTV.setOnClickListener(view -> {
            startActivity(new Intent(RegisterActivity.this, PrivacyPolicyActivity.class));
            CustomIntent.customType(RegisterActivity.this, "left-to-right");
        });

        submit.setOnClickListener(v -> {
            // check if input is empty
            if (username_et.getText().toString().isEmpty() || email_et.getText().toString().isEmpty() || password_et.getText().toString().isEmpty() || confirm_password_et.getText().toString().isEmpty()) {
                SmartDialogBox.showErrorDialog(RegisterActivity.this, "All fields are required", "OK");
            } else {
                // check if confirm password matches password
                if (!confirm_password_et.getText().toString().equals(password_et.getText().toString())) {
                    SmartDialogBox.showErrorDialog(RegisterActivity.this, "Passwords does not match.", "OK");
                } else {
                    singUp(username_et.getText().toString(), email_et.getText().toString(), password_et.getText().toString());
                }

            }

        });

        // Open login activity when TextView clicked
        singIn_btn.setOnClickListener(v -> {
            finish();
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            CustomIntent.customType(RegisterActivity.this, "fadein-to-fadeout");
        });

    }
}