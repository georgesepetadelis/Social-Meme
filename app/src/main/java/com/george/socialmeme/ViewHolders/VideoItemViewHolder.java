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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.george.socialmeme.Activities.Profile.FullScreenVideoActivity;
import com.george.socialmeme.Activities.Feed.HomeActivity;
import com.george.socialmeme.Activities.Profile.UserProfileActivity;
import com.george.socialmeme.Models.CommentModel;
import com.george.socialmeme.Models.PostModel;
import com.george.socialmeme.R;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import maes.tech.intentanim.CustomIntent;

public class VideoItemViewHolder extends RecyclerView.ViewHolder {

    public PostModel postModel;
    public Context context;
    public StyledPlayerView andExoPlayerView;
    public String postID, postAuthorID, videoURL;
    public ConstraintLayout openProfileView, followBtnView;
    public View openCommentsView;
    public TextView username, like_counter_tv, commentsCount, followBtn;
    public CircleImageView profilePicture;
    public HashMap<String, CommentModel> comments;
    public ImageButton like_btn, postOptionsButton, shareBtn, commentsBtn, enterFullScreenBtn, saveBtn;
    public boolean isPostLiked = false;

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
            like_btn.setVisibility(View.GONE);
            like_counter_tv.setVisibility(View.GONE);
            openCommentsView.setEnabled(false);
            openCommentsView.setEnabled(false);
            username.setText("DELETED POST");
            andExoPlayerView.setVisibility(View.GONE);
            profilePicture.setImageResource(R.drawable.user);
            openProfileView.setEnabled(false);
            postOptionsButton.setVisibility(View.GONE);
        });

    }

    public VideoItemViewHolder(@NonNull View itemView) {
        super(itemView);

        andExoPlayerView = itemView.findViewById(R.id.andExoPlayerView);
        profilePicture = itemView.findViewById(R.id.circleImageView2);
        username = itemView.findViewById(R.id.textView27);
        like_btn = itemView.findViewById(R.id.imageButton8);
        like_counter_tv = itemView.findViewById(R.id.textView36);
        openProfileView = itemView.findViewById(R.id.view5);
        postOptionsButton = itemView.findViewById(R.id.imageButton12);
        commentsCount = itemView.findViewById(R.id.textView71);
        openCommentsView = itemView.findViewById(R.id.openCommentsViewVideoItem);
        shareBtn = itemView.findViewById(R.id.imageButton14);
        commentsBtn = itemView.findViewById(R.id.imageButton11);
        followBtnView = itemView.findViewById(R.id.follow_btn_img);
        followBtn = itemView.findViewById(R.id.textView81);
        enterFullScreenBtn = itemView.findViewById(R.id.enter_fullscreen_btn);
        saveBtn = itemView.findViewById(R.id.imageButton25);

        openCommentsView.setOnClickListener(view -> showCommentsDialog(comments, username, commentsCount, context, postID));
        postOptionsButton.setOnClickListener(view -> showPostOptionsBottomSheet());
        followBtn.setOnClickListener(view -> followPostAuthor(context, postModel, followBtn, username));

        if (!HomeActivity.signedInAnonymously && !username.getText().toString().equals(user.getDisplayName())) {
            followBtnView.setVisibility(View.VISIBLE);
        } else {
            followBtnView.setVisibility(View.GONE);
        }

        if (HomeActivity.signedInAnonymously) {
            saveBtn.setVisibility(View.GONE);
        }

        saveBtn.setOnClickListener(v -> {
            saveBtn.setEnabled(false);
            saveBtn.setAlpha(0.5f);
            saveVideoToDeviceStorage();
        });

        // Check if logged-in user follows post author
        // to hide follow btn
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

        like_btn.setOnClickListener(v -> {

            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));

            // Animate like button when clicked
            YoYo.with(Techniques.Shake)
                    .duration(500)
                    .repeat(0)
                    .playOn(like_btn);

            if (isPostLiked) {
                isPostLiked = false;
                like_btn.setImageResource(R.drawable.ic_like);
                likesRef.child(postID).child(user.getUid()).removeValue();
                updateLikesToDB(postID, false);
            } else {
                isPostLiked = true;
                like_btn.setImageResource(R.drawable.ic_like_filled);
                likesRef.child(postID).child(user.getUid()).setValue("true");
                updateLikesToDB(postID, true);

                if (!user.getUid().equals(postAuthorID)) {
                    sendNotification(context, username, postModel.getId(), "like", "");
                }

            }

        });

        if (HomeActivity.signedInAnonymously) {
            openProfileView.setEnabled(false);
        }

        enterFullScreenBtn.setOnClickListener(v -> {
            Intent intent = new Intent(context, FullScreenVideoActivity.class);
            intent.putExtra("video_url", videoURL);
            context.startActivity(intent);
            CustomIntent.customType(context, "left-to-right");

            // stop video from playing inside recycler view
            Objects.requireNonNull(andExoPlayerView.getPlayer()).pause();

        });

        andExoPlayerView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {

            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                Objects.requireNonNull(andExoPlayerView.getPlayer()).pause();
            }
        });

        openProfileView.setOnClickListener(v -> {

            if (postAuthorID != null) {
                if (postAuthorID.equals(user.getUid())) {
                    if (HomeActivity.bottomNavBar == null) {
                        Intent intent = new Intent(context, UserProfileActivity.class);
                        intent.putExtra("user_id", postAuthorID);
                        intent.putExtra("username", username.getText().toString());
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
                    intent.putExtra("username", username.getText().toString());
                    context.startActivity(intent);
                    CustomIntent.customType(context, "left-to-right");
                }
            }

        });

        openProfileView.setOnLongClickListener(view -> {
            copyUsernameToClipboard();
            return true;
        });

    }

    public void showPostOptionsBottomSheet() {

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
            DatabaseReference reportsRef = FirebaseDatabase.getInstance().getReference("reportedPosts");
            FirebaseAuth auth = FirebaseAuth.getInstance();
            reportsRef.child(auth.getCurrentUser().getUid()).setValue(postID).addOnCompleteListener(task -> {
                like_btn.setVisibility(View.GONE);
                like_counter_tv.setVisibility(View.GONE);
                openCommentsView.setEnabled(false);
                openCommentsView.setEnabled(false);
                username.setText("REPORTED POST");
                shareBtn.setVisibility(View.GONE);
                andExoPlayerView.setVisibility(View.GONE);
                profilePicture.setImageResource(R.drawable.user);
                openProfileView.setEnabled(false);
                postOptionsButton.setVisibility(View.GONE);
                reportsRef.child(postID);
                FirebaseDatabase.getInstance().getReference("posts").child(postID).child("reported").setValue("true");
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
        dialog.getWindow().getAttributes().windowAnimations = R.style.BottomSheetDialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    private void saveVideoToDeviceStorage() {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(videoURL));
        request.setDescription("Downloading video");
        request.setTitle("Downloading " + username.getText().toString() + " post");
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, postID + ".mp4");
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);
        Toast.makeText(context, "Download started...", Toast.LENGTH_SHORT).show();
        sendNotification(context, username, postModel.getId(), "meme_saved", "");
    }


    public void updateLikesToDB(String postID, boolean likePost) {

        String currentLikesToString = like_counter_tv.getText().toString();
        int currentLikesToInt = Integer.parseInt(currentLikesToString);

        if (likePost) {

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
    
    void copyUsernameToClipboard() {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("username", username.getText().toString());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, "Username copied to clipboard", Toast.LENGTH_SHORT).show();
    }
}