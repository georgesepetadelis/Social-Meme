package com.george.socialmeme.Helpers;

import static com.george.socialmeme.Helpers.AppHelper.isNightModeEnabled;
import static com.george.socialmeme.Helpers.NotificationHelper.sendNotification;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.george.socialmeme.Activities.HomeActivity;
import com.george.socialmeme.Adapters.CommentsRecyclerAdapter;
import com.george.socialmeme.Models.CommentModel;
import com.george.socialmeme.Models.PostModel;
import com.george.socialmeme.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostHelper {

    public static ArrayList<String> extractMentionedUsers(String input) {
        ArrayList<String> mentionedUsers = new ArrayList<>();
        // pattern captures emojis and usernames containing emojis.
        Pattern pattern = Pattern.compile("@([_\\p{L}\\p{N}\\p{M}']+\\s*(?:[\\p{L}\\p{N}\\p{M}']+|\\p{So})+)"); // \\p{So} represents emojis
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            String username = matcher.group(1);
            mentionedUsers.add(username);
        }
        return mentionedUsers;
    }

    public static HashMap<Integer, Integer> findNameIndices(ArrayList<String> names, String text) {
        HashMap<Integer, Integer> result = new HashMap<>();
        for (String name : names) {
            int startIndex = 0;
            while (startIndex != -1) {
                startIndex = text.indexOf(name, startIndex);
                if (startIndex != -1) {
                    int endIndex = startIndex + name.length() - 1;
                    result.put(startIndex, endIndex + 1);
                    startIndex++;
                }
            }
        }
        return result;
    }

    public static void showCommentsDialog(HashMap<String, CommentModel> comments_, TextView username, TextView commentsCount, Context context, String postID) {

        AlertDialog dialog;
        FirebaseUser user = AuthHelper.getCurrentUser();

        // Set dialog theme
        if (isNightModeEnabled(context)) {
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
        } else {
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
                } else {
                    commentModel.setAuthorProfilePictureURL("none");
                }

                // Update comment counter on post item inside RecyclerView
                String currentCommentsCountToString = commentsCount.getText().toString();
                int newCurrentCommentsCountToInt = Integer.parseInt(currentCommentsCountToString) + 1;
                commentsCount.setText(String.valueOf(newCurrentCommentsCountToInt));

                ArrayList<String> tempNames = new ArrayList<>();
                for (String name : extractMentionedUsers(commentET.getText().toString())) {
                    tempNames.add(name);
                }

                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        HashMap<String, String> mentionedUsers = new HashMap<>();
                        for (DataSnapshot user : snapshot.getChildren()) {
                            if (user.child("name").exists()) {
                                if (tempNames.contains(user.child("name").getValue(String.class))) {
                                    String currentUserID = user.child("id").getValue(String.class);
                                    String username = user.child("name").getValue(String.class);
                                    mentionedUsers.put(currentUserID, username);
                                }
                            }
                        }
                        commentModel.setMentionedUsers(mentionedUsers);

                        commentModel.getMentionedUsers().forEach((uid, name) -> {
                            NotificationHelper.sendNotification(context, username, postID, "mention", "");
                        });

                        rootRef.child("posts").child(postID).child("comments")
                                .child(commentModel.getCommentID()).setValue(commentModel)
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

                        if (!username.getText().toString().equals(user.getDisplayName())) {
                            sendNotification(context, username, postID, "comment_added",
                                    commentET.getText().toString());
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


                /*
                findNameIndices(extractMentionedUsers(commentET.getText().toString()), commentET.getText().toString())
                        .forEach((start, end) -> {
                            Toast.makeText(context, "Start " + start + " end " + end, Toast.LENGTH_SHORT).show();
                        });*/

            } else {
                Toast.makeText(context, "Please write a comment", Toast.LENGTH_SHORT).show();
            }
        });

        rootRef.child("posts").child(postID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                HashMap<String, CommentModel> comments;
                PostModel currentPost = snapshot.getValue(PostModel.class);

                if (currentPost.getComments() != null) {
                    comments = currentPost.getComments();
                } else {
                    comments = new HashMap<>();
                }

                ArrayList<CommentModel> commentModelList = new ArrayList<>(comments.values());
                if (!comments.isEmpty()) {
                    for (CommentModel comment : commentModelList) {
                        commentModelArrayList.add(comment);
                        adapter.notifyDataSetChanged();
                        adapter.notifyItemInserted(commentModelArrayList.size() - 1);
                    }
                } else {
                    noCommentsMsg.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        recyclerViewProgressBar.setVisibility(View.GONE);
        dismissDialogButton.setOnClickListener(view -> dialog.dismiss());

        dialog.setView(dialogView);
        dialog.getWindow().getAttributes().windowAnimations = R.style.BottomSheetDialogAnimation;
        dialog.show();

    }

    public static void followPostAuthor(Context context, PostModel postModel, TextView followBtn, TextView username) {

        String postAuthorID = postModel.getAuthorID();
        FirebaseUser user = AuthHelper.getCurrentUser();
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
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
                        sendNotification(context, username, postModel.getId(), "follow", "");
                        Toast.makeText(context, "You started following " + username.getText().toString(), Toast.LENGTH_SHORT).show();
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

}
