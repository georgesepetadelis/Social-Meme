package com.george.socialmeme.Dialogs;

import android.Manifest;
import android.app.Dialog;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.app.ActivityCompat;

import com.george.socialmeme.R;
import com.george.socialmeme.Activities.HomeActivity;
import com.github.loadingview.LoadingDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;

import maes.tech.intentanim.CustomIntent;

public class PostOptionsDialog extends AppCompatDialogFragment {

    boolean isAuthor = false;
    String postId, postSourceURL;
    ImageView postImage;
    DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("posts");
    DatabaseReference reportsRef = FirebaseDatabase.getInstance().getReference("reports");
    LoadingDialog progressDialog;

    private String getFilePath() {
        ContextWrapper contextWrapper = new ContextWrapper(getContext());
        File imagesDir = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = new File(imagesDir, System.currentTimeMillis() + ".png");
        return file.getPath();
    }

    void downloadPostToDevice() {

        BitmapDrawable bitmapDrawable = (BitmapDrawable) postImage.getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();

        FileOutputStream outputStream = null;
        ContextWrapper contextWrapper = new ContextWrapper(getContext());
        File imagesDir = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = new File(imagesDir, System.currentTimeMillis() + ".png");
        //imagesDir.mkdirs();

        String filename = file.getName();
        File outFile = new File(imagesDir, filename);

        try {
            outputStream = new FileOutputStream(outFile);
        }catch (Exception e) {
            Toast.makeText(getActivity(), "Error: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

        try {
            outputStream.flush();
        }catch (Exception e) {
            Toast.makeText(getActivity(), "Error1: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }

        try {
            outputStream.close();
        }catch (Exception e) {
            Toast.makeText(getActivity(), "Error2: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    @NonNull
    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        progressDialog = LoadingDialog.Companion.get(getActivity());

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.post_options_dialog, null);
        View downloadMemeView = view.findViewById(R.id.save_post_btn);
        View deleteView = view.findViewById(R.id.view7);
        View reportView = view.findViewById(R.id.view6);
        ImageView deleteImageView = view.findViewById(R.id.imageView7);
        TextView deleteTextView = view.findViewById(R.id.textView48);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);

        if (!isAuthor) {
            deleteView.setVisibility(View.GONE);
            deleteImageView.setVisibility(View.GONE);
            deleteTextView.setVisibility(View.GONE);
        }

        downloadMemeView.setOnClickListener(view12 -> {

            // Request permission to read/write to device storage
            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

            downloadPostToDevice();

        });

        reportView.setOnClickListener(view1 -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            reportsRef.child(auth.getCurrentUser().getUid()).setValue(postId);
            Toast.makeText(getActivity(), "Report received, thank you!", Toast.LENGTH_SHORT).show();
        });


        deleteView.setOnClickListener(v -> {

            // show loading dialog
            progressDialog.show();

            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(postSourceURL);
            storageReference.delete()
                    .addOnSuccessListener(unused -> Toast.makeText(getContext(), "Post deleted", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());

            postsRef.child(postId).removeValue().addOnCompleteListener(task -> progressDialog.hide());

            getActivity().finish();
            startActivity(new Intent(getActivity(), HomeActivity.class));
            CustomIntent.customType(getActivity(), "fadein-to-fadeout");

        });

        return builder.create();
    }

    public void setAuthor(boolean author) {
        isAuthor = author;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public ImageView getPostImage() {
        return postImage;
    }

    public void setPostImage(ImageView postImage) {
        this.postImage = postImage;
    }

    public String getPostSourceURL() {
        return postSourceURL;
    }

    public void setPostSourceURL(String postSourceURL) {
        this.postSourceURL = postSourceURL;
    }
}