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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.inappmessaging.FirebaseInAppMessaging;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Queue;

import maes.tech.intentanim.CustomIntent;

public class HomeFragment extends Fragment {

    LoadingDialog progressDialog;
    final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    boolean isSearchOpen = false;
    private EditText searchView;
    ArrayList<PostModel> postModelArrayList;
    RecyclerView recyclerView;
    PostRecyclerAdapter recyclerAdapter;

    final int ITEM_LOAD_COUNT = 3;
    int totalItems = 0, lastVisibleItem = 0;
    boolean isLoading = false, isMaxData = false;
    String lastKey = "", lastNode = "";

    void getLastKeyFromFirebase() {

        Query getLastKey = FirebaseDatabase.getInstance().getReference()
                .child("posts")
                .orderByKey()
                .limitToLast(1);

        getLastKey.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot last_Key : snapshot.getChildren()) {
                    lastKey = last_Key.getKey();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                isLoading = false;
                Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    void loadPosts(View fragmentView, FirebaseUser user, boolean isRefresh, boolean getDataFromDB) {

        if (isRefresh) {
            fragmentView.findViewById(R.id.constraintLayout2).setVisibility(View.GONE);
            ((HomeActivity) getContext()).findViewById(R.id.bottom_nav).setVisibility(View.VISIBLE);
        }

        if (getDataFromDB) {

            getLastKeyFromFirebase();

            if (!isMaxData) {

                Query query;
                if (TextUtils.isEmpty(lastNode))
                    query = FirebaseDatabase.getInstance().getReference()
                            .child("posts")
                            .orderByKey()
                            .limitToLast(ITEM_LOAD_COUNT);
                else
                    query = FirebaseDatabase.getInstance().getReference()
                            .child("posts")
                            .orderByKey()
                            .startAt(lastNode)
                            .limitToFirst(ITEM_LOAD_COUNT);

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChildren()) {

                            new Handler().postDelayed(() -> {
                                fragmentView.findViewById(R.id.constraintLayout2).setVisibility(View.GONE);
                                HomeActivity.bottomNavBar.setVisibility(View.VISIBLE);
                                HomeActivity.showLoadingScreen = false;
                            }, 1200);

                            ArrayList<PostModel> newPosts = new ArrayList<>();
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {

                                PostModel postModel = new PostModel();
                                postModel.setId(userSnapshot.child("id").getValue(String.class));
                                postModel.setImgUrl(userSnapshot.child("imgUrl").getValue(String.class));
                                postModel.setLikes(userSnapshot.child("likes").getValue(String.class));
                                postModel.setName(userSnapshot.child("name").getValue(String.class));
                                postModel.setProfileImgUrl(userSnapshot.child("authorProfilePictureURL").getValue(String.class));
                                postModel.setPostType(userSnapshot.child("postType").getValue(String.class));

                                // Show post in recycler adapter only if the user is not blocked
                                if (!snapshot.child("users").child(user.getUid()).child("blockedUsers").child(userSnapshot.child("name").getValue(String.class)).exists()) {
                                    //postModelArrayList.add(postModel);
                                    newPosts.add(postModel);
                                    //postModelArrayList.add(postModel);
                                    recyclerAdapter.notifyDataSetChanged();
                                }
                            }

                            lastNode = newPosts.get(newPosts.size() - 1).getId();

                            if (!lastNode.equals(lastKey))
                                newPosts.remove(newPosts.size() - 1);
                            else
                                lastNode = "end"; // Avoiding infinity load

                            recyclerAdapter.addAll(newPosts);
                            isLoading = false;

                        } else {
                            isLoading = false;
                            isMaxData = true;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            } else {
                Toast.makeText(getContext(), "Hello3", Toast.LENGTH_SHORT).show();
                isLoading = false;
                isMaxData = true;
            }


        }

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
        View postsOfTheMonthBtn = view.findViewById(R.id.posts_of_the_month_btn);
        searchView = view.findViewById(R.id.search_view);

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (!HomeActivity.anonymous) {
            usernameLoadingScreen.setText(user.getDisplayName());
            if (user.getPhotoUrl() != null) {
                Glide.with(getContext()).load(user.getPhotoUrl().toString()).into((ImageView) view.findViewById(R.id.my_profile_image));
            }
        } else {
            searchUserBtn.setVisibility(View.GONE);
            notificationsBtn.setVisibility(View.GONE);
            usernameLoadingScreen.setText("Anonymous User");
        }

        recyclerView = view.findViewById(R.id.home_recycler_view);

        if (HomeActivity.savedPostsModelArrayList == null) {
            postModelArrayList = new ArrayList<>();
        } else {
            postModelArrayList = HomeActivity.savedPostsModelArrayList;
        }

        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerAdapter = new PostRecyclerAdapter(postModelArrayList, getContext(), getActivity());

        //layoutManager.setReverseLayout(true);
        //layoutManager.setStackFromEnd(true);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        progressDialog = LoadingDialog.Companion.get(getActivity());

        searchView.setVisibility(View.GONE);
        searchUserButton.setVisibility(View.GONE);

        if (HomeActivity.showWhatsNewMessage) {
            FirebaseInAppMessaging.getInstance().triggerEvent("new_version_open");
        }

        // Avoid data refresh on every swipe down
        //RelativeLayout scrollView = view.findViewById(R.id.scrollView3);
        recyclerView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) ->
                swipeRefreshLayout.setEnabled(scrollY <= 5));

        // Re-load data
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            loadPosts(view, user, true, true);
            swipeRefreshLayout.setRefreshing(false);
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                totalItems = layoutManager.getItemCount();
                lastVisibleItem = layoutManager.findLastVisibleItemPosition();

                if (!isLoading && totalItems <= ((lastVisibleItem + ITEM_LOAD_COUNT))) {
                    loadPosts(view, user, false, true);
                    isLoading = true;
                }

            }
        });

        HomeActivity.bottomNavBar.setVisibility(View.GONE);
        loadPosts(view, user, !HomeActivity.showLoadingScreen, true);

        postsOfTheMonthBtn.setOnClickListener(view13 -> {
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
