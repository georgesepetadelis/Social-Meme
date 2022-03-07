package com.george.socialmeme.ViewHolders;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.george.socialmeme.Activities.HomeActivity;
import com.george.socialmeme.Activities.UserProfileActivity;
import com.george.socialmeme.R;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;
import maes.tech.intentanim.CustomIntent;

public class VideoViewHolder extends RecyclerView.ViewHolder {

    public Context context;
    public StyledPlayerView andExoPlayerView;
    public String postID, userID, videoURL;
    public View openProfileView;
    public TextView username, like_counter_tv;
    public CircleImageView profilePicture;
    public ImageButton like_btn;
    public ImageButton postOptionsButton;
    public boolean isLiked = false;

    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = auth.getCurrentUser();

    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
    DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("posts");
    DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference("likes");

    public void setContext(Context context) {
        this.context = context;
    }


    private void deletePost() {

        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(videoURL);
        storageReference.delete()
                .addOnSuccessListener(unused -> Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        postsRef.child(postID).removeValue().addOnCompleteListener(task -> {
            context.startActivity(new Intent(context, HomeActivity.class));
            CustomIntent.customType(context, "fadein-to-fadeout");
        });

    }

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

                    if (snap.child("name").getValue().toString().equals(username.getText().toString())) {
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
                Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void saveVideoToDeviceStorage() {

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(videoURL));
        request.setDescription("Downloading video");
        request.setTitle("Downloading " + username.getText().toString() + "'s post");

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, postID + ".mp4");
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);

        Toast.makeText(context, "Download started...", Toast.LENGTH_SHORT).show();

        sendPostSavedNotificationToUser();

    }

    void showPostOptionsBottomSheet() {

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.post_options_bottom_sheet);

        View downloadMemeView = dialog.findViewById(R.id.view13);
        View reportPostView = dialog.findViewById(R.id.view15);
        View deletePostView = dialog.findViewById(R.id.view17);

        // Hide delete view if the current logged in user is not
        // the author of the current post
        if (!username.getText().toString().equals(user.getDisplayName())) {
            dialog.findViewById(R.id.delete_post_view).setVisibility(View.GONE);
        }

        downloadMemeView.setOnClickListener(view -> {
            saveVideoToDeviceStorage();
            dialog.dismiss();
        });

        reportPostView.setOnClickListener(view -> {
            DatabaseReference reportsRef = FirebaseDatabase.getInstance().getReference("reports");
            FirebaseAuth auth = FirebaseAuth.getInstance();
            reportsRef.child(auth.getCurrentUser().getUid()).setValue(postID).addOnCompleteListener(task -> {
                Toast.makeText(context, "Report received, thank you!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        });

        deletePostView.setOnClickListener(view -> {
            new AlertDialog.Builder(context)
                    .setTitle("Are you sure?")
                    .setMessage("Are you sure you want to delete this meme?. This action cannot be undone.")
                    .setIcon(R.drawable.ic_report)
                    .setPositiveButton("Yes", (dialogInterface, i) -> {
                        deletePost();
                        dialog.dismiss();
                    })
                    .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss())
                    .show();
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }


    public VideoViewHolder(@NonNull View itemView) {
        super(itemView);

        andExoPlayerView = itemView.findViewById(R.id.andExoPlayerView);
        profilePicture = itemView.findViewById(R.id.circleImageView2);
        username = itemView.findViewById(R.id.textView27);
        like_btn = itemView.findViewById(R.id.imageButton8);
        like_counter_tv = itemView.findViewById(R.id.textView36);
        openProfileView = itemView.findViewById(R.id.view5);
        postOptionsButton = itemView.findViewById(R.id.imageButton12);

        postOptionsButton.setOnClickListener(view -> {
            if (!HomeActivity.anonymous) {
                showPostOptionsBottomSheet();
            }
        });

        like_btn.setOnClickListener(v -> {

            // Animate like button when clicked
            YoYo.with(Techniques.Shake)
                    .duration(500)
                    .repeat(0)
                    .playOn(like_btn);

            isLiked = true;

            likesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if (isLiked) {
                        if (snapshot.child(postID).hasChild(user.getUid())) {
                            // Post is liked from this user, so user wants to unlike this post
                            like_btn.setImageResource(R.drawable.ic_thump_up_outline);
                            likesRef.child(postID).child(user.getUid()).removeValue();
                            isLiked = false;

                            // Update likes to DB
                            updateLikes(postID, false);
                        } else {
                            // Post is not liked from ths user, so the user wants to like this post
                            like_btn.setImageResource(R.drawable.ic_thumb_up_filled);
                            likesRef.child(postID).child(user.getUid()).setValue("true");

                            // Update likes to DB
                            updateLikes(postID, true);

                            sendLikeNotificationToUser();

                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    Toast.makeText(itemView.getContext(), "Error: " + error, Toast.LENGTH_LONG).show();
                }
            });
        });

        openProfileView.setOnClickListener(v -> {

            if (!HomeActivity.anonymous) {
                Intent intent = new Intent(context, UserProfileActivity.class);
                usersRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            if (snap.child("name").getValue().toString().equals(username.getText().toString())) {
                                UserProfileActivity.username = username.getText().toString();
                                UserProfileActivity.userID = snap.child("id").getValue().toString();
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                context.startActivity(intent);
                CustomIntent.customType(context, "left-to-right");

            }

        });

        openProfileView.setOnLongClickListener(view -> {
            copyUsernameToClipboard();
            return false;
        });

    }


    public void updateLikes(String postID, boolean isNotLiked) {
        postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                String currentLikesToString = snapshot.child(postID).child("likes").getValue().toString();
                int currentLikesToInt = Integer.parseInt(currentLikesToString);

                if (isNotLiked) {

                    int newCurrentLikes = currentLikesToInt + 1;
                    String newCurrentLikesToString = Integer.toString(newCurrentLikes);

                    // Update likes on Real-time DB
                    postsRef.child(postID).child("likes").setValue(newCurrentLikesToString);

                    // update likes on TextView
                    like_counter_tv.setText(newCurrentLikesToString);

                    // Animate like counter TextView
                    YoYo.with(Techniques.FadeInUp)
                            .duration(500)
                            .repeat(0)
                            .playOn(like_counter_tv);

                } else {

                    int newCurrentLikes = currentLikesToInt - 1;
                    String newCurrentLikesToString = Integer.toString(newCurrentLikes);

                    // Update likes on Real-time DB
                    postsRef.child(postID).child("likes").setValue(newCurrentLikesToString);

                    // update likes on TextView
                    like_counter_tv.setText(newCurrentLikesToString);

                    // Animate like counter TextView
                    YoYo.with(Techniques.FadeInDown)
                            .duration(500)
                            .repeat(0)
                            .playOn(like_counter_tv);

                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(context, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    void copyUsernameToClipboard() {

        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("username", username.getText().toString());
        clipboard.setPrimaryClip(clip);

        Toast.makeText(context, "Username copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    void sendLikeNotificationToUser() {

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String notificationID = usersRef.push().getKey();
                String currentDate = String.valueOf(android.text.format.DateFormat.format("dd-MM-yyyy", new java.util.Date()));

                Calendar calendar = Calendar.getInstance();
                int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                int currentMinutes = calendar.get(Calendar.MINUTE);

                for (DataSnapshot snap : snapshot.getChildren()) {

                    if (snap.child("name").getValue().toString().equals(username.getText().toString())) {
                        String postAuthorID = snap.child("id").getValue().toString();
                        usersRef.child(postAuthorID).child("notifications").child(notificationID).child("title").setValue("New like");
                        usersRef.child(postAuthorID).child("notifications").child(notificationID).child("type").setValue("like");
                        usersRef.child(postAuthorID).child("notifications").child(notificationID).child("date").setValue(currentDate + "  " + currentHour + ":" + currentMinutes);
                        usersRef.child(postAuthorID).child("notifications").child(notificationID).child("message").setValue(user.getDisplayName() + " liked your post");
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}