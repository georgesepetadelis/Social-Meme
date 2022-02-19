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

import android.os.Handler;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.george.socialmeme.Activities.HomeActivity;
import com.george.socialmeme.Activities.NotificationsActivity;
import com.george.socialmeme.Activities.PostsOfTheMonthActivity;
import com.george.socialmeme.Activities.SplashScreenActivity;
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
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import maes.tech.intentanim.CustomIntent;

public class HomeFragment extends Fragment {

    LoadingDialog progressDialog;
    final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    boolean isSearchOpen = false;
    private EditText searchView;
    ArrayList<PostModel> postModelArrayList;
    RecyclerView recyclerView;

    void loadPosts(View fragmentView, FirebaseUser user, ArrayList<PostModel> postModelArrayList, RecyclerView.Adapter postsAdapter, boolean isRefresh) {

        if (isRefresh) {
            fragmentView.findViewById(R.id.constraintLayout2).setVisibility(View.GONE);
            ((HomeActivity)getContext()).findViewById(R.id.bottom_nav).setVisibility(View.VISIBLE);
        }

        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                new Handler().postDelayed(() -> {
                    fragmentView.findViewById(R.id.constraintLayout2).setVisibility(View.GONE);
                    ((HomeActivity)getContext()).findViewById(R.id.bottom_nav).setVisibility(View.VISIBLE);
                    HomeActivity.showLoadingScreen = false;
                }, 2000);


                for (DataSnapshot snap : snapshot.child("posts").getChildren()) {

                    PostModel postModel = new PostModel();
                    postModel.setId(snap.child("id").getValue(String.class));
                    postModel.setImgUrl(snap.child("imgUrl").getValue(String.class));
                    postModel.setLikes(snap.child("likes").getValue(String.class));
                    postModel.setName(snap.child("name").getValue(String.class));
                    postModel.setProfileImgUrl(snap.child("authorProfilePictureURL").getValue(String.class));
                    postModel.setPostType(snap.child("postType").getValue(String.class));

                    // Show post in recycler adapter only if the user is not blocked
                    if (!snapshot.child("users").child(user.getUid()).child("blockedUsers").child(snap.child("name").getValue(String.class)).exists()) {
                        postModelArrayList.add(postModel);
                    }
                }

                if (snapshot.child("users").child(user.getUid()).child("blockedUsers").exists()) {
                    System.out.println(snapshot.child("users").child(user.getUid()).child("blockedUsers"));
                }

                postsAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        MobileAds.initialize(getContext(), initializationStatus -> {System.out.println("ADS: Home ad init completed");});

        TextView usernameLoadingScreen = view.findViewById(R.id.textView40);
        ImageButton searchUserBtn = view.findViewById(R.id.searchPersonButton);
        ImageButton notificationsBtn = view.findViewById(R.id.notificationsButton);
        ImageButton searchUserButton = view.findViewById(R.id.enter_search_button);
        View postsOfTheMonthBtn = view.findViewById(R.id.posts_of_the_month_btn);
        searchView = view.findViewById(R.id.search_view);

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        AdView mAdView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();

        if (!HomeActivity.anonymous) {
            usernameLoadingScreen.setText(user.getDisplayName());

            if (user.getPhotoUrl() != null) {
                Glide.with(getContext()).load(user.getPhotoUrl().toString()).into((ImageView) view.findViewById(R.id.my_profile_image));
            }

        }else {
            usernameLoadingScreen.setText("Anonymous User");
        }

        if (isAdded()) {
            mAdView.loadAd(adRequest);
        }

        if (HomeActivity.anonymous) {
            searchUserBtn.setVisibility(View.GONE);
            notificationsBtn.setVisibility(View.GONE);
        }

        recyclerView = view.findViewById(R.id.home_recycler_view);
        postModelArrayList = new ArrayList<>();
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

        ((HomeActivity)getContext()).findViewById(R.id.bottom_nav).setVisibility(View.GONE);

        if (HomeActivity.showLoadingScreen) {
            loadPosts(view, user, postModelArrayList, recyclerAdapter, false);
        }else {
            loadPosts(view, user, postModelArrayList, recyclerAdapter, true);
        }

        postsOfTheMonthBtn.setOnClickListener(view13 -> {
            Toast.makeText(getContext(), "State is: " + HomeActivity.showLoadingScreen, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), PostsOfTheMonthActivity.class);
            startActivity(intent);
            CustomIntent.customType(getContext(), "left-to-right");
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

}
