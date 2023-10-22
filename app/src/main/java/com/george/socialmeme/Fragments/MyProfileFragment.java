package com.george.socialmeme.Fragments;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.developer.kalert.KAlertDialog;
import com.george.socialmeme.Activities.AllUserPostsActivity;
import com.george.socialmeme.Activities.FollowerInfoActivity;
import com.george.socialmeme.Activities.HomeActivity;
import com.george.socialmeme.Activities.PostsOfTheMonthActivity;
import com.george.socialmeme.Activities.SettingsActivity;
import com.george.socialmeme.Adapters.PostRecyclerAdapter;
import com.george.socialmeme.Models.PostModel;
import com.george.socialmeme.Models.UserModel;
import com.george.socialmeme.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
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
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

import de.hdodenhof.circleimageview.CircleImageView;
import maes.tech.intentanim.CustomIntent;
import smartdevelop.ir.eram.showcaseviewlib.GuideView;
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType;
import smartdevelop.ir.eram.showcaseviewlib.config.Gravity;


public class MyProfileFragment extends Fragment {

    KAlertDialog progressDialog;
    CircleImageView profilePicture;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();
    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    TextView followersCounter, followingCounter, goldTrophiesCount, silverTrophiesCount, bronzeTrophiesCount;
    ProgressBar progressBar;
    ArrayList<PostModel> postModelArrayList;
    private int followers = 0;
    PostRecyclerAdapter recyclerAdapter;
    private int following = 0;
    TextView username, totalmemes;
    ImageView verified;
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    SwipeRefreshLayout swipeRefreshLayout;

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

    boolean copyUsernameFeature() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("copy_my_username", MODE_PRIVATE);
        return sharedPref.getBoolean("copy_my_username", false);
    }

    boolean verifiedFeature() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("verified", MODE_PRIVATE);
        return sharedPref.getBoolean("verified", false);
    }

    private void uploadProfilePictureToFirebase(Uri uri) {

        progressDialog.show();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Delete old profile picture file from storage
        if (user.getPhotoUrl() != null) {
            try {
                storage.getReferenceFromUrl(user.getPhotoUrl().toString()).delete().addOnCompleteListener(task -> {
                    if (task.isCanceled()) {
                        Toast.makeText(getContext(), "Error: we cannot delete your current profile picture", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception error) {
                Log.i("MY_PROFILE_FRAGMENT", error.getMessage());
            }

        }

        // Upload new profile picture to storage
        StorageReference fileRef = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(uri));
        fileRef.putFile(uri).addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri1 -> {

            // Update profile picture URL on Firebase Real-Time DB
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
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
                            if (snap.child("name").getValue(String.class) != null &&
                                    snap.child("id").getValue(String.class) != null)
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

    void getUserDataFromDB(FirebaseCallback firebaseCallback)   {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                UserModel userDataForSave = new UserModel();
                
                if (snapshot.child("users").child(user.getUid()).child("following").exists()) {
                    following = (int) snapshot.child("users").child(user.getUid()).child("following").getChildrenCount();
                    followingCounter.setText(String.format("%d", following));
                    userDataForSave.setFollowing(String.format("%d", following));
                } else {
                    userDataForSave.setFollowing("0");
                }

                if (snapshot.child("users").child(user.getUid()).child("followers").exists()) {
                    followers = (int) snapshot.child("users").child(user.getUid()).child("followers").getChildrenCount();
                    followersCounter.setText(String.format("%d", followers));
                    userDataForSave.setFollowers(String.format("%d", followers));
                }else {
                    userDataForSave.setFollowers("0");
                }

                if (user.getPhotoUrl() != null) {
                    if (isAdded()) {
                        Glide.with(getContext()).load(user.getPhotoUrl().toString()).into(profilePicture);
                    }
                    userDataForSave.setProfilePictureURL(user.getPhotoUrl().toString());
                }else {
                    userDataForSave.setProfilePictureURL("none");
                }

                if (snapshot.child("users").child(user.getUid()).child("trophies").exists()) {


                    String goldTrophies = snapshot.child("users")
                            .child(user.getUid()).child("trophies").child("gold").getValue(String.class);
                    String silverTrophies = snapshot.child("users")
                            .child(user.getUid()).child("trophies").child("silver").getValue(String.class);
                    String bronzeTrophies = snapshot.child("users")
                            .child(user.getUid()).child("trophies").child("bronze").getValue(String.class);

                    goldTrophiesCount.setText(goldTrophies);
                    silverTrophiesCount.setText(silverTrophies);
                    bronzeTrophiesCount.setText(bronzeTrophies);

                    userDataForSave.setGoldTrophiesCounter(goldTrophies);
                    userDataForSave.setSilverTrophiesCounter(silverTrophies);
                    userDataForSave.setBronzeTrophiesCounter(bronzeTrophies);

                }

                progressBar.setVisibility(View.GONE);
                firebaseCallback.onComplete();

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public interface FirebaseCallback {
        void onComplete();
    }

    void copyUsernameToClipboard(String username) {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("username", username);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getActivity(), "Username copied to clipboard", Toast.LENGTH_SHORT).show();
    }
    PostsLoadedCallback postsLoadedCallback = new PostsLoadedCallback() {
        @Override
        public void onComplete() {

            progressDialog.hide();
            recyclerView.setAdapter(null);
            recyclerAdapter.notifyDataSetChanged();
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(recyclerAdapter);

            Collections.reverse(postModelArrayList);

            ArrayList<PostModel> allUserPosts = new ArrayList<>();
            int totalPosts = 0;

            for (PostModel post : postModelArrayList) {
                if (totalPosts < 5) {
                    totalPosts++;
                    allUserPosts.add(post);
                }
            }

            Collections.reverse(allUserPosts);

            recyclerAdapter = new PostRecyclerAdapter(allUserPosts, getContext(), getActivity());
            recyclerAdapter.notifyDataSetChanged();

            recyclerView.setAdapter(null);
            recyclerAdapter.notifyDataSetChanged();
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(recyclerAdapter);

            int totalMemesToInt = postModelArrayList.size();
            String totalMemesToString = Integer.toString(totalMemesToInt);
            totalmemes.setText(totalMemesToString + " total memes!");

            Toast.makeText(getContext(), "User data refreshed", Toast.LENGTH_SHORT).show();

        }
    };

    void refreshUserInfoAndPosts(SwipeRefreshLayout refreshLayout) {

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        String userID = user.getUid();

        // calling postModelArrayList.clear()
        // here will crash the main thread
        // so we re-initialize the variable to empty the list
        postModelArrayList = new ArrayList<>();

        refreshLayout.setRefreshing(true);

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference();

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.child("users").child(userID).child("name").getValue(String.class) != null) {

                    if (snapshot.child("name").getValue(String.class) != null && snapshot.hasChild("verified")) {
                        verified.setVisibility(View.VISIBLE);
                    }

                    String profilePictureURL = "none";
                    //String username_from_db = snapshot.child("users").child(userID).child("name").getValue(String.class);
                    username.setText(user.getDisplayName());

                    if (snapshot.child("users").child(userID).child("profileImgUrl").exists()) {
                        profilePictureURL = snapshot.child("users").child(userID).child("profileImgUrl").getValue(String.class);
                    }

                    if (!profilePictureURL.equals("none")) {
                        Glide.with(getContext()).load(profilePictureURL).into(profilePicture);
                    }

                    // set followers and following counter values
                    if (snapshot.child("users").child(userID).child("following").exists()) {
                        following = (int) snapshot.child("users").child(userID).child("following").getChildrenCount();
                        followingCounter.setText(String.format("%d", following));
                    }

                    if (snapshot.child("users").child(userID).child("followers").exists()) {
                        followers = (int) snapshot.child("users").child(userID).child("followers").getChildrenCount();
                        followersCounter.setText(String.format("%d", followers));
                    }

                    // Load user trophies
                    if (snapshot.child("users").child(userID).child("trophies").exists()) {
                        String goldTrophies = snapshot.child("users").child(userID).child("trophies").child("gold").getValue(String.class);
                        String silverTrophies = snapshot.child("users").child(userID).child("trophies").child("silver").getValue(String.class);
                        String bronzeTrophies = snapshot.child("users").child(userID).child("trophies").child("bronze").getValue(String.class);

                        goldTrophiesCount.setText(goldTrophies);
                        silverTrophiesCount.setText(silverTrophies);
                        bronzeTrophiesCount.setText(bronzeTrophies);
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error while refreshing user data", Toast.LENGTH_SHORT).show();
            }
        });

        // Load user posts from db
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference();
        postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // Empty HomeFragment.postModelArrayList
                // so we can add our new data fetched from db
                HomeFragment.postModelArrayList.clear();

                if (postModelArrayList == null) {
                    postModelArrayList = new ArrayList<>();
                } else {
                    postModelArrayList.clear();
                }
                recyclerAdapter.notifyDataSetChanged();

                for (DataSnapshot postSnapshot : snapshot.child("posts").getChildren()) {

                    if (postSnapshot.child("name").getValue(String.class) != null /*&& postSnapshot.child("name").getValue(String.class).equals(user.getDisplayName())*/ && !postSnapshot.child("reported").exists()) {

                        if (!HomeActivity.singedInAnonymously && postSnapshot.child("name").getValue(String.class).equals(user.getDisplayName())) {
                            HomeActivity.userHasPosts = true;
                        }

                        PostModel postModel = postSnapshot.getValue(PostModel.class);

                        if (postModel.getComments() != null) {
                            postModel.setCommentsCount(String.valueOf(postSnapshot.child("comments").getChildrenCount()));
                        } else {
                            postModel.setCommentsCount("0");
                        }

                        if (!HomeActivity.singedInAnonymously) {
                            if (postSnapshot.child("name").getValue(String.class).equals(user.getDisplayName())) {
                                postModelArrayList.add(postModel);
                            }
                        } else {
                            postModelArrayList.add(postModel);
                        }
                        HomeFragment.postModelArrayList.add(postModel);
                    }

                }

                Collections.reverse(HomeFragment.postModelArrayList);
                refreshLayout.setRefreshing(false);
                //recyclerAdapter.notifyDataSetChanged();
                postsLoadedCallback.onComplete();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_profile, container, false);

        ImageView badge1 = view.findViewById(R.id.imageView14);
        ImageView badge2 = view.findViewById(R.id.imageView15);
        ImageView badge3 = view.findViewById(R.id.imageView24);
        ImageView verified = view.findViewById(R.id.imageView26);
        TextView verify_msg = view.findViewById(R.id.verified);
        verified.setVisibility(View.INVISIBLE);
        ImageView crown = view.findViewById(R.id.crown2);
        crown.setVisibility(View.GONE);

        profilePicture = view.findViewById(R.id.my_profile_image);
        username = view.findViewById(R.id.username_my_profile);
        ImageButton settings = view.findViewById(R.id.settings_btn);
        View showFollowersView = view.findViewById(R.id.showFollowersView_Profile);
        View showFollowingUsersView = view.findViewById(R.id.showFollowingView_Profile);
        ImageButton postsOfTheMonthInfo = view.findViewById(R.id.imageButton9);
        ImageButton badges = view.findViewById(R.id.imageButton22);
        Button allPostsBtn = view.findViewById(R.id.button5);
        progressBar = view.findViewById(R.id.progressBar);
        totalmemes = view.findViewById(R.id.my_total_memes);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        AdView mAdView = view.findViewById(R.id.adView5);
        AdRequest adRequest = new AdRequest.Builder().build();

        String email = "none";
        String finalEmail;

        if (!HomeActivity.singedInAnonymously) {
            email = user.getEmail();
        }

        finalEmail = email;

        verified.setOnClickListener(v -> {
            AlertDialog donateDialog = new AlertDialog.Builder(getContext())
                    .setTitle("You are verified")
                    .setMessage("You have a verified badge because your email address is verified")
                    .setPositiveButton("That's cool!", (dialogInterface, i) ->
                    {
                        dialogInterface.dismiss();
                    })
                    .setCancelable(false)
                    .setIcon(R.drawable.verify)
                    .create();
            donateDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            donateDialog.getWindow().setBackgroundDrawableResource(R.drawable.custom_dialog_background);
            donateDialog.show();
        });

        verify_msg.setOnClickListener(v -> {
            if (user != null && !user.isEmailVerified()) {
                AlertDialog donateDialog = new AlertDialog.Builder(getContext())
                        .setTitle("Verify your email")
                        .setMessage("By verifying your email a blue verify badge will be visible next to your username to other users")
                        .setPositiveButton("Send email", (dialogInterface, i) ->
                        {
                            user.sendEmailVerification();
                            AlertDialog donateDialog1 = new AlertDialog.Builder(getContext())
                                    .setTitle("Email Sent!")
                                    .setMessage("The verification email has been sent to " + finalEmail)
                                    .setPositiveButton("Okay", (dialogInterface1, i1) ->
                                    {
                                        dialogInterface1.dismiss();
                                    })
                                    .setCancelable(false)
                                    .setIcon(R.drawable.verify)
                                    .create();
                            donateDialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            donateDialog1.getWindow().setBackgroundDrawableResource(R.drawable.custom_dialog_background);
                            donateDialog1.show();
                        })
                        .setNegativeButton("No, thanks", (dialogInterface, i) -> dialogInterface.dismiss())
                        .setCancelable(false)
                        .setIcon(R.drawable.verify)
                        .create();
                donateDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                donateDialog.getWindow().setBackgroundDrawableResource(R.drawable.custom_dialog_background);
                donateDialog.show();
            } else {
                Toast.makeText(getContext(), "You are verified!", Toast.LENGTH_SHORT).show();
                verified.setVisibility(View.VISIBLE);
                verify_msg.setVisibility(View.GONE);
                rootRef.child("users").child(user.getUid()).child("verified").setValue("true");
            }
        });

        if (user != null && user.isEmailVerified()) {
            verified.setVisibility(View.VISIBLE);
            verify_msg.setVisibility(View.GONE);
            rootRef.child("users").child(user.getUid()).child("verified").setValue("true");
        } else {
            if (user != null) {
                int randomNum = ThreadLocalRandom.current().nextInt(0, 7 + 1);
                if (randomNum == 5) {
                    AlertDialog donateDialog = new AlertDialog.Builder(getContext())
                            .setTitle("Verify your email")
                            .setMessage("By verifying your email a blue verify badge will be visible next to your username to other users")
                            .setPositiveButton("Send email", (dialogInterface, i) ->
                            {
                                user.sendEmailVerification();
                                AlertDialog donateDialog1 = new AlertDialog.Builder(getContext())
                                        .setTitle("Email Sent!")
                                        .setMessage("The verification email has been sent to " + finalEmail)
                                        .setPositiveButton("Okay", (dialogInterface1, i1) ->
                                        {
                                            dialogInterface1.dismiss();
                                        })
                                        .setCancelable(false)
                                        .setIcon(R.drawable.verify)
                                        .create();
                                donateDialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                donateDialog1.getWindow().setBackgroundDrawableResource(R.drawable.custom_dialog_background);
                                donateDialog1.show();
                            })
                            .setNegativeButton("No, thanks", (dialogInterface, i) -> dialogInterface.dismiss())
                            .setCancelable(false)
                            .setIcon(R.drawable.verify)
                            .create();
                    donateDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    donateDialog.getWindow().setBackgroundDrawableResource(R.drawable.custom_dialog_background);
                    donateDialog.show();
                }
            }
        }

        if (HomeActivity.show_banners) {
            mAdView.loadAd(adRequest);
        }

        followersCounter = view.findViewById(R.id.followers_my_profile);
        followingCounter = view.findViewById(R.id.following_my_profile);
        TextView totalLikesCounter = view.findViewById(R.id.textView76);

        goldTrophiesCount = view.findViewById(R.id.gold_trophies_count);
        silverTrophiesCount = view.findViewById(R.id.silver_trophies_count);
        bronzeTrophiesCount = view.findViewById(R.id.bronze_trophies_count);

        badges.setOnClickListener(v -> {
            AlertDialog dialog = new AlertDialog.Builder(getContext())
                    .setTitle("How to earn badges!")
                    .setMessage(R.string.badges)
                    .setIcon(R.drawable.badge)
                    .setPositiveButton("OK", (dialog1, which) -> {
                        dialog1.dismiss();
                    }).show();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.custom_dialog_background);
            dialog.show();
        });

        username.setOnClickListener(v -> copyUsernameToClipboard(user.getDisplayName()));

        if (!copyUsernameFeature()) {

            SharedPreferences sharedPref = getActivity().getSharedPreferences("copy_my_username", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("copy_my_username", true);
            editor.apply();

            new GuideView.Builder(getContext())
                    .setTitle("Copy your username")
                    .setContentText("Copy your username by clicking here")
                    .setTargetView(username)
                    .setDismissType(DismissType.targetView)
                    .setGravity(Gravity.center)
                    .build()
                    .show();
        }

        if (!verifiedFeature() && user.isEmailVerified()) {

            SharedPreferences sharedPref = getActivity().getSharedPreferences("verified", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("verified", true);
            editor.apply();

            AlertDialog donateDialog = new AlertDialog.Builder(getContext())
                    .setTitle("You are verified")
                    .setMessage("You have a verified badge because your email address is verified")
                    .setPositiveButton("That's cool!", (dialogInterface, i) ->
                    {
                        dialogInterface.dismiss();
                    })
                    .setCancelable(false)
                    .setIcon(R.drawable.verify)
                    .create();
            donateDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            donateDialog.getWindow().setBackgroundDrawableResource(R.drawable.custom_dialog_background);
            donateDialog.show();

        }

        if (!HomeActivity.singedInAnonymously) username.setText(user.getDisplayName());
        else {
            getActivity().finish();
            HomeActivity.singedInAnonymously = true;
            startActivity(new Intent(getActivity(), HomeActivity.class));
            CustomIntent.customType(getActivity(), "fadein-to-fadeout");
        }

        allPostsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AllUserPostsActivity.class);
            intent.putExtra("userID", user.getUid());
            intent.putExtra("reverse_list", true);
            startActivity(intent);
            CustomIntent.customType(getContext(), "left-to-right");
        });

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

        postModelArrayList = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recyclerView_my_profile);
        layoutManager = new LinearLayoutManager(getContext());

        recyclerAdapter = new PostRecyclerAdapter(postModelArrayList, getContext(), getActivity());

        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setHasFixedSize(true);
        layoutManager.setReverseLayout(true);
        layoutManager.setSmoothScrollbarEnabled(true);
        recyclerView.setLayoutManager(layoutManager);

        progressDialog = new KAlertDialog(getContext(), KAlertDialog.PROGRESS_TYPE);
        progressDialog.getProgressHelper().setBarColor(R.color.main);
        progressDialog.setTitleText("Updating profile picture...");
        progressDialog.setCancelable(false);

        swipeRefreshLayout = view.findViewById(R.id.my_profile_refresh);
        swipeRefreshLayout.setOnRefreshListener(() -> refreshUserInfoAndPosts(swipeRefreshLayout));

        showFollowersView.setOnClickListener(view1 -> {
            if (!followersCounter.getText().toString().equals("0")) {
                Intent intent = new Intent(getActivity(), FollowerInfoActivity.class);
                intent.putExtra("userID", user.getUid());
                intent.putExtra("display_followers", true);
                startActivity(intent);
                CustomIntent.customType(getActivity(), "left-to-right");
            } else {
                Toast.makeText(getActivity(), "You don't have any followers", Toast.LENGTH_SHORT).show();
            }
        });

        showFollowingUsersView.setOnClickListener(view12 -> {
            if (!followingCounter.getText().toString().equals("0")) {
                Intent intent = new Intent(getActivity(), FollowerInfoActivity.class);
                intent.putExtra("userID", user.getUid());
                intent.putExtra("display_followers", false);
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

        if (!HomeActivity.singedInAnonymously && HomeFragment.postModelArrayList != null) {

            ArrayList<PostModel> reversedPosts = HomeFragment.postModelArrayList;

            ArrayList<String> loadedPostsID = new ArrayList<>();
            int postsCount = 0;
                for (PostModel post : reversedPosts) {
                    if (postsCount <= 4) {
                        if (post.getName() != null) {
                            if (post.getName().equals(user.getDisplayName()) && !loadedPostsID.contains(post.getId())) {
                                loadedPostsID.add(post.getId());
                                postModelArrayList.add(post);
                                postsCount++;
                            }
                        }
                    }
                }

            Collections.reverse(postModelArrayList);

            ArrayList<String> loadedPostsID1 = new ArrayList<>();
            int totalLikes = 0;
            for (PostModel post : HomeFragment.postModelArrayList) {
                if (post.getName() != null) {
                    if (post.getName().equals(user.getDisplayName()) && !loadedPostsID1.contains(post.getId())) {
                        loadedPostsID1.add(post.getId());
                        int likesToInt = Integer.parseInt(post.getLikes());
                        totalLikes += likesToInt;
                    }
                }
            }

            int totalMemesToInt = loadedPostsID1.size();
            String totalMemesToString = String.valueOf(totalMemesToInt);
            totalmemes.setText(totalMemesToString + " total memes!");

            if (totalLikes >= 20000) {
                badge1.setAlpha(1F);
            } else {
                badge1.setAlpha(.3F);
            }

            if (totalLikes >= 35000) {
                badge2.setAlpha(1F);
            } else {
                badge2.setAlpha(.3F);
            }

            if (totalLikes >= 50000) {
                badge3.setAlpha(1F);
            } else {
                badge3.setAlpha(.3F);
            }

            if (totalLikes >= 100000 || user.getUid().equals("HMQ6OPjzhuSsdQy848N1L0XNztH3")) {
                crown.setVisibility(View.VISIBLE);
            }

            totalLikesCounter.setText(HomeActivity.prettyCount(totalLikes));

            if (HomeActivity.savedUserData != null) {

                // Load saved user data
                String followingCounter_saved = HomeActivity.savedUserData.getFollowing();
                String followersCounter_saved = HomeActivity.savedUserData.getFollowers();
                String goldTrophies_saved = HomeActivity.savedUserData.getGoldTrophiesCounter();
                String silverTrophies_saved = HomeActivity.savedUserData.getSilverTrophiesCounter();
                String bronzeTrophies_saved = HomeActivity.savedUserData.getBronzeTrophiesCounter();

                followingCounter.setText("0");
                followersCounter.setText("0");


                totalLikesCounter.setText(HomeActivity.prettyCount(totalLikes));
                followingCounter.setText(followingCounter_saved);
                followersCounter.setText(followersCounter_saved);
                goldTrophiesCount.setText(goldTrophies_saved);
                silverTrophiesCount.setText(silverTrophies_saved);
                bronzeTrophiesCount.setText(bronzeTrophies_saved);

                if (user.getPhotoUrl() != null) {
                    Glide.with(getContext()).load(user.getPhotoUrl()).into(profilePicture);
                }

            } else {

                followingCounter.setText("0");
                followersCounter.setText("0");

                getUserDataFromDB(() -> {
                    UserModel userModel = new UserModel();
                    userModel.setFollowing(followingCounter.getText().toString());
                    userModel.setFollowers(followersCounter.getText().toString());
                    userModel.setTotalLikes(totalLikesCounter.getText().toString());
                    userModel.setGoldTrophiesCounter(goldTrophiesCount.getText().toString());
                    userModel.setSilverTrophiesCounter(silverTrophiesCount.getText().toString());
                    userModel.setBronzeTrophiesCounter(bronzeTrophiesCount.getText().toString());
                    HomeActivity.savedUserData = userModel;

                });
            }

            progressBar.setVisibility(View.GONE);

        } else {
            progressBar.setVisibility(View.GONE);
            username.setText("Anonymous User");
            settings.setVisibility(View.GONE);
            profilePicture.setEnabled(false);
        }

        return view;
    }

    public interface PostsLoadedCallback {
        void onComplete();
    }
}

