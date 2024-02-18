package com.george.socialmeme.Fragments;

import static android.content.Context.MODE_PRIVATE;
import static com.george.socialmeme.Activities.Feed.HomeActivity.allPostsSaved;
import static com.george.socialmeme.Activities.Feed.HomeActivity.filtersBtn;
import static com.george.socialmeme.Activities.Feed.HomeActivity.goUp;
import static com.george.socialmeme.Activities.Feed.HomeActivity.lastHomePosition;
import static com.george.socialmeme.Activities.Feed.HomeActivity.postListToRestore;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
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
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.george.socialmeme.Activities.Common.SplashScreenActivity;
import com.george.socialmeme.Activities.Feed.HomeActivity;
import com.george.socialmeme.Activities.Feed.NotificationsActivity;
import com.george.socialmeme.Activities.Feed.PostActivity;
import com.george.socialmeme.Activities.Feed.SearchUserActivity;
import com.george.socialmeme.Activities.Profile.UserProfileActivity;
import com.george.socialmeme.Adapters.PostRecyclerAdapter;
import com.george.socialmeme.BuildConfig;
import com.george.socialmeme.Helpers.AppHelper;
import com.george.socialmeme.Models.PostModel;
import com.george.socialmeme.R;
import com.github.loadingview.LoadingDialog;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import maes.tech.intentanim.CustomIntent;
import smartdevelop.ir.eram.showcaseviewlib.GuideView;
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType;
import smartdevelop.ir.eram.showcaseviewlib.config.Gravity;

public class HomeFragment extends Fragment {

    private InterstitialAd mInterstitialAd;

    public static ArrayList<PostModel> postModelArrayList, filteredPostsArrayList, notSuffledPostsArray, randomArrayList, allPostsList;

    public static boolean refreshed = false;
    PostRecyclerAdapter recyclerAdapter;
    LoadingDialog progressDialog;
    ProgressBar progressBar;
    public static RecyclerView recyclerView;
    ImageButton notificationsBtn, searchUserBtn;
    View openNotificationsView;

    RewardedAd mRewardedAd;
    final String TAG = "MainActivity";

    // Variables for filter dialog
    final boolean[] imagesItemSelected = {true};
    final boolean[] videosItemSelected = {true};
    final boolean[] soundsItemSelected = {true};
    final boolean[] textItemSelected = {true};

    void appShowCase() {

        if (isAdded() && !HomeActivity.signedInAnonymously) {
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

    void loadFullScreenAd(Context context) {

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.child("ad_type").getValue(String.class).equals("default")) {

                    MobileAds.initialize(context, initializationStatus -> {
                    });

                    AdRequest adRequest = new AdRequest.Builder().build();

                    InterstitialAd.load(context, "ca-app-pub-9627755439548346/4088445598", adRequest,
                            new InterstitialAdLoadCallback() {
                                @Override
                                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                                    // The mInterstitialAd reference will be null until
                                    // an ad is loaded.
                                    mInterstitialAd = interstitialAd;
                                    Log.i("ad_full", "onAdLoaded");

                                    mInterstitialAd.show(getActivity());

                                    mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                        @Override
                                        public void onAdClicked() {
                                            super.onAdClicked();
                                        }

                                        @Override
                                        public void onAdDismissedFullScreenContent() {
                                            super.onAdDismissedFullScreenContent();
                                        }

                                        @Override
                                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                            super.onAdFailedToShowFullScreenContent(adError);

                                        }

                                        @Override
                                        public void onAdImpression() {
                                            super.onAdImpression();
                                            HomeActivity.watched_ad = true;
                                        }

                                        @Override
                                        public void onAdShowedFullScreenContent() {
                                            super.onAdShowedFullScreenContent();
                                            HomeActivity.watched_ad = true;
                                        }
                                    });

                                }

                                @Override
                                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                    // Handle the error
                                    Log.d("ad_full", loadAdError.toString());
                                    mInterstitialAd = null;
                                }
                            });

                } else {
                    loadRewardAd(context);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    void loadRewardAd(Context context) {
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(context, "ca-app-pub-9627755439548346/8245739492",
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        mRewardedAd = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        super.onAdLoaded(rewardedAd);
                        mRewardedAd = rewardedAd;

                        mRewardedAd.show(getActivity(), rewardItem -> Log.i("AD REWARD", "wacthed_Ad"));

                        mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdClicked() {
                                // Called when a click is recorded for an ad.
                                Log.d(TAG, "Ad was clicked.");
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when ad is dismissed.
                                // Set the ad reference to null so you don't show the ad a second time.
                                Log.d(TAG, "Ad dismissed fullscreen content.");
                                mRewardedAd = null;
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when ad fails to show.
                                Log.e(TAG, "Ad failed to show fullscreen content.");
                                mRewardedAd = null;
                            }

                            @Override
                            public void onAdImpression() {
                                // Called when an impression is recorded for an ad.
                                Log.d(TAG, "Ad recorded an impression.");
                                HomeActivity.watched_ad = true;
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when ad is shown.
                                Log.d(TAG, "Ad showed fullscreen content.");
                                HomeActivity.watched_ad = true;
                            }
                        });

                    }
                });

    }

    public interface FirebaseCallback {
        void onComplete();
    }

    Activity activity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        activity = getActivity();

        if (!HomeActivity.watched_ad) {

            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
            rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.child("show_full_ads").getValue(String.class).equals("enabled")
                            || !AppHelper.isAppInstalledFromPlayStore(context)) {
                        checkForDailyAd(context);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, "Error (admob): " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }

    }

    void checkForDailyAd(Context context) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.child("only_daily").getValue(String.class).equals("enabled")) {

                    SharedPreferences sharedPref = context.getSharedPreferences("last_ad", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

                    if (!AppHelper.isAppInstalledFromPlayStore(context) ||
                            sharedPref.getString("last_ad", "none").equals(null) ||
                            sharedPref.getString("last_ad", "none").equals("none") ||
                            !sharedPref.getString("last_ad", "none").equals(date)) {
                        editor.putString("last_ad", date);
                        editor.apply();
                        loadFullScreenAd(context);
                    }
                } else {
                    loadFullScreenAd(context);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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
                dialog.findViewById(R.id.imageView30).setVisibility(View.GONE);
            } else {
                imagesItemSelected[0] = true;
                dialog.findViewById(R.id.imageView30).setVisibility(View.VISIBLE);
            }
        });

        videosItem.setOnClickListener(view -> {
            if (videosItemSelected[0]) {
                videosItemSelected[0] = false;
                dialog.findViewById(R.id.imageView32).setVisibility(View.GONE);
            } else {
                videosItemSelected[0] = true;
                dialog.findViewById(R.id.imageView32).setVisibility(View.VISIBLE);
            }
        });

        soundsItem.setOnClickListener(view -> {
            if (soundsItemSelected[0]) {
                soundsItemSelected[0] = false;
                dialog.findViewById(R.id.imageView34).setVisibility(View.GONE);
            } else {
                soundsItemSelected[0] = true;
                dialog.findViewById(R.id.imageView34).setVisibility(View.VISIBLE);
            }
        });

        textItem.setOnClickListener(view -> {
            if (textItemSelected[0]) {
                textItemSelected[0] = false;
                dialog.findViewById(R.id.imageView36).setVisibility(View.GONE);
            } else {
                textItemSelected[0] = true;
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

    FirebaseCallback callback = () -> {

        Collections.reverse(HomeFragment.postModelArrayList);
        allPostsList = new ArrayList<>();
        allPostsList.addAll(postModelArrayList);
        allPostsSaved.addAll(postModelArrayList);
        allPostsList = allPostsSaved;

        if (HomeActivity.openNotification) {

            if (isAdded()) {
                if (HomeActivity.notiPostId != null && HomeActivity.notiUserId == null) {
                    Intent intent = new Intent(activity, PostActivity.class);
                    intent.putExtra("post_id", HomeActivity.notiPostId);
                    startActivity(intent);
                    CustomIntent.customType(getActivity(), "left-to-right");
                } else if (HomeActivity.notiPostId == null && HomeActivity.notiUserId != null) {
                    Intent intent = new Intent(activity, UserProfileActivity.class);
                    intent.putExtra("user_id", HomeActivity.notiUserId);
                    intent.putExtra("username", HomeActivity.notiUsername);
                    startActivity(intent);
                    CustomIntent.customType(getActivity(), "left-to-right");
                }
            }
        }
    };

    public void getAllPostsFromDB(boolean refreshDataFromDB, View fragmentView, SwipeRefreshLayout swipeRefreshLayout) {

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (HomeActivity.showLoadingScreen) {
            filtersBtn.setVisibility(View.INVISIBLE);
            HomeActivity.goUp.setVisibility(View.INVISIBLE);
        }

        if (refreshDataFromDB) {
            getActivity().startActivity(new Intent(getActivity(), HomeActivity.class));
            CustomIntent.customType(getActivity(), "fadein-to-fadeout");
            getActivity().finish();
            refreshed = true;
        } else {

            // Check for cached posts
            if (!HomeActivity.savedPostsArrayList.isEmpty()) {

                // Load saved data
                HomeActivity.noSuffledPostsList = HomeActivity.savedPostsArrayList;
                randomArrayList.addAll(HomeActivity.savedPostsArrayList);
                notificationsBtn.setEnabled(true);
                Collections.shuffle(randomArrayList);
                
                if (HomeActivity.noSuffledPostsList != null) {
                    HomeFragment.postModelArrayList.addAll(HomeActivity.noSuffledPostsList);
                }

                if (postModelArrayList != null && randomArrayList != null) {
                    // Add post's of the month view as RecyclerView item
                    // to avoid using ScrollView
                    PostModel postsOfTheMonthView = new PostModel();
                    postsOfTheMonthView.setPostType("postsOfTheMonth");
                    randomArrayList.add(postsOfTheMonthView);
                    recyclerAdapter.notifyItemInserted(postModelArrayList.size() - 1);
                }

                progressBar.setVisibility(View.GONE);

            } else {
                // Load data from DB
                postModelArrayList.clear();
                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (!HomeActivity.signedInAnonymously) {
                            if (!snapshot.child("users").child(user.getUid()).child("fcm_token").exists()) {
                                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        rootRef.child("users").child(user.getUid()).child("fcm_token").setValue(task.getResult());
                                    }
                                });
                            }
                        }

                        for (DataSnapshot postSnapshot : snapshot.child("posts").getChildren()) {

                            if (postSnapshot.child("name").getValue(String.class) != null && !postSnapshot.child("reported").exists()) {

                                if (!HomeActivity.signedInAnonymously && postSnapshot.child("name").getValue(String.class).equals(user.getDisplayName())) {
                                    HomeActivity.userHasPosts = true;
                                }

                                PostModel model = postSnapshot.getValue(PostModel.class);

                                if (model.getComments() != null) {
                                    model.setCommentsCount(String.valueOf(model.getComments().size()));
                                } else {
                                    model.setCommentsCount("0");
                                }

                                if (!HomeActivity.signedInAnonymously && user.getUid() != null) {
                                    // Show post in recycler adapter only if the user is not blocked
                                    if (!snapshot.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .child("blockedUsers").child(postSnapshot.child("name").getValue(String.class)).exists()) {

                                        if (HomeActivity.noSuffledPostsList != null) {
                                            HomeActivity.noSuffledPostsList.add(model);
                                        }

                                        postModelArrayList.add(model);
                                    }
                                } else {
                                    HomeActivity.noSuffledPostsList.add(model);
                                    postModelArrayList.add(model);
                                }
                                recyclerAdapter.notifyItemInserted(postModelArrayList.size() - 1);

                            }

                        }

                        randomArrayList.addAll(postModelArrayList);
                        Collections.shuffle(randomArrayList);

                        // Add post's of the month view as RecyclerView item
                        // to avoid using ScrollView
                        PostModel postsOfTheMonthView = new PostModel();
                        postsOfTheMonthView.setPostType("postsOfTheMonth");
                        randomArrayList.add(postsOfTheMonthView);
                        recyclerAdapter.notifyItemInserted(postModelArrayList.size() - 1);

                        appShowCase();

                        if (HomeActivity.showLoadingScreen) {

                            new Handler().postDelayed(() -> {

                                filtersBtn.setVisibility(View.INVISIBLE);
                                HomeActivity.goUp.setVisibility(View.INVISIBLE);
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
                                        /*else {
                                                new AlertDialog.Builder(getActivity())
                                                        .setTitle("Update required.")
                                                        .setCancelable(false)
                                                        .setMessage("For security reasons having the latest version is required to use Social Meme")
                                                        .setPositiveButton("Update", (dialogInterface, i) -> {
                                                            Uri uri = Uri.parse("https://github.com/georgesepetadelis/Social-Meme");
                                                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                                            startActivity(intent);
                                                        }).show();
                                        }*/

                                    }

                                } else {

                                    new Handler().postDelayed(() -> {

                                        notificationsBtn.setEnabled(true);
                                        filtersBtn.setVisibility(View.VISIBLE);
                                        HomeActivity.goUp.setVisibility(View.VISIBLE);
                                        HomeActivity.bottomNavBar.setVisibility(View.VISIBLE);
                                        swipeRefreshLayout.setEnabled(true);
                                        HomeActivity.showLoadingScreen = false;
                                        HomeActivity.savedPostsArrayList = postModelArrayList;
                                        HomeActivity.noSuffledPostsList = notSuffledPostsArray;

                                        YoYo.with(Techniques.FadeOut).withListener(new AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                super.onAnimationEnd(animation);
                                                fragmentView.findViewById(R.id.constraintLayout2).setVisibility(View.GONE);
                                                if (HomeActivity.UploadNewPost && HomeActivity.fileUri != null && isAdded()) {
                                                    HomeActivity.bottomNavBar.setItemSelected(R.id.new_post_fragment, true);
                                                }
                                            }
                                        }).repeat(0).duration(1000).playOn(fragmentView.findViewById(R.id.constraintLayout2));


                                    }, 1500);

                                }
                            }, 0);
                        }

                        progressBar.setVisibility(View.GONE);
                        callback.onComplete();

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
    public void onResume() {
        super.onResume();
        allPostsList = new ArrayList<>();
        allPostsList.addAll(postModelArrayList);
        if (!HomeActivity.signedInAnonymously && HomeActivity.lastHomePosition != -1 && postListToRestore != null) {
            if (postListToRestore.size() - lastHomePosition <= 2) {
                recyclerView.scrollToPosition(postListToRestore.size());
            } else {
                int savedPosition = HomeActivity.lastHomePosition;
                recyclerView.scrollToPosition(savedPosition - 1);
                postModelArrayList.clear();
                postModelArrayList.addAll(postListToRestore);
                recyclerAdapter.notifyDataSetChanged();
            }

        }
    }

    @Override
    public void onStop() {
        super.onStop();
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager != null && !HomeActivity.signedInAnonymously) {
            HomeActivity.lastHomePosition = layoutManager.findFirstCompletelyVisibleItemPosition() + 1;
            postListToRestore = randomArrayList;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Context context = this.getContext();
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        if (HomeActivity.signedInAnonymously) {
            // Disable bottom nav options on anonymous mode
            HomeActivity.bottomNavBar.setItemEnabled(R.id.new_post_fragment, false);
            HomeActivity.bottomNavBar.setItemEnabled(R.id.my_profile_fragment, false);
        }

        if (!HomeActivity.showLoadingScreen) {
            view.findViewById(R.id.constraintLayout2).setVisibility(View.INVISIBLE);
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        /*if (user == null && !HomeActivity.signedInAnonymously){
            Toast.makeText(context, "You have been banned from Social Meme. ", Toast.LENGTH_LONG).show();
        }*/

        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.root_view);
        TextView usernameLoadingScreen = view.findViewById(R.id.textView40);
        searchUserBtn = view.findViewById(R.id.searchPersonButton);

        if (postListToRestore == null) {
            randomArrayList = new ArrayList<PostModel>();
        } else {
            randomArrayList = postListToRestore;
        }

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
        recyclerAdapter = new PostRecyclerAdapter(randomArrayList, getContext(), getActivity());
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(layoutManager);

        if (!HomeActivity.signedInAnonymously && user != null) {

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

                int totalRecyclerViewItems = recyclerView.getAdapter().getItemCount();
                if ((totalRecyclerViewItems - layoutManager.findFirstVisibleItemPosition()) < 5) {
                    YoYo.with(Techniques.FadeOut)
                            .duration(600)
                            .onEnd(animator -> goUp.setVisibility(View.INVISIBLE))
                            .playOn(goUp);
                } else {
                    YoYo.with(Techniques.FadeIn)
                            .duration(600)
                            .onEnd(animator -> goUp.setVisibility(View.VISIBLE))
                            .playOn(goUp);
                }
            }
        });

        // Refresh posts
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (!HomeActivity.signedInAnonymously) {
                swipeRefreshLayout.setRefreshing(true);
                getAllPostsFromDB(true, view, swipeRefreshLayout);
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        if (HomeActivity.showLoadingScreen) {
            HomeActivity.bottomNavBar.setVisibility(View.GONE);
            swipeRefreshLayout.setEnabled(false);
            view.findViewById(R.id.constraintLayout2).setVisibility(View.VISIBLE);
        }

        if (lastHomePosition == -1 || postListToRestore == null) {
            getAllPostsFromDB(false, view, swipeRefreshLayout);
        } else {
            recyclerAdapter.notifyDataSetChanged();
        }


        filtersBtn.setOnClickListener(view13 -> showFiltersDialog());

        notificationsBtn.setOnClickListener(view12 -> {
            startActivity(new Intent(getActivity(), NotificationsActivity.class));
            CustomIntent.customType(getContext(), "left-to-right");
        });

        searchUserBtn.setOnClickListener(view1 -> {
            startActivity(new Intent(getActivity(), SearchUserActivity.class));
            CustomIntent.customType(getContext(), "left-to-right");
        });

        return view;
    }

}