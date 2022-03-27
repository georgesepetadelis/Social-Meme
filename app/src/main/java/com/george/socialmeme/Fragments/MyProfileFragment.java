package com.george.socialmeme.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.developer.kalert.KAlertDialog;
import com.george.socialmeme.Activities.FollowerInfoActivity;
import com.george.socialmeme.Activities.LoginActivity;
import com.george.socialmeme.Activities.PostsOfTheMonthActivity;
import com.george.socialmeme.Activities.UserProfileActivity;
import com.george.socialmeme.R;
import com.george.socialmeme.Activities.HomeActivity;
import com.george.socialmeme.Activities.SettingsActivity;
import com.george.socialmeme.Adapters.PostRecyclerAdapter;
import com.george.socialmeme.Models.PostModel;
import com.github.loadingview.LoadingDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import maes.tech.intentanim.CustomIntent;


public class MyProfileFragment extends Fragment {

    KAlertDialog progressDialog;
    CircleImageView profilePicture;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();
    DatabaseReference userRef;

    private int followers = 0;
    private int following = 0;

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Intent data = result.getData();
                if (result.getResultCode() == Activity.RESULT_OK && data != null) {
                    Uri imgUri = data.getData();
                    profilePicture.setImageURI(imgUri);
                    uploadProfilePictureToFirebase(imgUri);
                }
            });

    private String getFileExtension(Uri mUri) {
        ContentResolver cr = getActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(mUri));
    }

    private void uploadProfilePictureToFirebase(Uri uri) {

        // Show loading dialog
        progressDialog.show();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user.getPhotoUrl() != null) {
            storage.getReferenceFromUrl(user.getPhotoUrl().toString()).delete().addOnCompleteListener(task -> {
                if (task.isCanceled()) {
                    Toast.makeText(getContext(), "Error: we cannot delete your current profile picture", Toast.LENGTH_SHORT).show();
                }
            });
        }

        StorageReference fileRef = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(uri));
        fileRef.putFile(uri).addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri1 -> {

            // Update profile picture URL on Firebase Real-Time DB
            userRef.child("profileImgUrl").setValue(uri1.toString());

            // Update profile picture URL on FirebaseAuth
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(uri1).build();
            user.updateProfile(profileUpdates).addOnCompleteListener(task -> {

                // Update profile picture URL on all user posts
                DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("posts");
                postsRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            if (snap.child("name").getValue(String.class).equals(user.getDisplayName())) {
                                postsRef.child(snap.child("id").getValue(String.class)).child("authorProfilePictureURL").setValue(user.getPhotoUrl().toString());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getActivity(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            });

            Toast.makeText(getActivity(), "Profile picture updated!", Toast.LENGTH_SHORT).show();

        })).addOnFailureListener(e -> Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()).addOnCompleteListener(task -> {
            progressDialog.hide();
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_profile, container, false);

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        profilePicture = view.findViewById(R.id.my_profile_image);
        TextView username = view.findViewById(R.id.username_my_profile);
        ImageButton settings = view.findViewById(R.id.settings_btn);
        View showFollowersView = view.findViewById(R.id.showFollowersView_Profile);
        View showFollowingUsersView = view.findViewById(R.id.showFollowingView_Profile);
        ImageButton postsOfTheMonthInfo = view.findViewById(R.id.imageButton9);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        AdView mAdView = view.findViewById(R.id.adView5);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        TextView followersCounter = view.findViewById(R.id.followers_my_profile);
        TextView followingCounter = view.findViewById(R.id.following_my_profile);
        TextView totalLikesCounter = view.findViewById(R.id.textView76);

        TextView goldTrophiesCount = view.findViewById(R.id.gold_trophies_count);
        TextView silverTrophiesCount = view.findViewById(R.id.silver_trophies_count);
        TextView bronzeTrophiesCount = view.findViewById(R.id.bronze_trophies_count);

        postsOfTheMonthInfo.setOnClickListener(view13 -> {
            Intent intent = new Intent(getContext(), PostsOfTheMonthActivity.class);
            startActivity(intent);
            CustomIntent.customType(getContext(), "left-to-right");
        });

        settings.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
            CustomIntent.customType(getActivity(), "left-to-right");
        });

        if (!isAdded()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        final RecyclerView recyclerView = view.findViewById(R.id.recyclerView_my_profile);
        final ArrayList<PostModel> postModelArrayList = new ArrayList<>();
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        final RecyclerView.Adapter recyclerAdapter = new PostRecyclerAdapter(postModelArrayList, getContext(), getActivity());
        final DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("posts");

        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        progressDialog = new KAlertDialog(getContext(), KAlertDialog.PROGRESS_TYPE);
        progressDialog.getProgressHelper().setBarColor(R.color.main);
        progressDialog.setTitleText("Updating profile picture...");
        progressDialog.setCancelable(false);

        showFollowersView.setOnClickListener(view1 -> {
            if (followers != 0) {
                FollowerInfoActivity.userID = user.getUid();
                FollowerInfoActivity.displayFollowers = true;
                Intent intent = new Intent(getActivity(), FollowerInfoActivity.class);
                startActivity(intent);
                CustomIntent.customType(getActivity(), "left-to-right");
            }
        });

        showFollowingUsersView.setOnClickListener(view12 -> {
            if (following != 0) {
                FollowerInfoActivity.userID = user.getUid();
                FollowerInfoActivity.displayFollowers = false;
                Intent intent = new Intent(getActivity(), FollowerInfoActivity.class);
                startActivity(intent);
                CustomIntent.customType(getActivity(), "left-to-right");
            }
        });

        profilePicture.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            someActivityResultLauncher.launch(intent);
        });


        if (!HomeActivity.anonymous) {

            userRef = FirebaseDatabase.getInstance().getReference("users").child(mAuth.getCurrentUser().getUid());

            // Set username
            username.setText(user.getDisplayName());

            // Load profile picture & following\followers counter and user trophies
            usersRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @SuppressLint("DefaultLocale")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    // set followers and following counter values
                    if (snapshot.child("following").exists()) {
                        following = (int) snapshot.child("following").getChildrenCount();
                        followingCounter.setText(String.format("%d", following));
                    }

                    if (snapshot.child("followers").exists()) {
                        followers = (int) snapshot.child("followers").getChildrenCount();
                        followersCounter.setText(String.format("%d", followers));
                    }

                    if (user.getPhotoUrl() != null) {
                        Glide.with(getActivity()).load(user.getPhotoUrl().toString()).into(profilePicture);
                    }

                    if (snapshot.child("trophies").exists()) {

                        String goldTrophies = snapshot.child(user.getUid()).child("trophies").child("gold").getValue(String.class);
                        String silverTrophies = snapshot.child(user.getUid()).child("trophies").child("silver").getValue(String.class);
                        String bronzeTrophies = snapshot.child(user.getUid()).child("trophies").child("bronze").getValue(String.class);

                        goldTrophiesCount.setText(goldTrophies);
                        silverTrophiesCount.setText(silverTrophies);
                        bronzeTrophiesCount.setText(bronzeTrophies);

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                    int totalLikes = 0;

                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {

                        if (postSnapshot.child("name").getValue(String.class).equals(user.getDisplayName())) {
                            PostModel postModel = new PostModel();
                            postModel.setId(postSnapshot.child("id").getValue(String.class));
                            postModel.setImgUrl(postSnapshot.child("imgUrl").getValue(String.class));
                            postModel.setLikes(postSnapshot.child("likes").getValue(String.class));
                            postModel.setName(postSnapshot.child("name").getValue(String.class));
                            postModel.setPostType(postSnapshot.child("postType").getValue(String.class));

                            for (DataSnapshot user : snapshot.child("users").getChildren()) {
                                if (user.child("name").getValue(String.class).equals(postSnapshot.child("name").getValue(String.class))) {
                                    postModel.setAuthorID(user.child("id").getValue(String.class));
                                }
                            }

                            if (postSnapshot.child("postType").getValue(String.class).equals("text")) {
                                postModel.setPostTitle(postSnapshot.child("joke_title").getValue(String.class));
                                postModel.setPostContentText(postSnapshot.child("joke_content").getValue(String.class));
                            }

                            totalLikes += Integer.parseInt(postSnapshot.child("likes").getValue(String.class));

                            if (postSnapshot.child("comments").exists()) {
                                postModel.setCommentsCount(String.valueOf(postSnapshot.child("comments").getChildrenCount()));
                            }else {
                                postModel.setCommentsCount("0");
                            }

                            // Set total likes
                            totalLikesCounter.setText(String.valueOf(totalLikes));

                            postModelArrayList.add(postModel);
                            recyclerAdapter.notifyDataSetChanged();
                        }
                    }

                    progressBar.setVisibility(View.GONE);

                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }else {
            progressBar.setVisibility(View.GONE);
            username.setText("Anonymous User");
            settings.setVisibility(View.GONE);
            profilePicture.setEnabled(false);
        }

        return view;
    }
}