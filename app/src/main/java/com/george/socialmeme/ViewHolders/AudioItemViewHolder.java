package com.george.socialmeme.ViewHolders;

import static com.george.socialmeme.Helpers.NotificationHelper.sendNotification;
import static com.george.socialmeme.Helpers.PostHelper.followPostAuthor;
import static com.george.socialmeme.Helpers.PostHelper.showCommentsDialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.george.socialmeme.Activities.Feed.HomeActivity;
import com.george.socialmeme.Activities.Profile.UserProfileActivity;
import com.george.socialmeme.Models.CommentModel;
import com.george.socialmeme.Models.PostModel;
import com.george.socialmeme.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hugomatilla.audioplayerview.AudioPlayerView;

import java.io.IOException;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import maes.tech.intentanim.CustomIntent;

public class AudioItemViewHolder extends RecyclerView.ViewHolder {

    public Context context;
    public PostModel postModel;
    public CircleImageView profilePicture;
    public AudioPlayerView audioPlayerView;
    public TextView likesCounter, usernameTV, commentsCounter, audioName, followBtn;
    public ImageButton playBtn, shareBtn, likeBtn, postOptionsBtn, saveBtn;
    public View openCommentsView, openUserProfileView;
    public ProgressBar audioViewProgressBar;
    public boolean isAudioPlaying, isPostLiked;
    public String postID, postAuthorID, audioURL;
    public HashMap<String, CommentModel> comments;
    public ConstraintLayout followBtnView;

    public AudioItemViewHolder(@NonNull View itemView) {
        super(itemView);
        profilePicture = itemView.findViewById(R.id.user_profile_image);
        audioPlayerView = itemView.findViewById(R.id.audioView);
        likesCounter = itemView.findViewById(R.id.like_counter);
        usernameTV = itemView.findViewById(R.id.post_username);
        commentsCounter = itemView.findViewById(R.id.textView63);
        audioName = itemView.findViewById(R.id.textView46);
        playBtn = itemView.findViewById(R.id.imageButton19);
        shareBtn = itemView.findViewById(R.id.imageButton13);
        likeBtn = itemView.findViewById(R.id.likeBtn);
        postOptionsBtn = itemView.findViewById(R.id.imageButton16);
        openCommentsView = itemView.findViewById(R.id.openCommentsViewAudioItem);
        audioViewProgressBar = itemView.findViewById(R.id.progressBar4);
        openUserProfileView = itemView.findViewById(R.id.constraintLayout10);
        isAudioPlaying = false;
        followBtnView = itemView.findViewById(R.id.follow_btn_img);
        followBtn = itemView.findViewById(R.id.textView81);
        saveBtn = itemView.findViewById(R.id.imageButton26);

        postOptionsBtn.setOnClickListener(view -> showPostOptionsBottomSheet());
        openCommentsView.setOnClickListener(view -> showCommentsDialog(comments, usernameTV, commentsCounter, context, postID));
        followBtn.setOnClickListener(view -> followPostAuthor(context, postModel, followBtn, usernameTV));

        if (!HomeActivity.singedInAnonymously && !usernameTV.getText().toString().equals(user.getDisplayName())) {
            followBtnView.setVisibility(View.VISIBLE);
        } else {
            followBtnView.setVisibility(View.GONE);
        }

        // Check if logged-in user follows post author
        // to hide follow button
        if (user != null) {
            usersRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.hasChild("following")) {
                        // Logged-in user follows post author
                        // hide follow btn
                        if (postAuthorID != null) {
                            if (snapshot.child("following").child(postAuthorID).exists()) {
                                followBtnView.setVisibility(View.GONE);
                            }
                        } else {
                            followBtnView.setVisibility(View.GONE);
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (HomeActivity.singedInAnonymously) {
            saveBtn.setVisibility(View.GONE);
        }

        saveBtn.setOnClickListener(v -> {
            saveBtn.setEnabled(false);
            saveBtn.setAlpha(0.5f);
            saveAudioFileToDeviceStorage();
        });

        playBtn.setOnClickListener(view -> {
            isAudioPlaying = !isAudioPlaying;
            try {
                audioPlayerView.toggleAudio();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (isAudioPlaying) {
                playBtn.setImageResource(R.drawable.ic_pause);
            } else {
                playBtn.setImageResource(R.drawable.ic_play);
            }
        });

        audioPlayerView.setOnAudioPlayerViewListener(new AudioPlayerView.OnAudioPlayerViewListener() {
            @Override
            public void onAudioPreparing() {
                playBtn.setVisibility(View.GONE);
                audioViewProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAudioReady() {
                playBtn.setVisibility(View.VISIBLE);
                audioViewProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onAudioFinished() {
                playBtn.setImageResource(R.drawable.ic_play);
                isAudioPlaying = false;
            }
        });

        openUserProfileView.setOnClickListener(v -> {

            if (postAuthorID != null) {
                if (postAuthorID.equals(user.getUid())) {
                    if (HomeActivity.bottomNavBar == null) {
                        Intent intent = new Intent(context, UserProfileActivity.class);
                        intent.putExtra("user_id", postAuthorID);
                        intent.putExtra("username", usernameTV.getText().toString());
                        context.startActivity(intent);
                        CustomIntent.customType(context, "left-to-right");
                    } else {
                        int selectedItemId = HomeActivity.bottomNavBar.getSelectedItemId();
                        if (selectedItemId != R.id.my_profile_fragment) {
                            HomeActivity.bottomNavBar.setItemSelected(R.id.my_profile_fragment, true);
                        }
                    }
                } else {
                    Intent intent = new Intent(context, UserProfileActivity.class);
                    intent.putExtra("user_id", postAuthorID);
                    intent.putExtra("username", usernameTV.getText().toString());
                    context.startActivity(intent);
                    CustomIntent.customType(context, "left-to-right");
                }
            }

        });

        openUserProfileView.setOnLongClickListener(view -> {
            copyUsernameToClipboard();
            return true;
        });

        likeBtn.setOnClickListener(v -> {

            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));

            // Animate like button when clicked
            YoYo.with(Techniques.Shake)
                    .duration(500)
                    .repeat(0)
                    .playOn(likeBtn);

            if (isPostLiked) {
                isPostLiked = false;
                likeBtn.setImageResource(R.drawable.ic_like);
                likesRef.child(postID).child(user.getUid()).removeValue();
                updateLikesToDB(postID, false);
            } else {
                isPostLiked = true;
                likeBtn.setImageResource(R.drawable.ic_like_filled);
                likesRef.child(postID).child(user.getUid()).setValue("true");
                updateLikesToDB(postID, true);

                if (!user.getUid().equals(postAuthorID)) {
                    sendNotification(context, usernameTV, postModel.getId(), "like", "");
                }

            }

        });

    }

    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
    DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("posts");
    DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference("likes");

    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = auth.getCurrentUser();

    public void setContext(Context context) {
        this.context = context;
    }

    public void copyUsernameToClipboard() {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("username", usernameTV.getText().toString());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, "Username copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    public void updateLikesToDB(String postID, boolean likePost) {

        String currentLikesToString = likesCounter.getText().toString();
        int currentLikesToInt = Integer.parseInt(currentLikesToString);

        if (likePost) {

            int newCurrentLikes = currentLikesToInt + 1;
            String newCurrentLikesToString = Integer.toString(newCurrentLikes);

            // Update likes on Real-time DB
            postsRef.child(postID).child("likes").setValue(newCurrentLikesToString);

            // update likes on TextView
            likesCounter.setText(newCurrentLikesToString);

            // Animate like counter TextView
            YoYo.with(Techniques.FadeInUp)
                    .duration(500)
                    .repeat(0)
                    .playOn(likesCounter);

        } else {

            int newCurrentLikes = currentLikesToInt - 1;
            String newCurrentLikesToString = Integer.toString(newCurrentLikes);

            // Update likes on Real-time DB
            postsRef.child(postID).child("likes").setValue(newCurrentLikesToString);

            // update likes on TextView
            likesCounter.setText(newCurrentLikesToString);

            // Animate like counter TextView
            YoYo.with(Techniques.FadeInDown)
                    .duration(500)
                    .repeat(0)
                    .playOn(likesCounter);

        }

    }

    private void deletePost() {

        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(audioURL);
        storageReference.delete()
                .addOnSuccessListener(unused -> Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        postsRef.child(postID).removeValue().addOnCompleteListener(task -> {
            likeBtn.setVisibility(View.GONE);
            likesCounter.setVisibility(View.GONE);
            openCommentsView.setEnabled(false);
            openCommentsView.setEnabled(false);
            usernameTV.setText("DELETED POST");
            playBtn.setEnabled(false);
            profilePicture.setImageResource(R.drawable.user);
            openUserProfileView.setEnabled(false);
            postOptionsBtn.setVisibility(View.GONE);
        });

    }

    private void saveAudioFileToDeviceStorage() {

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(audioURL));
        request.setDescription("Downloading audio");
        request.setTitle("Downloading " + usernameTV.getText().toString() + " post");

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, postID + ".mp3");
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);

        Toast.makeText(context, "Download started...", Toast.LENGTH_SHORT).show();
        sendNotification(context, usernameTV, postModel.getId(), "meme_saved", "");

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
        if (!usernameTV.getText().toString().equals(user.getDisplayName())) {
            dialog.findViewById(R.id.delete_post_view).setVisibility(View.GONE);
        }

        downloadMemeView.setOnClickListener(view -> {
            saveAudioFileToDeviceStorage();
            dialog.dismiss();
        });

        reportPostView.setOnClickListener(view -> {
            DatabaseReference reportsRef = FirebaseDatabase.getInstance().getReference("reportedPosts");
            FirebaseAuth auth = FirebaseAuth.getInstance();
            reportsRef.child(auth.getCurrentUser().getUid()).setValue(postID).addOnCompleteListener(task -> {
                likeBtn.setVisibility(View.GONE);
                likesCounter.setVisibility(View.GONE);
                openCommentsView.setEnabled(false);
                openCommentsView.setEnabled(false);
                usernameTV.setText("REPORTED POST");
                playBtn.setEnabled(false);
                profilePicture.setImageResource(R.drawable.user);
                openUserProfileView.setEnabled(false);
                postOptionsBtn.setVisibility(View.GONE);
                Toast.makeText(context, "Report received, thank you!", Toast.LENGTH_SHORT).show();
                reportsRef.child(postID);
                FirebaseDatabase.getInstance().getReference("posts").child(postID).child("reported").setValue("true");
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
        dialog.getWindow().getAttributes().windowAnimations = R.style.BottomSheetDialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

}
