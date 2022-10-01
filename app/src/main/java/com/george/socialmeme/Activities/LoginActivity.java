package com.george.socialmeme.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.claudylab.smartdialogbox.SmartDialogBox;
import com.developer.kalert.KAlertDialog;
import com.george.socialmeme.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import maes.tech.intentanim.CustomIntent;

public class LoginActivity extends AppCompatActivity {

    KAlertDialog progressDialog;
    public static String TAG = "GoogleActivity: ";
    public static int RC_SIGN_IN = 9001;
    GoogleSignInClient mGoogleSignInClient;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK || result.getResultCode() == RC_SIGN_IN) {

                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

                        try {
                            // Google Sign In was successful, authenticate user with Firebase
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            firebaseAuthWithGoogle(account.getIdToken());
                        } catch (ApiException e) {
                            progressDialog.hide();
                            SmartDialogBox.showErrorDialog(LoginActivity.this, e.getLocalizedMessage(), "OK");
                        }
                    } else {
                        progressDialog.hide();
                        SmartDialogBox.showErrorDialog(LoginActivity.this, "Error: Can't sign-in with google right now. Try again later.", "OK");
                    }
                }
            });


    void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        someActivityResultLauncher.launch(signInIntent);
    }

    void updateUserTokenOnDB() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userRef.child("fcm_token").setValue(task.getResult());
            }
        });
    }

    void singIn(String email, String password) {

        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email.trim(), password).addOnSuccessListener(authResult -> {
            progressDialog.hide();
            updateUserTokenOnDB();
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            CustomIntent.customType(LoginActivity.this, "left-to-right");
            finish();
            HomeActivity.singedInAnonymously = false;
            HomeActivity.showLoadingScreen = true;
        }).addOnFailureListener(e -> {
            progressDialog.hide();
            SmartDialogBox.showErrorDialog(LoginActivity.this, e.getLocalizedMessage(), "OK");
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        CustomIntent.customType(LoginActivity.this, "right-to-left");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressDialog = new KAlertDialog(LoginActivity.this, KAlertDialog.PROGRESS_TYPE);
        progressDialog.getProgressHelper().setBarColor(R.color.main);
        progressDialog.setTitleText("Signing in...");
        progressDialog.setCancelable(false);

        EditText email = findViewById(R.id.email_login);
        EditText password = findViewById(R.id.password_login);
        Button submit = findViewById(R.id.submit_login);
        TextView resetPasswordTV = findViewById(R.id.textView6);
        TextView register = findViewById(R.id.register_login);
        ConstraintLayout googleSignInBtn = findViewById(R.id.signInButton);

        // Google sign config
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInBtn.setOnClickListener(view -> {
            progressDialog.show();
            googleSignIn();
        });

        resetPasswordTV.setOnClickListener(view -> {

            AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this)
                    .setTitle("Reset your password");
            final EditText email_et = new EditText(LoginActivity.this);
            email_et.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
            email_et.setHint("Enter your email");
            dialog.setView(email_et);
            dialog.setPositiveButton("OK", (dialogInterface, i) -> {
                if (!email_et.getText().toString().isEmpty()) {
                    sendResetPasswordLink(email_et.getText().toString());
                }
            }).setNegativeButton("Cancel", (dialogInterface, i) -> {
                dialogInterface.dismiss();
            }).show();

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(550, FrameLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(50, 6, 30, 6);
            email_et.setLayoutParams(params);
        });

        register.setOnClickListener(v -> {
            finish();
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            CustomIntent.customType(LoginActivity.this, "fadein-to-fadeout");
        });

        submit.setOnClickListener(v -> {
            if (email.getText().toString().isEmpty() || password.getText().toString().isEmpty()) {
                SmartDialogBox.showErrorDialog(LoginActivity.this, "Email or password cannot be empty", "OK");
            } else {
                singIn(email.getText().toString(), password.getText().toString());
            }
        });

    }

    private void sendResetPasswordLink(String email) {
        progressDialog.show();
        mAuth.sendPasswordResetEmail(email.trim()).addOnSuccessListener(task -> {
            progressDialog.hide();
            SmartDialogBox.showSuccessDialog(LoginActivity.this, "Reset password link has been send on this email address\n" + email, "OK");
        }).addOnFailureListener(e -> {
            progressDialog.hide();
            SmartDialogBox.showErrorDialog(LoginActivity.this, e.getLocalizedMessage(), "OK");
        });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "GoogleSignIn:success");
                FirebaseUser user = mAuth.getCurrentUser();
                updateUI(user, null);
            } else {
                // If sign-in fails, display a message to the user.
                updateUI(null, task.getException());
            }
        });
    }

    private void updateUI(FirebaseUser user, Exception errorMsg) {
        progressDialog.hide();

        if (user != null) {
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.child(user.getUid()).exists()) {

                        usersRef.child(user.getUid()).child("id").setValue(user.getUid());
                        usersRef.child(user.getUid()).child("name").setValue(user.getDisplayName());
                        usersRef.child(user.getUid()).child("profileImgUrl").setValue(user.getPhotoUrl().toString());
                    }else {
                        usersRef.child(user.getUid()).child("id").setValue(user.getUid());
                        usersRef.child(user.getUid()).child("name").setValue(user.getDisplayName());
                        usersRef.child(user.getUid()).child("profileImgUrl").setValue(user.getPhotoUrl().toString());

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(LoginActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            updateUserTokenOnDB();
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            CustomIntent.customType(LoginActivity.this, "left-to-right");
            finish();
            HomeActivity.showLoadingScreen = true;

        } else {
            SmartDialogBox.showErrorDialog(LoginActivity.this, errorMsg.getLocalizedMessage(), "OK");
        }
        progressDialog.hide();
    }

}