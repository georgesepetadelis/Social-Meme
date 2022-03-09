package com.george.socialmeme.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.george.socialmeme.R;
import com.george.socialmeme.Activities.HomeActivity;
import com.george.socialmeme.Activities.RegisterActivity;
import com.george.socialmeme.Models.UploadPostModel;
import com.github.loadingview.LoadingDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import maes.tech.intentanim.CustomIntent;

public class NewPostFragment extends Fragment {

    final private DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("posts");
    final private StorageReference storageReference = FirebaseStorage.getInstance().getReference("images");
    private Uri mediaUri;
    private static String mediaType;
    final private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    final private FirebaseUser user = mAuth.getCurrentUser();
    LoadingDialog loadingDialog;


    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {

                    new AlertDialog.Builder(getContext())
                            .setTitle("Upload new meme")
                            .setMessage("Are you sure you want to upload this file?\nFile: " + result.getData().getData().getPath())
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (mediaType.equals("video")) {
                                        Intent data = result.getData();
                                        mediaUri = data.getData();
                                        uploadPostToFirebase(mediaUri, "video");
                                    } else {
                                        Intent data = result.getData();
                                        mediaUri = data.getData();
                                        uploadPostToFirebase(mediaUri, "image");
                                    }
                                }
                            })
                            .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss())
                            .show();
                }
            });


    private String getFileExtension(Uri mUri) {
        ContentResolver cr = getActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(mUri));
    }

    private void uploadPostToFirebase(Uri uri, String type) {

        String postId = postsRef.push().getKey();

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
            getActivity().finish();
            startActivity(new Intent(getActivity(), HomeActivity.class));
            CustomIntent.customType(getActivity(), "fadein-to-fadeout");

        })).addOnProgressListener(snapshot -> loadingDialog.show())
                .addOnFailureListener(e -> {
                    loadingDialog.hide();
                    Toast.makeText(getActivity(), "Upload fail: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_post, container, false);

        AdView mAdView = view.findViewById(R.id.adView2);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        loadingDialog = LoadingDialog.Companion.get(getActivity());
        View select_img = view.findViewById(R.id.select_img_btn);
        View selectVideo = view.findViewById(R.id.select_video_btn);

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

        selectVideo.setOnClickListener(view1 -> {
            mediaType = "video";
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("video/*");
            someActivityResultLauncher.launch(intent);
        });

        select_img.setOnClickListener(v -> {
            mediaType = "image";
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            someActivityResultLauncher.launch(intent);
        });

        return view;
    }
}