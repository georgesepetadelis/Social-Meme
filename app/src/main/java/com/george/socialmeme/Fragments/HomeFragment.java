package com.george.socialmeme.Fragments;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.claudylab.smartdialogbox.SmartDialogBox;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.george.socialmeme.Activities.HomeActivity;
import com.george.socialmeme.Activities.NotificationsActivity;
import com.george.socialmeme.Activities.SplashScreenActivity;
import com.george.socialmeme.Activities.UserProfileActivity;
import com.george.socialmeme.Adapters.PostRecyclerAdapter;
import com.george.socialmeme.BuildConfig;
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
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import maes.tech.intentanim.CustomIntent;
import smartdevelop.ir.eram.showcaseviewlib.GuideView;
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType;
import smartdevelop.ir.eram.showcaseviewlib.config.Gravity;

public class HomeFragment extends Fragment {

    ArrayList<PostModel> postModelArrayList;
    PostRecyclerAdapter recyclerAdapter;
    LoadingDialog progressDialog;
    ProgressBar progressBar;
    EditText searchView;
    RecyclerView recyclerView;
    ImageButton notificationsBtn, searchUserButton;
    View openNotificationsView;
    boolean isSearchViewExpanded = false;

    void appShowCase() {

        if (isAdded()) {
            SharedPreferences sharedPref = getContext().getSharedPreferences("app_showcase", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();

            if (!sharedPref.getBoolean("app_showcase", false)) {
                new GuideView.Builder(getContext())
                        .setTitle("Check your notifications")
                        .setContentText("You can click here to see your notifications")
                        .setTargetView(notificationsBtn)
                        .setDismissType(DismissType.anywhere)
                        .setGravity(Gravity.center)
                        .setGuideListener(view -> {
                            new GuideView.Builder(getContext())
                                    .setTitle("Search a user")
                                    .setContentText("You can search a user from here by clicking this\nbutton and typing their name")
                                    .setTargetView(openNotificationsView)
                                    .setDismissType(DismissType.anywhere)
                                    .setGravity(Gravity.center)
                                    .setGuideListener(view1 -> {
                                        new GuideView.Builder(getContext())
                                                .setTitle("Upload a new meme")
                                                .setContentText("You can upload a new meme by clicking\nthis button and select the meme type you want to upload")
                                                .setTargetView(HomeActivity.bottomNavBar.getViewById(R.id.new_post_fragment))
                                                .setDismissType(DismissType.anywhere)
                                                .setGravity(Gravity.center)
                                                .setGuideListener(view2 -> {
                                                    new GuideView.Builder(getContext())
                                                            .setTitle("View your profile")
                                                            .setContentText("You can see your memes, followers\ntrophies and access settings and more from here.")
                                                            .setTargetView(HomeActivity.bottomNavBar.getViewById(R.id.my_profile_fragment))
                                                            .setDismissType(DismissType.anywhere)
                                                            .setGravity(Gravity.center)
                                                            .build()
                                                            .show();
                                                })
                                                .build()
                                                .show();
                                    })
                                    .build()
                                    .show();
                        }).build().show();
                editor.putBoolean("app_showcase", true);
                editor.apply();
            }
        }

    }

    void openDonateURL() {
        if (isAdded()) {
            Uri uri = Uri.parse("https://PayPal.me/GSepetadelis");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }

    void fetchAllPostsFromDB(boolean refreshDataFromDB, View fragmentView, SwipeRefreshLayout swipeRefreshLayout) {

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        notificationsBtn.setEnabled(false);
        searchUserButton.setEnabled(false);

        if (refreshDataFromDB) {
            getActivity().recreate();
            CustomIntent.customType(getActivity(), "fadein-to-fadeout");
        }
        else {
            if (!HomeActivity.savedPostsArrayList.isEmpty()) {
                // Load saved data
                postModelArrayList.addAll(HomeActivity.savedPostsArrayList);
                notificationsBtn.setEnabled(true);
                searchUserButton.setEnabled(true);
            } else {
                // Load data from DB
                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (!HomeActivity.singedInAnonymously) {
                            if (!snapshot.child("users").child(user.getUid()).child("fcm_token").exists()) {
                                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        rootRef.child("users").child(user.getUid()).child("fcm_token").setValue(task.getResult());
                                    }
                                });
                            }
                        }

                        for (DataSnapshot postSnapshot : snapshot.child("posts").getChildren()) {

                            PostModel postModel = new PostModel();
                            postModel.setId(postSnapshot.child("id").getValue(String.class));
                            postModel.setImgUrl(postSnapshot.child("imgUrl").getValue(String.class));
                            postModel.setLikes(postSnapshot.child("likes").getValue(String.class));
                            postModel.setName(postSnapshot.child("name").getValue(String.class));
                            postModel.setProfileImgUrl(postSnapshot.child("authorProfilePictureURL").getValue(String.class));
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

                            if (postSnapshot.child("postType").getValue(String.class).equals("audio")) {
                                postModel.setAudioName(postSnapshot.child("audioName").getValue(String.class));
                            }

                            if (postSnapshot.child("comments").exists()) {
                                postModel.setCommentsCount(String.valueOf(postSnapshot.child("comments").getChildrenCount()));
                            } else {
                                postModel.setCommentsCount("0");
                            }

                            if (!HomeActivity.singedInAnonymously) {
                                // Show post in recycler adapter only if the user is not blocked
                                if (!snapshot.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .child("blockedUsers").child(postSnapshot.child("name").getValue(String.class)).exists()) {
                                    postModelArrayList.add(postModel);
                                }
                            } else {
                                postModelArrayList.add(postModel);
                            }
                            recyclerAdapter.notifyItemInserted(postModelArrayList.size() - 1);
                        }

                        if (HomeActivity.showLoadingScreen) {
                            new Handler().postDelayed(() -> {
                                int appVersionCode = BuildConfig.VERSION_CODE;
                                int latestAppVersion = Integer.parseInt(snapshot.child("latest_version_code").getValue(String.class));
                                if (appVersionCode < latestAppVersion) {
                                    new AlertDialog.Builder(getContext())
                                            .setTitle("Update required.")
                                            .setCancelable(false)
                                            .setMessage("For security reasons having the latest version is required to use Social Meme")
                                            .setPositiveButton("Update", (dialogInterface, i) -> {
                                                Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.george.socialmeme");
                                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                                startActivity(intent);
                                            }).show();
                                } else {
                                    notificationsBtn.setEnabled(true);
                                    searchUserButton.setEnabled(true);
                                    fragmentView.findViewById(R.id.constraintLayout2).setVisibility(View.GONE);
                                    HomeActivity.bottomNavBar.setVisibility(View.VISIBLE);
                                    swipeRefreshLayout.setEnabled(true);
                                    HomeActivity.showLoadingScreen = false;
                                    HomeActivity.savedPostsArrayList = postModelArrayList;
                                }
                            }, 0);
                        }

                        // Add post's of the month view as RecyclerView item
                        // to avoid using ScrollView
                        PostModel postsOfTheMonthView = new PostModel();
                        postsOfTheMonthView.setPostType("postsOfTheMonth");
                        postModelArrayList.add(postsOfTheMonthView);
                        recyclerAdapter.notifyItemInserted(postModelArrayList.size() - 1);
                        appShowCase();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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
        notificationsBtn = view.findViewById(R.id.notificationsButton);
        searchUserButton = view.findViewById(R.id.enter_search_button);
        recyclerView = view.findViewById(R.id.home_recycler_view);
        searchView = view.findViewById(R.id.search_view);
        progressDialog = LoadingDialog.Companion.get(getActivity());
        openNotificationsView = view.findViewById(R.id.view18);
        progressBar = view.findViewById(R.id.home_progress_bar);

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        postModelArrayList = new ArrayList<>();

        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerAdapter = new PostRecyclerAdapter(postModelArrayList, getContext(), getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(layoutManager);

        searchView.setVisibility(View.GONE);
        searchUserButton.setVisibility(View.GONE);

        if (!HomeActivity.singedInAnonymously) {
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
                if (layoutManager.findFirstVisibleItemPosition() == 0) {
                    swipeRefreshLayout.setEnabled(true);
                }
            }
        });

        // Reload data
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            fetchAllPostsFromDB(true, view, swipeRefreshLayout);
            swipeRefreshLayout.setRefreshing(false);
        });

        if (HomeActivity.showLoadingScreen) {
            HomeActivity.bottomNavBar.setVisibility(View.GONE);
            swipeRefreshLayout.setEnabled(false);
            view.findViewById(R.id.constraintLayout2).setVisibility(View.VISIBLE);
        }

        fetchAllPostsFromDB(false, view, swipeRefreshLayout);

        // Display donate dialog (1 in 15 cases)
        // except the user is singed in anonymously
        if (!HomeActivity.singedInAnonymously) {
            int randomNum = ThreadLocalRandom.current().nextInt(0, 15 + 1);
            if (randomNum == 3) {
                AlertDialog donateDialog = new AlertDialog.Builder(getContext())
                        .setTitle("Can you buy me a coffee?")
                        .setMessage(getString(R.string.donate_msg))
                        .setPositiveButton("Donate", (dialogInterface, i) ->
                                openDonateURL())
                        .setNegativeButton("No, thanks", (dialogInterface, i) -> dialogInterface.dismiss())
                        .setIcon(R.drawable.ic_coffee)
                        .create();
                donateDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                donateDialog.getWindow().setBackgroundDrawableResource(R.drawable.custom_dialog_background);
                donateDialog.show();
            }
        } else {
            AlertDialog donateDialog = new AlertDialog.Builder(getContext())
                    .setTitle("Can you buy me a coffee?")
                    .setMessage(getString(R.string.donate_msg))
                    .setPositiveButton("Donate", (dialogInterface, i) ->
                            openDonateURL())
                    .setNegativeButton("No, thanks", (dialogInterface, i) -> dialogInterface.dismiss())
                    .setCancelable(false)
                    .setIcon(R.drawable.ic_coffee)
                    .create();
            donateDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            donateDialog.getWindow().setBackgroundDrawableResource(R.drawable.custom_dialog_background);
            donateDialog.show();
        }

        searchUserButton.setOnClickListener(v -> usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                boolean userFound = false;

                for (DataSnapshot snap : snapshot.getChildren()) {
                    if (snap.child("name").getValue().toString().equals(searchView.getText().toString())) {
                        userFound = true;
                        Intent intent = new Intent(getActivity(), UserProfileActivity.class);
                        intent.putExtra("user_id", snap.child("id").getValue().toString());
                        intent.putExtra("username", searchView.getText().toString());
                        getActivity().startActivity(intent);
                        CustomIntent.customType(getActivity(), "left-to-right");
                        break;
                    }
                }

                if (!userFound) {
                    SmartDialogBox.showSearchDialog(getActivity(), "We cannot find a user with this username", "OK");
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

            if (isSearchViewExpanded) {
                // search is closed
                searchUserBtn.setImageResource(R.drawable.ic_search);
                isSearchViewExpanded = false;

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
                isSearchViewExpanded = true;

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
