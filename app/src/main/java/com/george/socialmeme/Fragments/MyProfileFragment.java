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
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.george.socialmeme.R;
import com.george.socialmeme.Activities.HomeActivity;
import com.george.socialmeme.Activities.SettingsActivity;
import com.george.socialmeme.Adapters.PostRecyclerAdapter;
import com.george.socialmeme.Models.PostModel;
import com.github.loadingview.LoadingDialog;
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

    LoadingDialog progressDialog;
    public int followers = 0;
    public int following = 0;
    private Uri imgUri;
    CircleImageView img;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();
    DatabaseReference userRef;


    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {

                Intent data = result.getData();

                if (result.getResultCode() == Activity.RESULT_OK && data != null) {
                    imgUri = data.getData();
                    //img.setImageURI(imgUri);
                    uploadToFirebase(imgUri);
                }
            });

    private String getFileExtension(Uri mUri) {
        ContentResolver cr = getActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(mUri));
    }

    private void uploadToFirebase(Uri uri) {

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

            // update profile picture url on Firebase Real-Time DB
            userRef.child("profileImgUrl").setValue(uri1.toString());

            // update profile picture url on FirebaseAuth
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(uri1).build();
            user.updateProfile(profileUpdates);

            // update profile picture on all user posts
            DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("posts");
            postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
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

            getActivity().finish();
            startActivity(new Intent(getActivity(), HomeActivity.class));
            CustomIntent.customType(getActivity(), "left-to-right");

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

        CircleImageView profilePicture = view.findViewById(R.id.my_profile_image);
        TextView username = view.findViewById(R.id.username_my_profile);
        ImageButton settings = view.findViewById(R.id.settings_btn);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        TextView followersCounter = view.findViewById(R.id.followers_my_profile);
        TextView followingCounter = view.findViewById(R.id.following_my_profile);

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

        progressDialog = LoadingDialog.Companion.get(getActivity());


        profilePicture.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            someActivityResultLauncher.launch(intent);
        });


        if (!HomeActivity.anonymous) {

            progressDialog.show();

            userRef = FirebaseDatabase.getInstance().getReference("users").child(mAuth.getCurrentUser().getUid());

            // Set username
            username.setText(user.getDisplayName());

            // Load profile picture & following\followers counter
            usersRef.child(user.getUid()).addValueEventListener(new ValueEventListener() {
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

                    if (snapshot.child("profileImgUrl").exists() && !snapshot.child("profileImgUrl").getValue().toString().equals("none")) {
                        if (isAdded()) {
                            Glide.with(getContext()).load(snapshot.child("profileImgUrl").getValue().toString()).into(profilePicture);
                        }
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

                    progressDialog.hide();

                    for (DataSnapshot snap : snapshot.getChildren()) {

                        if (snap.child("name").getValue(String.class).equals(user.getDisplayName())) {
                            PostModel postModel = new PostModel();
                            postModel.setId(snap.child("id").getValue(String.class));
                            postModel.setImgUrl(snap.child("imgUrl").getValue(String.class));
                            postModel.setLikes(snap.child("likes").getValue(String.class));
                            postModel.setName(snap.child("name").getValue(String.class));
                            postModel.setPostType(snap.child("postType").getValue(String.class));

                            postModelArrayList.add(postModel);

                            recyclerAdapter.notifyDataSetChanged();
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }else {
            username.setText("Anonymous User");
            settings.setEnabled(false);
        }

        return view;
    }
}