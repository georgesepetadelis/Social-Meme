package com.george.socialmeme.Fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.george.socialmeme.Activities.HomeActivity;
import com.george.socialmeme.Activities.NotificationsActivity;
import com.george.socialmeme.Activities.UserProfileActivity;
import com.george.socialmeme.R;
import com.george.socialmeme.Adapters.PostRecyclerAdapter;
import com.george.socialmeme.Models.PostModel;
import com.github.loadingview.LoadingDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import maes.tech.intentanim.CustomIntent;

public class HomeFragment extends Fragment {

    LoadingDialog progressDialog;
    final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    boolean isSearchOpen = false;
    final private DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
    private EditText searchView;
    

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        MobileAds.initialize(getContext(), initializationStatus -> {System.out.println("ADS: Home ad init completed");});

        ImageButton searchUserBtn = view.findViewById(R.id.searchPersonButton);
        ImageButton notificationsBtn = view.findViewById(R.id.notificationsButton);

        searchView = view.findViewById(R.id.search_view);
        ImageButton searchUserButton = view.findViewById(R.id.enter_search_button);
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        AdView mAdView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();

        if (isAdded()) {
            mAdView.loadAd(adRequest);
        }

        if (HomeActivity.anonymous) {
            searchUserBtn.setVisibility(View.GONE);
            notificationsBtn.setVisibility(View.GONE);
        }

        final RecyclerView recyclerView = view.findViewById(R.id.home_recycler_view);
        final ArrayList<PostModel> postModelArrayList = new ArrayList<>();
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        final RecyclerView.Adapter recyclerAdapter = new PostRecyclerAdapter(postModelArrayList, getContext(), getActivity());

        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        progressDialog = LoadingDialog.Companion.get(getActivity());

        searchView.setVisibility(View.GONE);
        searchUserButton.setVisibility(View.GONE);

        progressDialog.show();

        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                progressDialog.hide();

                for (DataSnapshot snap : snapshot.child("posts").getChildren()) {

                    PostModel postModel = new PostModel();
                    postModel.setId(snap.child("id").getValue(String.class));
                    postModel.setImgUrl(snap.child("imgUrl").getValue(String.class));
                    postModel.setLikes(snap.child("likes").getValue(String.class));
                    postModel.setName(snap.child("name").getValue(String.class));
                    postModel.setProfileImgUrl(snap.child("authorProfilePictureURL").getValue(String.class));
                    postModel.setPostType(snap.child("postType").getValue(String.class));

                    postModelArrayList.add(postModel);

                }

                recyclerAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        searchUserButton.setOnClickListener(v -> usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                boolean userFound = false;

                for (DataSnapshot snap : snapshot.getChildren()) {
                    if (snap.child("name").getValue().toString().equals(searchView.getText().toString())) {

                        userFound = true;
                        UserProfileActivity.username = searchView.getText().toString();
                        UserProfileActivity.userID = snap.child("id").getValue().toString();

                        Intent intent = new Intent(getActivity(), UserProfileActivity.class);
                        getActivity().startActivity(intent);
                        CustomIntent.customType(getActivity(), "left-to-right");

                        break;
                    }
                }

                if (!userFound) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("User not found")
                            .setMessage("We cannot find a user with this username")
                            .setPositiveButton("ok", (dialog, which) -> dialog.dismiss())
                            .show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }));

        notificationsBtn.setOnClickListener(view12 -> {
            startActivity(new Intent(getActivity(), NotificationsActivity.class));
            CustomIntent.customType(getContext(), "left-to-right");
        });

        searchUserBtn.setOnClickListener(view1 -> {

            if (isSearchOpen) {
                // search is closed
                searchUserBtn.setImageResource(R.drawable.ic_search);
                isSearchOpen = false;

                // animate button
                YoYo.with(Techniques.FadeInUp)
                        .duration(500)
                        .repeat(0)
                        .playOn(searchUserBtn);

                // hide and animate search view
                YoYo.with(Techniques.FadeInUp)
                        .duration(500)
                        .repeat(0)
                        .playOn(searchView);

                YoYo.with(Techniques.FadeInUp)
                        .duration(500)
                        .repeat(0)
                        .playOn(searchUserButton);

                searchView.setVisibility(View.GONE);
                searchUserButton.setVisibility(View.GONE);

            }else {
                // search is open
                searchUserBtn.setImageResource(R.drawable.ic_close);
                isSearchOpen = true;

                // animate button
                YoYo.with(Techniques.FadeInDown)
                        .duration(500)
                        .repeat(0)
                        .playOn(searchUserBtn);

                // show and animate search view
                searchView.setVisibility(View.VISIBLE);
                searchUserButton.setVisibility(View.VISIBLE);

                YoYo.with(Techniques.FadeInDown)
                        .duration(500)
                        .repeat(0)
                        .playOn(searchView);

                YoYo.with(Techniques.FadeInDown)
                        .duration(500)
                        .repeat(0)
                        .playOn(searchUserButton);

                // display keyboard to type
                ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                        .showSoftInput(searchView, InputMethodManager.SHOW_FORCED);

            }

        });

        return view;
    }

    private void sendProfileVisitNotification() {

        String notificationID = usersRef.push().getKey();
        String currentDate = String.valueOf(android.text.format.DateFormat.format("dd-MM-yyyy", new java.util.Date()));
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot1) {

                for (DataSnapshot userSnapshot : snapshot1.getChildren()) {

                    if (userSnapshot.child("name").getValue().toString().equals(searchView.getText().toString())) {
                        Toast.makeText(getContext(), "User found", Toast.LENGTH_SHORT).show();
                        String postAuthorID = userSnapshot.child("id").getValue().toString();
                        usersRef.child(postAuthorID).child("notifications").child(notificationID).child("title").setValue("New profile visitor");
                        usersRef.child(postAuthorID).child("notifications").child(notificationID).child("type").setValue("profile_visit");
                        usersRef.child(postAuthorID).child("notifications").child(notificationID).child("date").setValue(currentDate);
                        usersRef.child(postAuthorID).child("notifications").child(notificationID).child("message").setValue(user.getDisplayName() + " visited your profile");
                        break;
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
