package com.george.socialmeme.Fragments;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.claudylab.smartdialogbox.SmartDialogBox;
import com.developer.kalert.KAlertDialog;
import com.george.socialmeme.Activities.LoginActivity;
import com.george.socialmeme.R;
import com.george.socialmeme.Activities.HomeActivity;
import com.george.socialmeme.Activities.RegisterActivity;
import com.george.socialmeme.Models.UploadPostModel;
import com.github.loadingview.LoadingDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.concurrent.atomic.AtomicBoolean;

import maes.tech.intentanim.CustomIntent;

public class NewPostFragment extends Fragment {

    final private DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("posts");
    final private StorageReference storageReference = FirebaseStorage.getInstance().getReference("images");
    private Uri mediaUri;
    private static String mediaType;
    final private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    final private FirebaseUser user = mAuth.getCurrentUser();
    KAlertDialog loadingDialog;

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Upload new meme")
                            .setMessage("Are you sure you want to upload this file?\nFile: " + result.getData().getData().getPath())
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent data = result.getData();
                                    mediaUri = data.getData();
                                    uploadPostToFirebase(mediaUri, mediaType);
                                }
                            })
                            .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss())
                            .show();
                }
            });


    private String getFileExtension(Uri uri) {
        if (uri != null) {
            ContentResolver cr = getActivity().getContentResolver();
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            return mime.getExtensionFromMimeType(cr.getType(uri));
        }else {
            return "no_extension";
        }
    }

    boolean isNightModeEnabled() {
        SharedPreferences sharedPref = getContext().getSharedPreferences("dark_mode", MODE_PRIVATE);
        return sharedPref.getBoolean("dark_mode", false);
    }

    private void uploadPostToFirebase(Uri uri, String type) {

        String postId = postsRef.push().getKey();
        AtomicBoolean uploadCancelled = new AtomicBoolean(false);

        if (type.equals("text")) {

            AlertDialog dialog;

            // Set dialog theme
            if (isNightModeEnabled()) {
                dialog = new AlertDialog.Builder(getContext()).create();
                Window window = dialog.getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.BLACK);
            } else {
                dialog = new AlertDialog.Builder(getContext()).create();
                Window window = dialog.getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.WHITE);
            }

            LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View dialogView = layoutInflater.inflate(R.layout.upload_text_dialog, null);

            EditText jokeTitleEditText = dialogView.findViewById(R.id.joke_title);
            EditText jokeContentEditText = dialogView.findViewById(R.id.joke_content);
            Button uploadJokeButton = dialogView.findViewById(R.id.upload_joke_btn);
            ImageButton dismissDialogButton = dialogView.findViewById(R.id.dismissDialogBtn);

            uploadJokeButton.setOnClickListener(view -> {

                if (jokeTitleEditText.getText().toString().isEmpty() || jokeContentEditText.getText().toString().isEmpty()) {
                    SmartDialogBox.showErrorDialog(getActivity(), "Title or joke content cannot be empty", "OK");
                }else {
                    dialog.dismiss();
                    UploadPostModel model = new UploadPostModel();
                    postsRef.child(postId).setValue(model);
                    postsRef.child(postId).child("name").setValue(user.getDisplayName());
                    postsRef.child(postId).child("likes").setValue("0");
                    postsRef.child(postId).child("id").setValue(postId);
                    postsRef.child(postId).child("postType").setValue(type);
                    postsRef.child(postId).child("joke_title").setValue(jokeTitleEditText.getText().toString());
                    postsRef.child(postId).child("joke_content").setValue(jokeContentEditText.getText().toString());

                    if (user.getPhotoUrl() != null) {
                        postsRef.child(postId).child("authorProfilePictureURL").setValue(user.getPhotoUrl().toString())
                                .addOnCompleteListener(task -> loadingDialog.hide());
                    } else {
                        postsRef.child(postId).child("authorProfilePictureURL").setValue("none")
                                .addOnCompleteListener(task -> loadingDialog.hide());
                    }

                    Toast.makeText(getActivity(), "Joke uploaded!", Toast.LENGTH_SHORT).show();
                    HomeActivity.bottomNavBar.setItemSelected(R.id.home_fragment, true);
                }
            });

            dismissDialogButton.setOnClickListener(view -> dialog.dismiss());

            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setView(dialogView);
            dialog.show();

        }

        // Set a name for the audio if the user uploading an audio file
        if (type.equals("audio")) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity())
                    .setTitle("Give a name to your audio")
                    .setCancelable(false);
            final EditText audioNameET = new EditText(getActivity());
            audioNameET.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
            audioNameET.setHint("Write a name");
            dialog.setView(audioNameET);

            dialog.setPositiveButton("Ok", (dialogInterface, i) -> {
                if (!audioNameET.getText().toString().isEmpty()) {

                    StorageReference fileRef = storageReference.child(postId + "." + getFileExtension(uri));
                    fileRef.putFile(uri).addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri1 -> {
                        loadingDialog.hide();

                        UploadPostModel model = new UploadPostModel(uri1.toString());
                        postsRef.child(postId).setValue(model);
                        postsRef.child(postId).child("name").setValue(user.getDisplayName());
                        postsRef.child(postId).child("likes").setValue("0");
                        postsRef.child(postId).child("id").setValue(postId);
                        postsRef.child(postId).child("postType").setValue(type);

                        if (!audioNameET.getText().toString().isEmpty()) {
                            postsRef.child(postId).child("audioName").setValue(audioNameET.getText().toString());
                        }

                        if (user.getPhotoUrl() != null) {
                            postsRef.child(postId).child("authorProfilePictureURL").setValue(user.getPhotoUrl().toString());
                        } else {
                            postsRef.child(postId).child("authorProfilePictureURL").setValue("none");
                        }
                        Toast.makeText(getActivity(), "Meme uploaded!", Toast.LENGTH_SHORT).show();
                        HomeActivity.bottomNavBar.setItemSelected(R.id.home_fragment, true);

                    })).addOnProgressListener(snapshot -> {
                        loadingDialog.show();
                        loadingDialog.setTitleText("Uploading meme...");
                    })
                            .addOnFailureListener(e -> {
                                loadingDialog.hide();
                                Toast.makeText(getActivity(), "Upload fail: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });

                } else {
                    Toast.makeText(getActivity(), "Please provide a name for your sound and try again", Toast.LENGTH_LONG).show();
                    dialogInterface.dismiss();
                }
            }).setNegativeButton("Cancel", (dialogInterface, i) -> {
                uploadCancelled.set(true);
                dialogInterface.dismiss();
            }).show();

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(550, FrameLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(50, 6, 30, 6);
            audioNameET.setLayoutParams(params);
        } else {

            if (uri != null) {
                StorageReference fileRef = storageReference.child(postId + "." + getFileExtension(uri));
                fileRef.putFile(uri).addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri1 -> {

                    loadingDialog.hide();

                    UploadPostModel model = new UploadPostModel(uri1.toString());
                    postsRef.child(postId).setValue(model);
                    postsRef.child(postId).child("name").setValue(user.getDisplayName());
                    postsRef.child(postId).child("likes").setValue("0");
                    postsRef.child(postId).child("id").setValue(postId);
                    postsRef.child(postId).child("postType").setValue(type);

                    if (user.getPhotoUrl() != null) {
                        postsRef.child(postId).child("authorProfilePictureURL").setValue(user.getPhotoUrl().toString());
                    } else {
                        postsRef.child(postId).child("authorProfilePictureURL").setValue("none");
                    }

                    Toast.makeText(getActivity(), "Meme uploaded!", Toast.LENGTH_SHORT).show();
                    HomeActivity.bottomNavBar.setItemSelected(R.id.home_fragment, true);

                })).addOnProgressListener(snapshot -> loadingDialog.show())
                        .addOnFailureListener(e -> {
                            loadingDialog.hide();
                            Toast.makeText(getActivity(), "Upload fail: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            }

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_post, container, false);

        AdView mAdView = view.findViewById(R.id.adView2);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        loadingDialog =  new KAlertDialog(getContext(), KAlertDialog.PROGRESS_TYPE);
        loadingDialog.getProgressHelper().setBarColor(R.color.main);
        loadingDialog.setTitleText("Loading...");
        loadingDialog.setCancelable(false);

        View selectPicture = view.findViewById(R.id.select_img_btn);
        View selectVideo = view.findViewById(R.id.select_video_btn);
        View selectAudio = view.findViewById(R.id.select_audio_btn);
        View uploadText = view.findViewById(R.id.upload_text_view);

        if (HomeActivity.anonymous) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Sign in required")
                    .setMessage("You need to sign in to upload memes!")
                    .setPositiveButton("Okay", (dialogInterface, i) -> {
                        getActivity().finish();
                        startActivity(new Intent(getActivity(), RegisterActivity.class));
                        CustomIntent.customType(getActivity(), "fadein-to-fadeout");
                    })
                    .setCancelable(false)
                    .show();
        }

        uploadText.setOnClickListener(v -> {
            mediaType = "text";
            uploadPostToFirebase(mediaUri, "text");
        });

        selectAudio.setOnClickListener(view12 -> {
            mediaType = "audio";
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("audio/*");
            activityResultLauncher.launch(intent);
        });

        selectVideo.setOnClickListener(view1 -> {
            mediaType = "video";
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("video/*");
            activityResultLauncher.launch(intent);
        });

        selectPicture.setOnClickListener(v -> {
            mediaType = "image";
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            activityResultLauncher.launch(intent);
        });

        return view;
    }
}