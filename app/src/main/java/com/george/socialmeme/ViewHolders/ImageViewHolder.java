package com.george.socialmeme.ViewHolders;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.george.socialmeme.Activities.HomeActivity;
import com.george.socialmeme.Activities.UserProfileActivity;
import com.george.socialmeme.Dialogs.PostOptionsDialog;
import com.george.socialmeme.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;
import maes.tech.intentanim.CustomIntent;

public class ImageViewHolder extends RecyclerView.ViewHolder {

    public Context context;
    public CardView container;
    public String postID, postImageURL, userID;
    public TextView username, like_counter_tv;
    public ImageView postImg;
    public ImageButton like_btn, show_comments_btn;
    public CircleImageView profileImage;
    public boolean isLiked = false;
    public ConstraintLayout openUserProfileView;

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

    public ImageViewHolder(@NonNull View itemView) {
        super(itemView);

        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();
        final ConstraintLayout sContainer = itemView.findViewById(R.id.second_container);
        profileImage = itemView.findViewById(R.id.user_profile_image);
        container = itemView.findViewById(R.id.post_item_container);
        username = itemView.findViewById(R.id.post_username);
        postImg = itemView.findViewById(R.id.post_image);
        like_btn = itemView.findViewById(R.id.likeBtn);
        show_comments_btn = itemView.findViewById(R.id.show_comments_btn);
        like_counter_tv = itemView.findViewById(R.id.like_counter);
        openUserProfileView = itemView.findViewById(R.id.view_profile);
        likesRef.keepSynced(true);

        show_comments_btn.setOnClickListener(view -> showComments(postID, itemView));

        openUserProfileView.setOnClickListener(v -> {

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

        openUserProfileView.setOnLongClickListener(view -> {
            copyUsernameToClipboard();
            return false;
        });

        sContainer.setOnLongClickListener(v -> {

            if (!HomeActivity.anonymous) {
                FragmentManager manager = ((AppCompatActivity) context).getSupportFragmentManager();
                PostOptionsDialog optionsDialog = new PostOptionsDialog();
                optionsDialog.setPostId(postID);
                optionsDialog.setPostImage(postImg);
                optionsDialog.setPostSourceURL(postImageURL);
                optionsDialog.setPostType("image");
                optionsDialog.setAuthor(username.getText().toString().equals(user.getDisplayName()));
                optionsDialog.setAuthorName(username.getText().toString());
                optionsDialog.show(manager, "options");
            }

            return false;
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
                    Toast.makeText(context, "Error: " + error, Toast.LENGTH_LONG).show();
                }
            });
        });


    }

    private void showComments(String postID, View itemView) {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context, R.style.Theme_SocialMeme);
        View bottomSheetView = LayoutInflater.from(context.getApplicationContext())
                .inflate(R.layout.post_options_bottom_sheet,
                        (ConstraintLayout)itemView.findViewById(R.id.bottom_sheet_container));

        /*bottomSheetView.findViewById(R.id.imageButton11).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });*/
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();

    }
}