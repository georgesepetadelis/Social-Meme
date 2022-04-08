package com.george.socialmeme.Fragments;

import static android.content.Context.MODE_PRIVATE;

import static com.george.socialmeme.Activities.HomeActivity.filtersBtn;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.claudylab.smartdialogbox.SmartDialogBox;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.george.socialmeme.Activities.HomeActivity;
import com.george.socialmeme.Activities.NotificationsActivity;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import maes.tech.intentanim.CustomIntent;
import smartdevelop.ir.eram.showcaseviewlib.GuideView;
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType;
import smartdevelop.ir.eram.showcaseviewlib.config.Gravity;

public class HomeFragment extends Fragment {

    ArrayList<PostModel> postModelArrayList, filteredPostsArrayList;
    PostRecyclerAdapter recyclerAdapter;
    LoadingDialog progressDialog;
    ProgressBar progressBar;
    RecyclerView recyclerView;
    ImageButton notificationsBtn;
    View openNotificationsView;

    // Variables for filter dialog
    final boolean[] imagesItemSelected = {true};
    final boolean[] videosItemSelected = {true};
    final boolean[] soundsItemSelected = {true};
    final boolean[] textItemSelected = {true};

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

    void showSearchUserDialog() {

        Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.search_user_top_sheet);

        ImageButton dismissDialogButton = dialog.findViewById(R.id.dismiss_top_dialog_btn);
        dismissDialogButton.setOnClickListener(view -> dialog.dismiss());

        EditText usernameForSearch = dialog.findViewById(R.id.user_search_et);
        ImageButton submitUserSearchButton = dialog.findViewById(R.id.submit_user_search);
        ProgressBar progressBar = dialog.findViewById(R.id.progress_bar_top_sheet);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        submitUserSearchButton.setOnClickListener(view -> {

            String usernameInput = usernameForSearch.getText().toString();
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

            // Prevent user from dismissing the
            // dialog while app is searching for the user
            // to avoid any errors
            dialog.setCancelable(false);

            if (!usernameInput.isEmpty()) {

                submitUserSearchButton.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);

                if (usernameInput.equals(user.getDisplayName())) {
                    dialog.dismiss();
                    HomeActivity.bottomNavBar.setItemSelected(R.id.my_profile_fragment, true);
                } else {
                    usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            boolean userFound = false;

                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                if (Objects.equals(userSnapshot.child("name").getValue(String.class), usernameInput)) {
                                    userFound = true;
                                    Intent intent = new Intent(getActivity(), UserProfileActivity.class);
                                    intent.putExtra("user_id", userSnapshot.child("id").getValue().toString());
                                    intent.putExtra("username", usernameInput);
                                    getActivity().startActivity(intent);
                                    CustomIntent.customType(getActivity(), "left-to-right");
                                    break;
                                }
                            }

                            if (!userFound) {
                                SmartDialogBox.showSearchDialog(getActivity(), "We cannot find a user with this username", "OK");
                            }

                            progressBar.setVisibility(View.GONE);
                            submitUserSearchButton.setVisibility(View.VISIBLE);
                            dialog.setCancelable(true);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            dialog.setCancelable(true);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getActivity(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            } else {
                dialog.setCancelable(true);
                progressBar.setVisibility(View.GONE);
                SmartDialogBox.showInfoDialog(getActivity(), "Username cannot be empty", "OK");
            }

        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.TopSheetDialogAnimation;
        dialog.getWindow().setGravity(android.view.Gravity.TOP);

    }

    void showFiltersDialog() {

        Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.posts_filter_bottom_sheet);

        View imagesItem = dialog.findViewById(R.id.view23);
        View videosItem = dialog.findViewById(R.id.view21);
        View soundsItem = dialog.findViewById(R.id.view22);
        View textItem = dialog.findViewById(R.id.view20);
        Button applyFiltersBtn = dialog.findViewById(R.id.apply_filters);

        if (!imagesItemSelected[0]) {
            dialog.findViewById(R.id.imageView30).setVisibility(View.GONE);
        }

        if (!videosItemSelected[0]) {
            dialog.findViewById(R.id.imageView32).setVisibility(View.GONE);
        }

        if (!soundsItemSelected[0]) {
            dialog.findViewById(R.id.imageView34).setVisibility(View.GONE);
        }

        if (!textItemSelected[0]) {
            dialog.findViewById(R.id.imageView36).setVisibility(View.GONE);
        }

        imagesItem.setOnClickListener(view -> {
            if (imagesItemSelected[0]) {
                imagesItemSelected[0] = false;
                YoYo.with(Techniques.FadeOut).withListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        dialog.findViewById(R.id.imageView30).setVisibility(View.GONE);
                    }
                }).duration(500).repeat(0).playOn(dialog.findViewById(R.id.imageView30));
            } else {
                imagesItemSelected[0] = true;
                YoYo.with(Techniques.FadeIn).duration(500).repeat(0).playOn(dialog.findViewById(R.id.imageView30));
                dialog.findViewById(R.id.imageView30).setVisibility(View.VISIBLE);
            }
        });

        videosItem.setOnClickListener(view -> {
            if (videosItemSelected[0]) {
                videosItemSelected[0] = false;
                YoYo.with(Techniques.FadeOut).withListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        dialog.findViewById(R.id.imageView32).setVisibility(View.GONE);
                    }
                }).duration(500).repeat(0).playOn(dialog.findViewById(R.id.imageView32));
            } else {
                videosItemSelected[0] = true;
                YoYo.with(Techniques.FadeIn).duration(500).repeat(0).playOn(dialog.findViewById(R.id.imageView32));
                dialog.findViewById(R.id.imageView32).setVisibility(View.VISIBLE);
            }
        });

        soundsItem.setOnClickListener(view -> {
            if (soundsItemSelected[0]) {
                soundsItemSelected[0] = false;
                YoYo.with(Techniques.FadeOut).withListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        dialog.findViewById(R.id.imageView34).setVisibility(View.GONE);
                    }
                }).duration(500).repeat(0).playOn(dialog.findViewById(R.id.imageView34));
            } else {
                soundsItemSelected[0] = true;
                YoYo.with(Techniques.FadeIn).duration(500).repeat(0).playOn(dialog.findViewById(R.id.imageView34));
                dialog.findViewById(R.id.imageView34).setVisibility(View.VISIBLE);
            }
        });

        textItem.setOnClickListener(view -> {
            if (textItemSelected[0]) {
                textItemSelected[0] = false;
                YoYo.with(Techniques.FadeOut).withListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        dialog.findViewById(R.id.imageView36).setVisibility(View.GONE);
                    }
                }).duration(500).repeat(0).playOn(dialog.findViewById(R.id.imageView36));
            } else {
                textItemSelected[0] = true;
                YoYo.with(Techniques.FadeIn).duration(500).repeat(0).playOn(dialog.findViewById(R.id.imageView36));
                dialog.findViewById(R.id.imageView36).setVisibility(View.VISIBLE);
            }
        });

        applyFiltersBtn.setOnClickListener(view -> {

            if (!imagesItemSelected[0] && !videosItemSelected[0] && !soundsItemSelected[0] && !textItemSelected[0]) {
                dialog.dismiss();
            } else {

                applyFiltersBtn.setText("Filtering...");
                applyFiltersBtn.setEnabled(false);
                dialog.setCancelable(false);

                for (PostModel postModel : postModelArrayList) {

                    if (postModel.getPostType().equals("image")) {
                        if (imagesItemSelected[0]) {
                            filteredPostsArrayList.add(postModel);
                        }
                    }

                    if (postModel.getPostType().equals("video")) {
                        if (videosItemSelected[0]) {
                            filteredPostsArrayList.add(postModel);
                        }
                    }

                    if (postModel.getPostType().equals("audio")) {
                        if (soundsItemSelected[0]) {
                            filteredPostsArrayList.add(postModel);
                        }
                    }

                    if (postModel.getPostType().equals("text")) {
                        if (textItemSelected[0]) {
                            filteredPostsArrayList.add(postModel);
                        }
                    }

                }

                // Update RecyclerView adapter
                recyclerAdapter = new PostRecyclerAdapter(filteredPostsArrayList, getContext(), getActivity());
                LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
                layoutManager.setStackFromEnd(true);
                layoutManager.setReverseLayout(true);
                recyclerView.setAdapter(recyclerAdapter);
                recyclerView.setLayoutManager(layoutManager);

                // Add post's of the month view as RecyclerView item
                // to avoid using ScrollView
                PostModel postsOfTheMonthView = new PostModel();
                postsOfTheMonthView.setPostType("postsOfTheMonth");
                filteredPostsArrayList.add(postsOfTheMonthView);

                recyclerAdapter.notifyDataSetChanged();

                dialog.dismiss();

            }

        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.BottomSheetDialogAnimation;
        dialog.getWindow().setGravity(android.view.Gravity.BOTTOM);

    }

    void getAllPostsFromDB(boolean refreshDataFromDB, View fragmentView, SwipeRefreshLayout swipeRefreshLayout) {

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        notificationsBtn.setEnabled(false);

        if (HomeActivity.showLoadingScreen) {
            filtersBtn.setVisibility(View.INVISIBLE);
        }

        if (refreshDataFromDB) {
            getActivity().startActivity(new Intent(getActivity(), HomeActivity.class));
            CustomIntent.customType(getActivity(), "fadein-to-fadeout");
            getActivity().finish();
        } else {
            if (!HomeActivity.savedPostsArrayList.isEmpty()) {
                // Load saved data
                postModelArrayList.addAll(HomeActivity.savedPostsArrayList);
                notificationsBtn.setEnabled(true);
                progressBar.setVisibility(View.GONE);
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

                            if (postSnapshot.child("imgUrl").getValue(String.class) == null) {
                                postModel.setImgUrl("none");
                            }else {
                                postModel.setImgUrl(postSnapshot.child("imgUrl").getValue(String.class));
                            }

                            postModel.setLikes(postSnapshot.child("likes").getValue(String.class));
                            postModel.setName(postSnapshot.child("name").getValue(String.class));
                            postModel.setProfileImgUrl(postSnapshot.child("authorProfilePictureURL").getValue(String.class));
                            postModel.setPostType(postSnapshot.child("postType").getValue(String.class));

                            for (DataSnapshot user : snapshot.child("users").getChildren()) {
                                if (Objects.equals(user.child("name").getValue(String.class), postSnapshot.child("name").getValue(String.class))) {
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

                        // Add post's of the month view as RecyclerView item
                        // to avoid using ScrollView
                        PostModel postsOfTheMonthView = new PostModel();
                        postsOfTheMonthView.setPostType("postsOfTheMonth");
                        postModelArrayList.add(postsOfTheMonthView);
                        recyclerAdapter.notifyItemInserted(postModelArrayList.size() - 1);
                        appShowCase();

                        if (HomeActivity.showLoadingScreen) {
                            new Handler().postDelayed(() -> {
                                filtersBtn.setVisibility(View.INVISIBLE);
                                int appVersionCode = BuildConfig.VERSION_CODE;
                                int latestAppVersion = Integer.parseInt(snapshot.child("latest_version_code").getValue(String.class));
                                if (appVersionCode < latestAppVersion) {

                                    if (getActivity() != null) {
                                        new AlertDialog.Builder(getActivity())
                                                .setTitle("Update required.")
                                                .setCancelable(false)
                                                .setMessage("For security reasons having the latest version is required to use Social Meme")
                                                .setPositiveButton("Update", (dialogInterface, i) -> {
                                                    Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.george.socialmeme");
                                                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                                    startActivity(intent);
                                                }).show();
                                    }

                                } else {
                                    notificationsBtn.setEnabled(true);
                                    filtersBtn.setVisibility(View.VISIBLE);
                                    HomeActivity.bottomNavBar.setVisibility(View.VISIBLE);
                                    swipeRefreshLayout.setEnabled(true);
                                    HomeActivity.showLoadingScreen = false;
                                    HomeActivity.savedPostsArrayList = postModelArrayList;

                                    YoYo.with(Techniques.FadeOut).withListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            super.onAnimationEnd(animation);
                                            fragmentView.findViewById(R.id.constraintLayout2).setVisibility(View.GONE);
                                        }
                                    }).repeat(0).duration(1000).playOn(fragmentView.findViewById(R.id.constraintLayout2));

                                }
                            }, 0);
                        }

                        progressBar.setVisibility(View.GONE);

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

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.root_view);
        TextView usernameLoadingScreen = view.findViewById(R.id.textView40);
        ImageButton searchUserBtn = view.findViewById(R.id.searchPersonButton);

        notificationsBtn = view.findViewById(R.id.notificationsButton);
        recyclerView = view.findViewById(R.id.home_recycler_view);
        progressDialog = LoadingDialog.Companion.get(getActivity());
        openNotificationsView = view.findViewById(R.id.view18);
        progressBar = view.findViewById(R.id.home_progress_bar);

        postModelArrayList = new ArrayList<>();
        filteredPostsArrayList = new ArrayList<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerAdapter = new PostRecyclerAdapter(postModelArrayList, getContext(), getActivity());
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(layoutManager);

        if (!HomeActivity.singedInAnonymously) {
            usernameLoadingScreen.setText(user.getDisplayName());
            if (user.getPhotoUrl() != null) {
                Picasso.get().load(user.getPhotoUrl().toString()).into(view.findViewById(R.id.my_profile_image), new Callback() {
                    @Override
                    public void onSuccess() {
                        YoYo.with(Techniques.BounceIn).duration(1200).repeat(0).playOn(view.findViewById(R.id.my_profile_image));
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.i("IMAGE_LOAD_HOME", "Error: " + e.getLocalizedMessage());
                    }
                });
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

        // Refresh posts
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            getAllPostsFromDB(true, view, swipeRefreshLayout);
            swipeRefreshLayout.setRefreshing(false);
        });

        if (HomeActivity.showLoadingScreen) {
            HomeActivity.bottomNavBar.setVisibility(View.GONE);
            swipeRefreshLayout.setEnabled(false);
            view.findViewById(R.id.constraintLayout2).setVisibility(View.VISIBLE);
        }

        getAllPostsFromDB(false, view, swipeRefreshLayout);
        filtersBtn.setOnClickListener(view13 -> showFiltersDialog());

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

        notificationsBtn.setOnClickListener(view12 -> {
            startActivity(new Intent(getActivity(), NotificationsActivity.class));
            CustomIntent.customType(getContext(), "left-to-right");
        });

        searchUserBtn.setOnClickListener(view1 -> showSearchUserDialog());

        return view;
    }

}
