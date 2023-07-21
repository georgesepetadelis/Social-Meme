package com.george.socialmeme.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.george.socialmeme.R;
import com.github.loadingview.LoadingDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;
import maes.tech.intentanim.CustomIntent;

public class SelectProfileImageActivity extends AppCompatActivity {

    LoadingDialog progressDialog;
    private Button set_image;
    private Button skip;
    private Uri imgUri;
    CircleImageView img;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(mAuth.getCurrentUser().getUid());


    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {

                Intent data = result.getData();

                if (result.getResultCode() == Activity.RESULT_OK && data != null) {
                    imgUri = data.getData();
                    img.setImageURI(imgUri);
                    uploadToFirebase(imgUri);
                    set_image.setEnabled(false);
                    skip.setEnabled(false);
                }
            });


    private void uploadToFirebase(Uri uri) {

        // Show loading dialog
        progressDialog.show();

        StorageReference fileRef = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(uri));

        fileRef.putFile(uri).addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri1 -> {
            userRef.child("profileImgUrl").setValue(uri1.toString());

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(uri1).build();
            user.updateProfile(profileUpdates);

            finish();
            startActivity(new Intent(SelectProfileImageActivity.this, HomeActivity.class));
            CustomIntent.customType(SelectProfileImageActivity.this, "left-to-right");

        })).addOnFailureListener(e -> Toast.makeText(SelectProfileImageActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()).addOnCompleteListener(task -> {
            progressDialog.hide();
            set_image.setEnabled(true);
            skip.setEnabled(true);
        });

    }

    private String getFileExtension(Uri mUri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(mUri));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_profile_image);

        progressDialog = LoadingDialog.Companion.get(this);
        set_image = findViewById(R.id.select_image);
        skip = findViewById(R.id.skip_image);
        img = findViewById(R.id.profile_image);

        set_image.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            someActivityResultLauncher.launch(intent);
        });

        skip.setOnClickListener(v -> {

            // Show loading dialog
            progressDialog.show();

            userRef.child("profileImgUrl").setValue("none").addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    finish();
                    startActivity(new Intent(SelectProfileImageActivity.this, HomeActivity.class));
                    CustomIntent.customType(SelectProfileImageActivity.this, "left-to-right");
                }
            }).addOnFailureListener(e -> new AlertDialog.Builder(SelectProfileImageActivity.this)
                    .setTitle("Error")
                    .setMessage(e.getMessage())
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss()));

        });

    }
}