package com.george.socialmeme.Activities;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.claudylab.smartdialogbox.SmartDialogBox;
import com.george.socialmeme.R;
import com.github.loadingview.LoadingDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import maes.tech.intentanim.CustomIntent;

public class AccountSettingsActivity extends AppCompatActivity {

    LoadingDialog progressDialog;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        CustomIntent.customType(AccountSettingsActivity.this, "right-to-left");
    }

    boolean isNightModeEnabled() {
        SharedPreferences sharedPref = getSharedPreferences("dark_mode", MODE_PRIVATE);
        return sharedPref.getBoolean("dark_mode", false);
    }

    void updateUsernameOnUserPosts(String oldName, String newName) {

        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("posts");
        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    if (snap.child("name").getValue(String.class).equals(oldName)) {
                        postsRef.child(snap.child("id").getValue(String.class)).child("name").setValue(newName);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AccountSettingsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    void showChangePasswordDialog() {

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        AlertDialog.Builder dialog = new AlertDialog.Builder(AccountSettingsActivity.this)
                .setTitle("Change Password");
        final EditText newPassEt = new EditText(AccountSettingsActivity.this);
        newPassEt.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        newPassEt.setHint("New password");
        dialog.setView(newPassEt);
        dialog.setPositiveButton("Okay", (dialogInterface, i) -> {

            if (!(newPassEt.getText().toString().isEmpty() && newPassEt.getText().toString().isEmpty())) {

                user.updatePassword(newPassEt.getText().toString()).addOnSuccessListener(unused -> {
                    Toast.makeText(AccountSettingsActivity.this, "Password updated!", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> Toast.makeText(AccountSettingsActivity.this, "Error: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());


            } else {
                Toast.makeText(AccountSettingsActivity.this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
            }

            dialogInterface.dismiss();
        }).setNegativeButton("Cancel", (dialogInterface, i) -> {
            dialogInterface.dismiss();
        }).show();

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(550, FrameLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(50, 6, 30, 6);
        newPassEt.setLayoutParams(params);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (isNightModeEnabled()) {
            Resources.Theme theme = super.getTheme();
            theme.applyStyle(R.style.AppTheme_Base_Night, true);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        progressDialog = LoadingDialog.Companion.get(AccountSettingsActivity.this);

        ImageButton backBtn = findViewById(R.id.imageButton7);
        EditText username = findViewById(R.id.username_settings);
        EditText email = findViewById(R.id.email_settings);
        Button save_changes_btn = findViewById(R.id.save_changes_btn);
        Button changePasswordBtn = findViewById(R.id.change_password_settings);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() != 0) {
                    save_changes_btn.setEnabled(true);
                }
            }
        };

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        username.setText(user.getDisplayName());
        email.setText(user.getEmail());
        backBtn.setOnClickListener(v -> onBackPressed());

        username.addTextChangedListener(textWatcher);
        email.addTextChangedListener(textWatcher);

        changePasswordBtn.setOnClickListener(view -> {

            AlertDialog.Builder dialog = new AlertDialog.Builder(AccountSettingsActivity.this)
                    .setTitle("Confirm your password");
            final EditText pass = new EditText(AccountSettingsActivity.this);
            pass.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
            pass.setHint("Current password");
            dialog.setView(pass);
            dialog.setPositiveButton("Okay", (dialogInterface, i) -> {

                progressDialog.show();

                if (!pass.getText().toString().isEmpty()) {
                    progressDialog.show();
                    auth.signInWithEmailAndPassword(user.getEmail(), pass.getText().toString()).addOnCompleteListener(task -> progressDialog.hide())
                            .addOnSuccessListener(authResult -> {
                        showChangePasswordDialog();
                    }).addOnFailureListener(e -> {
                        dialogInterface.dismiss();
                        Toast.makeText(AccountSettingsActivity.this, "Error: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    });
                    progressDialog.hide();
                } else {
                    Toast.makeText(AccountSettingsActivity.this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
                }

                dialogInterface.dismiss();
            }).setNegativeButton("Cancel", (dialogInterface, i) -> {
                dialogInterface.dismiss();
            }).show();

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(550, FrameLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(50, 6, 30, 6);
            pass.setLayoutParams(params);
        });

        save_changes_btn.setOnClickListener(v -> {

            if (!(username.getText().toString().isEmpty() && email.getText().toString().isEmpty())) {

                save_changes_btn.setEnabled(false);
                save_changes_btn.setText("Please Wait...");

                if (!user.getEmail().equals(email.getText().toString())) {

                    user.updateEmail(email.getText().toString()).addOnCompleteListener(task -> {

                        save_changes_btn.setEnabled(true);
                        save_changes_btn.setText("Save Changes");

                        if (task.isSuccessful()) {
                            Toast.makeText(AccountSettingsActivity.this, "Email updated!", Toast.LENGTH_SHORT).show();
                        } else if (task.isCanceled()) {
                            Toast.makeText(AccountSettingsActivity.this, "Error: Can't update email", Toast.LENGTH_SHORT).show();
                        }

                    });

                }

                if (username.getText().length() > 25 || username.getText().length() < 3) {
                    SmartDialogBox.showErrorDialog(AccountSettingsActivity.this, "Username must be 5-25 characters.", "OK");
                }else {

                    if (!user.getDisplayName().equals(username.getText().toString())) {

                        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

                        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                boolean nameExists = false;

                                for (DataSnapshot snap : snapshot.getChildren()) {
                                    if (Objects.equals(snap.child("name").getValue(String.class), username.getText().toString())) {
                                        nameExists = true;
                                        break;
                                    }
                                }

                                if (nameExists) {
                                    save_changes_btn.setEnabled(true);
                                    save_changes_btn.setText("Save Changes");
                                    Toast.makeText(AccountSettingsActivity.this, "This username is already used by another user", Toast.LENGTH_LONG).show();
                                } else {

                                    // update username on current user node
                                    usersRef.child(user.getUid()).child("name").setValue(username.getText().toString());

                                    // update username on all user posts
                                    updateUsernameOnUserPosts(user.getDisplayName(), username.getText().toString());

                                    // update username on FirebaseAuth
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(username.getText().toString()).build();
                                    user.updateProfile(profileUpdates).addOnCompleteListener(task -> {

                                        save_changes_btn.setEnabled(true);
                                        save_changes_btn.setText("Save Changes");

                                        if (task.isSuccessful()) {
                                            Toast.makeText(AccountSettingsActivity.this, "Username updated!", Toast.LENGTH_SHORT).show();
                                        } else if (task.isCanceled()) {
                                            Toast.makeText(AccountSettingsActivity.this, "Error: Can't update username", Toast.LENGTH_SHORT).show();
                                        }

                                    });

                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(AccountSettingsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }

                }

            }
        });

    }
}