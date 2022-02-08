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

import com.george.socialmeme.R;
import com.github.loadingview.LoadingDialog;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInApi;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import maes.tech.intentanim.CustomIntent;

public class LoginActivity extends AppCompatActivity {

    LoadingDialog progressDialog;
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
                            // Google Sign In was successful, authenticate with Firebase
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                            firebaseAuthWithGoogle(account.getIdToken());
                        } catch (ApiException e) {
                            progressDialog.hide();
                            new AlertDialog.Builder(LoginActivity.this)
                                    .setTitle("Error")
                                    .setMessage(e.getLocalizedMessage())
                                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                    .show();
                        }
                    }else {
                        progressDialog.hide();
                        Toast.makeText(LoginActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                }
            });


    void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        someActivityResultLauncher.launch(signInIntent);
        //startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    void singIn(String email, String password) {

        // show progress dialog
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(authResult -> {
            progressDialog.hide();
            finish();
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            CustomIntent.customType(LoginActivity.this, "left-to-right");
            HomeActivity.anonymous = false;
        }).addOnFailureListener(e -> {
            progressDialog.hide();
            new AlertDialog.Builder(LoginActivity.this)
                    .setTitle("Whoops!")
                    .setMessage(e.getMessage())
                    .setPositiveButton("Okay", (dialog, which) -> dialog.dismiss()).show();
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

        progressDialog = LoadingDialog.Companion.get(LoginActivity.this);

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
                new AlertDialog.Builder(LoginActivity.this)
                        .setTitle("Whoops!")
                        .setMessage("All fields are required")
                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss()).show();
            } else {
                singIn(email.getText().toString(), password.getText().toString());
            }
        });

    }

    private void sendResetPasswordLink(String email) {

        progressDialog.show();

        mAuth.sendPasswordResetEmail(email).addOnSuccessListener(task -> {

            progressDialog.hide();

            new AlertDialog.Builder(LoginActivity.this)
                    .setIcon(R.drawable.success_icon)
                    .setTitle("Email send")
                    .setMessage("Reset password link has been send on this email address \n" + email)
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss()).show();

        }).addOnFailureListener(e -> {

            progressDialog.hide();

            new AlertDialog.Builder(LoginActivity.this)
                    .setIcon(R.drawable.error_icon)
                    .setTitle("Error")
                    .setMessage(e.getLocalizedMessage())
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss()).show();
        });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user, null);
                    } else {
                        // If sign in fails, display a message to the user.
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
                        usersRef.child(user.getUid()).child("email").setValue(user.getEmail());
                        usersRef.child(user.getUid()).child("profileImgUrl").setValue(user.getPhotoUrl());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(LoginActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            finish();
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            CustomIntent.customType(LoginActivity.this, "left-to-right");

            Toast.makeText(LoginActivity.this, "Signed in as " + user.getDisplayName(), Toast.LENGTH_SHORT).show();

        } else {
            new AlertDialog.Builder(LoginActivity.this)
                    .setTitle("Error")
                    .setMessage(errorMsg.getLocalizedMessage())
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
        }
        progressDialog.hide();
    }

}