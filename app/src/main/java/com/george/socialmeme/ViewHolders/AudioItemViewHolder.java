package com.george.socialmeme.ViewHolders;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.george.socialmeme.Activities.HomeActivity;
import com.george.socialmeme.Activities.UserProfileActivity;
import com.george.socialmeme.Adapters.CommentsRecyclerAdapter;
import com.george.socialmeme.Models.CommentModel;
import com.george.socialmeme.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.hugomatilla.audioplayerview.AudioPlayerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import maes.tech.intentanim.CustomIntent;

public class AudioItemViewHolder extends RecyclerView.ViewHolder {

    public Context context;
    public CircleImageView profilePicture;
    public AudioPlayerView audioPlayerView;
    public TextView likesCounter, usernameTV, commentsCounter, audioName, followBtn;
    public ImageButton playBtn, shareBtn, likeBtn, postOptionsBtn;
    public View openCommentsView, openUserProfileView;
    public ProgressBar audioViewProgressBar;
    public boolean isAudioPlaying, isPostLiked;
    public String postID, postAuthorID, audioURL;
    public ConstraintLayout followBtnView;

    void followPostAuthor() {

        usersRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("following")) {
                    // Logged-in user follows post author
                    if (!snapshot.child("following").child(postAuthorID).exists()) {
                        usersRef.child(postAuthorID).child("followers").child(user.getUid()).setValue(user.getUid());
                        usersRef.child(user.getUid()).child("following").child(postAuthorID).setValue(postAuthorID);
                        followBtn.setTextColor(context.getColor(R.color.gray));
                        followBtn.setText("Following");
                        followBtn.setEnabled(false);
                        sendNotificationToPostAuthor("follow", "");
                        Toast.makeText(context, "You started following " + usernameTV.getText().toString(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "You already following this user.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

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

        postOptionsBtn.setOnClickListener(view -> showPostOptionsBottomSheet());
        openCommentsView.setOnClickListener(view -> showCommentsDialog());
        followBtn.setOnClickListener(view -> followPostAuthor());

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
                    int selectedItemId = HomeActivity.bottomNavBar.getSelectedItemId();
                    if (selectedItemId != R.id.my_profile_fragment) {
                        HomeActivity.bottomNavBar.setItemSelected(R.id.my_profile_fragment, true);
                    }
                } else {
                    Intent intent = new Intent(context, UserProfileActivity.class);
                    intent.putExtra("user_id", postAuthorID);
                    intent.putExtra("username", usernameTV.getText().toString());
                    intent.putExtra("allPosts", new Gson().toJson(HomeActivity.savedPostsArrayList));
                    context.startActivity(intent);
                    CustomIntent.customType(context, "left-to-right");
                }
            }
        });

        openUserProfileView.setOnLongClickListener(view -> {
            copyUsernameToClipboard();
            return false;
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
                    sendNotificationToPostAuthor("like", "");
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

    void sendNotificationToPostAuthor(String notificationType, String commentText) {

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        final String[] notification_message = {"none"};
        final String[] notification_title = {"none"};

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String notificationID = usersRef.push().getKey();
                String currentDate = String.valueOf(android.text.format.DateFormat.format("dd-MM-yyyy", new java.util.Date()));

                Calendar calendar = Calendar.getInstance();
                int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                int currentMinutes = calendar.get(Calendar.MINUTE);

                for (DataSnapshot snap : snapshot.getChildren()) {
                    if (snap.child("name").getValue(String.class) != null) {
                        if (snap.child("name").getValue().toString().equals(usernameTV.getText().toString())) {

                            String postAuthorID = snap.child("id").getValue().toString();
                            usersRef.child(postAuthorID).child("notifications").child(notificationID).child("date").setValue(currentDate);

                            if (notificationType.equals("like")) {
                                notification_title[0] = "New like";
                                notification_message[0] = user.getDisplayName() + " liked your post";
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("title").setValue(notification_title[0]);
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("type").setValue("like");
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("date").setValue(currentDate + " " + currentHour + ":" + currentMinutes);
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("message").setValue(notification_message[0]);
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("post_id").setValue(postID);
                            }
                            if (notificationType.equals("meme_saved")) {
                                notification_title[0] = "Meme saved";
                                notification_message[0] = user.getDisplayName() + " has saved your post";
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("title").setValue("Meme saved");
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("type").setValue("post_save");
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("date").setValue(currentDate + " " + currentHour + ":" + currentMinutes);
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("message").setValue(user.getDisplayName() + " has saved your post");
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("post_id").setValue(postID);
                            } else if (notificationType.equals("comment_added")) {
                                notification_title[0] = "New comment";
                                notification_message[0] = user.getDisplayName() + ": " + commentText;
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("title").setValue(notification_title[0]);
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("type").setValue("comment_added");
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("date").setValue(currentDate + " " + currentHour + ":" + currentMinutes);
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("message").setValue(notification_message[0]);
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("post_id").setValue(postID);
                            }

                            break;
                        }
                    }
                }

                // Find user token from DB
                // and add notification to Firestore
                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    if (userSnap.child("name").getValue(String.class) != null) {
                        if (userSnap.child("name").getValue(String.class).equals(usernameTV.getText().toString())) {
                            if (userSnap.child("fcm_token").exists()) {
                                // add notification to Firestore to send
                                // push notification from back-end
                                Map<String, Object> notification = new HashMap<>();
                                notification.put("token", userSnap.child("fcm_token").getValue(String.class));
                                notification.put("title", notification_title[0]);
                                notification.put("message", notification_message[0]);
                                notification.put("not_type", notificationType);
                                notification.put("postID", postID);
                                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                                firestore.collection("notifications")
                                        .document(notificationID).set(notification);
                            }
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
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
        sendNotificationToPostAuthor("meme_saved", "");

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

    boolean isNightModeEnabled() {
        SharedPreferences sharedPref = context.getSharedPreferences("dark_mode", MODE_PRIVATE);
        return sharedPref.getBoolean("dark_mode", false);
    }

    private void showCommentsDialog() {

        AlertDialog dialog;

        // Set dialog theme
        if (isNightModeEnabled()) {
            dialog = new AlertDialog.Builder(context, R.style.AppTheme_Base_Night).create();
            Window window = dialog.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);
        } else {
            dialog = new AlertDialog.Builder(context, R.style.Theme_SocialMeme).create();
            Window window = dialog.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.WHITE);
        }

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = layoutInflater.inflate(R.layout.comments_dialog_fragment, null);

        CircleImageView profilePicture = dialogView.findViewById(R.id.comments_profile_image);
        ImageButton dismissDialogButton = dialogView.findViewById(R.id.imageButton17);
        EditText commentET = dialogView.findViewById(R.id.writeCommentET);
        ImageButton addCommentBtn = dialogView.findViewById(R.id.imageButton18);
        ProgressBar recyclerViewProgressBar = dialogView.findViewById(R.id.commentsProgressBar);
        RecyclerView commentsRecyclerView = dialogView.findViewById(R.id.comments_recycler_view);
        TextView noCommentsMsg = dialogView.findViewById(R.id.textView22);

        ArrayList<CommentModel> commentModelArrayList = new ArrayList<>();
        CommentsRecyclerAdapter adapter = new CommentsRecyclerAdapter(commentModelArrayList, context, dialog.getOwnerActivity());
        commentsRecyclerView.setAdapter(adapter);

        AdView mAdView = dialogView.findViewById(R.id.adView6);
        AdRequest adRequest = new AdRequest.Builder().build();
        if (HomeActivity.show_banners) mAdView.loadAd(adRequest);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        commentsRecyclerView.setAdapter(adapter);
        commentsRecyclerView.setHasFixedSize(true);
        commentsRecyclerView.setLayoutManager(layoutManager);

        // Load current user profile picture
        if (user.getPhotoUrl() != null) {
            Glide.with(context).load(user.getPhotoUrl().toString()).into(profilePicture);
        }else {
            profilePicture.setImageResource(R.drawable.user);
        }

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        addCommentBtn.setOnClickListener(view -> {
            if (!commentET.getText().toString().isEmpty()) {

                ProgressBar progressBar = dialogView.findViewById(R.id.addCommentProgressBar);
                progressBar.setVisibility(View.VISIBLE);
                addCommentBtn.setVisibility(View.INVISIBLE);

                String commendID = rootRef.push().getKey();

                CommentModel commentModel = new CommentModel();
                commentModel.setAuthor(user.getUid());
                commentModel.setCommentID(commendID);
                commentModel.setAuthorUsername(user.getDisplayName());
                commentModel.setPostID(postID);
                commentModel.setCommentText(commentET.getText().toString());

                if (user.getPhotoUrl() != null) {
                    commentModel.setAuthorProfilePictureURL(user.getPhotoUrl().toString());
                }else {
                    commentModel.setAuthorProfilePictureURL("none");
                }

                // Update comment counter on post item inside RecyclerView
                String currentCommentsCountToString = commentsCounter.getText().toString();
                int newCurrentCommentsCountToInt = Integer.parseInt(currentCommentsCountToString) + 1;
                commentsCounter.setText(String.valueOf(newCurrentCommentsCountToInt));

                // Add comment to Firebase Real-Time database
                rootRef.child("posts").child(postID).child("comments").child(commendID).setValue(commentModel)
                        .addOnSuccessListener(unused -> {

                            // Add comment to RecyclerView
                            commentModelArrayList.add(commentModel);
                            adapter.notifyDataSetChanged();
                            adapter.notifyItemInserted(commentModelArrayList.size() - 1);

                            progressBar.setVisibility(View.GONE);
                            addCommentBtn.setVisibility(View.VISIBLE);
                            commentET.setText("");

                            // Hide no comments warning message if is visible
                            if (commentModelArrayList.size() == 1) {
                                noCommentsMsg.setVisibility(View.GONE);
                            }

                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Error: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            addCommentBtn.setVisibility(View.VISIBLE);
                        });

                sendNotificationToPostAuthor("comment_added", commentET.getText().toString());

            } else {
                Toast.makeText(context, "Please write a comment", Toast.LENGTH_SHORT).show();
            }
        });

        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("posts").child(postID).hasChild("comments")) {
                    for (DataSnapshot commentsSnapshot : snapshot.child("posts").child(postID).child("comments").getChildren()) {
                        CommentModel commentModel = new CommentModel();
                        commentModel.setAuthor(commentsSnapshot.child("author").getValue(String.class));
                        commentModel.setCommentID(commentsSnapshot.child("commentID").getValue(String.class));
                        commentModel.setAuthorUsername(commentsSnapshot.child("authorUsername").getValue(String.class));
                        commentModel.setPostID(commentsSnapshot.child("postID").getValue(String.class));
                        commentModel.setAuthorProfilePictureURL(commentsSnapshot.child("authorProfilePictureURL").getValue(String.class));
                        commentModel.setCommentText(commentsSnapshot.child("commentText").getValue(String.class));
                        commentModelArrayList.add(commentModel);
                        adapter.notifyDataSetChanged();
                        adapter.notifyItemInserted(commentModelArrayList.size() - 1);
                    }
                }else {
                    noCommentsMsg.setVisibility(View.VISIBLE);
                }
                recyclerViewProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        dismissDialogButton.setOnClickListener(view -> dialog.dismiss());

        dialog.setView(dialogView);
        dialog.getWindow().getAttributes().windowAnimations = R.style.BottomSheetDialogAnimation;
        dialog.show();

    }

}
