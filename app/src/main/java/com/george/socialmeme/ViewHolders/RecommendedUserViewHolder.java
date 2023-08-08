package com.george.socialmeme.ViewHolders;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.george.socialmeme.Activities.HomeActivity;
import com.george.socialmeme.Activities.UserProfileActivity;
import com.george.socialmeme.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import de.hdodenhof.circleimageview.CircleImageView;
import maes.tech.intentanim.CustomIntent;

public class RecommendedUserViewHolder extends RecyclerView.ViewHolder {

    public Context context;
    public String recommendedUserID;
    public View openUserProfile;
    public CircleImageView profilePicture;
    public TextView usernameTV, memesCounterTV;
    public Button followButton;

    void followRecommendedUser() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.child(recommendedUserID).child("followers").child(user.getUid()).setValue(user.getUid());
        usersRef.child(user.getUid()).child("following").child(recommendedUserID).setValue(recommendedUserID);
        followButton.setEnabled(false);
        followButton.setText("Following");
    }

    public RecommendedUserViewHolder(@NonNull View itemView) {
        super(itemView);
        openUserProfile = itemView.findViewById(R.id.openRecommendedUserProfile);
        profilePicture = itemView.findViewById(R.id.user_profile_picture);
        usernameTV = itemView.findViewById(R.id.recommended_user_tv);
        memesCounterTV = itemView.findViewById(R.id.memes_counter);
        followButton = itemView.findViewById(R.id.follow_recommended_user_btn);
        followButton.setOnClickListener(v -> followRecommendedUser());

        openUserProfile.setOnClickListener(v -> {
            Intent intent = new Intent(context, UserProfileActivity.class);
            intent.putExtra("user_id", recommendedUserID);
            intent.putExtra("username", usernameTV.getText().toString());
            //intent.putExtra("allPosts", new Gson().toJson(HomeActivity.savedPostsArrayList));
            context.startActivity(intent);
            CustomIntent.customType(context, "left-to-right");
        });

    }

}
