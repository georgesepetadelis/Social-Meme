package com.george.socialmeme.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.george.socialmeme.Activities.HomeActivity;
import com.george.socialmeme.Activities.NotificationsActivity;
import com.george.socialmeme.Activities.UserProfileActivity;
import com.george.socialmeme.Adapters.PostRecyclerAdapter;
import com.george.socialmeme.Models.PostModel;
import com.george.socialmeme.R;
import com.github.loadingview.LoadingDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

import maes.tech.intentanim.CustomIntent;

public class HomeFragment extends Fragment {

    LoadingDialog progressDialog;
    boolean isSearchOpen = false;
    private EditText searchView;
    ArrayList<PostModel> postModelArrayList;
    RecyclerView recyclerView;
    PostRecyclerAdapter recyclerAdapter;

    void loadAllPosts(View fragmentView, SwipeRefreshLayout swipeRefreshLayout) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (HomeActivity.showLoadingScreen) {
                    new Handler().postDelayed(() -> {
                        fragmentView.findViewById(R.id.constraintLayout2).setVisibility(View.GONE);
                        HomeActivity.bottomNavBar.setVisibility(View.VISIBLE);
                        swipeRefreshLayout.setEnabled(true);
                        HomeActivity.showLoadingScreen = false;
                    }, 1200);
                }

                for (DataSnapshot postSnapshot : snapshot.child("posts").getChildren()) {

                    PostModel postModel = new PostModel();
                    postModel.setId(postSnapshot.child("id").getValue(String.class));
                    postModel.setImgUrl(postSnapshot.child("imgUrl").getValue(String.class));
                    postModel.setLikes(postSnapshot.child("likes").getValue(String.class));
                    postModel.setName(postSnapshot.child("name").getValue(String.class));
                    postModel.setProfileImgUrl(postSnapshot.child("authorProfilePictureURL").getValue(String.class));
                    postModel.setPostType(postSnapshot.child("postType").getValue(String.class));

                    if (postSnapshot.child("comments").exists()) {
                        postModel.setCommentsCount(String.valueOf(postSnapshot.child("comments").getChildrenCount()));
                    }else {
                        postModel.setCommentsCount("0");
                    }

                    if (!HomeActivity.anonymous) {
                        // Show post in recycler adapter only if the user is not blocked
                        if (!snapshot.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("blockedUsers").child(postSnapshot.child("name").getValue(String.class)).exists()) {
                            postModelArrayList.add(postModel);
                        }
                    }else {
                        postModelArrayList.add(postModel);
                    }
                    recyclerAdapter.notifyItemInserted(postModelArrayList.size() -1);
                }

                // Add post's of the month view as RecyclerView item
                // to avoid using ScrollView
                PostModel postsOfTheMonthView = new PostModel();
                postsOfTheMonthView.setPostType("postsOfTheMonth");
                postModelArrayList.add(postsOfTheMonthView);
                recyclerAdapter.notifyItemInserted(postModelArrayList.size() -1);

                // Reverse elements inside postModelArrayList
                // to show items inside RecyclerView reversed
                Collections.reverse(postModelArrayList);

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
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        AdView mAdView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();

        if (isAdded()) {
            mAdView.loadAd(adRequest);
        }

        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.root_view);
        TextView usernameLoadingScreen = view.findViewById(R.id.textView40);
        ImageButton searchUserBtn = view.findViewById(R.id.searchPersonButton);
        ImageButton notificationsBtn = view.findViewById(R.id.notificationsButton);
        ImageButton searchUserButton = view.findViewById(R.id.enter_search_button);
        recyclerView = view.findViewById(R.id.home_recycler_view);
        searchView = view.findViewById(R.id.search_view);
        progressDialog = LoadingDialog.Companion.get(getActivity());

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        postModelArrayList = new ArrayList<>();

        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerAdapter = new PostRecyclerAdapter(postModelArrayList, getContext(), getActivity());
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(layoutManager);

        searchView.setVisibility(View.GONE);
        searchUserButton.setVisibility(View.GONE);

        if (!HomeActivity.anonymous) {
            usernameLoadingScreen.setText(user.getDisplayName());
            if (user.getPhotoUrl() != null) {
                Glide.with(getContext()).load(user.getPhotoUrl().toString())
                        .into((ImageView) view.findViewById(R.id.my_profile_image));
            }
        } else {
            searchUserBtn.setVisibility(View.GONE);
            notificationsBtn.setVisibility(View.GONE);
            usernameLoadingScreen.setText("Anonymous User");
        }

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // Avoid data reload on every scroll
                swipeRefreshLayout.setEnabled(dy <= 5);
            }
        });

        // Reload data
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            loadAllPosts(view, swipeRefreshLayout);
            swipeRefreshLayout.setRefreshing(false);
        });

        if (HomeActivity.showLoadingScreen) {
            HomeActivity.bottomNavBar.setVisibility(View.GONE);
            swipeRefreshLayout.setEnabled(false);
            view.findViewById(R.id.constraintLayout2).setVisibility(View.VISIBLE);
        }

        loadAllPosts(view, swipeRefreshLayout);

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

            } else {
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
