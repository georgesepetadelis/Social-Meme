package com.george.socialmeme.Dialogs;

import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.george.socialmeme.R;
import com.george.socialmeme.Activities.HomeActivity;
import com.github.loadingview.LoadingDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import maes.tech.intentanim.CustomIntent;

public class PostOptionsDialog extends AppCompatDialogFragment {

    boolean isAuthor = false;
    String postId, postSourceURL, postType, authorName;
    ImageView postImage;
    DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("posts");
    DatabaseReference reportsRef = FirebaseDatabase.getInstance().getReference("reports");
    LoadingDialog progressDialog;

    ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(getActivity(), "Permission denied, can't save meme to gallery", Toast.LENGTH_SHORT).show();
                } else {
                    savePictureToDeviceStorage();
                }
            });

    void sendPostSavedNotificationToUser() {

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String notificationID = usersRef.push().getKey();
                String currentDate = String.valueOf(android.text.format.DateFormat.format("dd-MM-yyyy", new java.util.Date()));

                for (DataSnapshot snap : snapshot.getChildren()) {

                    if (snap.child("name").getValue().toString().equals(authorName)) {
                        String postAuthorID = snap.child("id").getValue().toString();
                        usersRef.child(postAuthorID).child("notifications").child(notificationID).child("title").setValue("Meme saved");
                        usersRef.child(postAuthorID).child("notifications").child(notificationID).child("type").setValue("post_save");
                        usersRef.child(postAuthorID).child("notifications").child(notificationID).child("date").setValue(currentDate);
                        usersRef.child(postAuthorID).child("notifications").child(notificationID).child("message").setValue(user.getDisplayName() + " has saved your post");
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @NonNull
    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        progressDialog = LoadingDialog.Companion.get(getActivity());

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.post_options_dialog, null);
        View downloadMemeView = view.findViewById(R.id.save_post_btn);
        View reportView = view.findViewById(R.id.view6);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);

        View deleteView = view.findViewById(R.id.view7);

        // Hide report view is the logged in user is post author
        if (isAuthor) {
            reportView.setVisibility(View.GONE);
            view.findViewById(R.id.imageView5).setVisibility(View.GONE);
            view.findViewById(R.id.textView46).setVisibility(View.GONE);
        }

        // If the current logged in user is not the author
        // of the post, hide delete view
        if (!isAuthor) {
            deleteView.setVisibility(View.GONE);
            view.findViewById(R.id.imageView7).setVisibility(View.GONE);
            view.findViewById(R.id.textView48).setVisibility(View.GONE);
        }

        downloadMemeView.setOnClickListener(view12 -> {
            if (postType.equals("image")) {
                savePictureToDeviceStorage();
            } else {
                saveVideoToDeviceStorage();
            }
        });

        reportView.setOnClickListener(view1 -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            reportsRef.child(auth.getCurrentUser().getUid()).setValue(postId);
            Toast.makeText(getActivity(), "Report received, thank you!", Toast.LENGTH_SHORT).show();
        });


        deleteView.setOnClickListener(v -> {
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

    private void saveVideoToDeviceStorage() {

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(postSourceURL));
        request.setDescription("Downloading video");
        request.setTitle("Downloading " + authorName + " post");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }

        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, postId + ".mp4");
        DownloadManager downloadManager = (DownloadManager) getContext().getSystemService(getContext().DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);

        Toast.makeText(getActivity(), "Download started...", Toast.LENGTH_SHORT).show();

    }

    private void savePictureToDeviceStorage() {

        postImage.buildDrawingCache();
        Bitmap bmp = postImage.getDrawingCache();
        File storageLoc = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File file = new File(storageLoc, postId + ".jpg");

        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
            scanFile(getContext(), Uri.fromFile(file));
            Toast.makeText(getContext(), "Meme saved in: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();

            // send notification to user
            sendPostSavedNotificationToUser();

        } catch (FileNotFoundException e) {
            Toast.makeText(getActivity(), "File not found", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(getActivity(), "Error saving file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private static void scanFile(Context context, Uri imageUri) {
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        scanIntent.setData(imageUri);
        context.sendBroadcast(scanIntent);
    }

    public void setPostType(String postType) {
        this.postType = postType;
    }
    public void setAuthor(boolean author) {
        isAuthor = author;
    }
    public void setPostId(String postId) {
        this.postId = postId;
    }
    public void setPostImage(ImageView postImage) {
        this.postImage = postImage;
    }
    public void setPostSourceURL(String postSourceURL) {
        this.postSourceURL = postSourceURL;
    }
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
}