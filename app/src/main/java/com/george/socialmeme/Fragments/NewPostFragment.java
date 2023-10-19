package com.george.socialmeme.Fragments;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.claudylab.smartdialogbox.SmartDialogBox;
import com.developer.kalert.KAlertDialog;
import com.george.socialmeme.Activities.HomeActivity;
import com.george.socialmeme.Activities.RegisterActivity;
import com.george.socialmeme.Models.PostModel;
import com.george.socialmeme.Models.UploadPostModel;
import com.george.socialmeme.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import maes.tech.intentanim.CustomIntent;

public class NewPostFragment extends Fragment {

    final private DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("posts");
    final private DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
    final private StorageReference storageReference = FirebaseStorage.getInstance().getReference("images");
    private Uri mediaUri;
    private static String mediaType;
    final private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    final private FirebaseUser user = mAuth.getCurrentUser();
    KAlertDialog loadingDialog;

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {

                    Uri returnUri = result.getData().getData();
                    Cursor returnCursor =
                            getContext().getContentResolver().query(returnUri, null, null, null, null);

                    int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                    returnCursor.moveToFirst();
                    long fileSizeToInt = returnCursor.getLong(sizeIndex) / 1024 / 1024;

                    if (fileSizeToInt <= 60) {
                        AlertDialog dialog = new AlertDialog.Builder(getContext())
                                .setTitle("Upload new meme?")
                                .setIcon(R.drawable.ic_upload)
                                .setMessage("Are you sure you want to upload this file?\n" /*+ returnCursor.getString(nameIndex)*/)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent data = result.getData();
                                        mediaUri = data.getData();
                                        uploadPostToFirebase(mediaUri, mediaType);
                                    }
                                })
                                .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss())
                                .create();
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.getWindow().setBackgroundDrawableResource(R.drawable.custom_dialog_background);
                        dialog.show();
                    } else {
                        new AlertDialog.Builder(getContext())
                                .setTitle("Whoops!")
                                .setMessage("Your file is larger than 60mb, please choose a smaller one!" /*+ "\nFile: " + result.getData().getData().getPath().toString()*/)
                                .setPositiveButton("Yes", (dialogInterface, i) -> dialogInterface.dismiss())
                                .show();
                    }


                }
            });

    void uploadIncomingFile() {
        Uri fileUri = HomeActivity.fileUri;
        if (fileUri != null) {

            // Using try/catch to make sure that this feature works on oneUI
            // Skiping file size check in oneUI
            // In oneUI fileSizeToInt always return null, so we're doing this
            // workaround to until a new way to get the file size is found
            try {
                Cursor returnCursor =
                        getContext().getContentResolver().query(fileUri, null, null, null, null);
                int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                returnCursor.moveToFirst();
                long fileSizeToInt = returnCursor.getLong(sizeIndex) / 1024 / 1024;

                AlertDialog dialog;
                if (fileSizeToInt <= 60) {
                    dialog = new AlertDialog.Builder(getContext())
                            .setTitle("Upload new meme?")
                            .setIcon(R.drawable.ic_upload)
                            .setMessage("Are you sure you want to upload this " + HomeActivity.IncomingPostType + "?" /*+ returnCursor.getString(nameIndex)*/)
                            .setPositiveButton("Yes", (dialogInterface, i) -> uploadPostToFirebase(fileUri, HomeActivity.IncomingPostType))
                            .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss())
                            .create();

                } else {
                    dialog = new AlertDialog.Builder(getContext())
                            .setTitle("Whoops!")
                            .setMessage("Your file is larger than 60mb, please choose a smaller one!" /*+ "\nFile: " + result.getData().getData().getPath().toString()*/)
                            .setPositiveButton("Yes", (dialogInterface, i) -> dialogInterface.dismiss())
                            .create();
                }
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().setBackgroundDrawableResource(R.drawable.custom_dialog_background);
                dialog.show();
            } catch (Exception e) {
                // failed to check file size
                AlertDialog dialog;
                dialog = new AlertDialog.Builder(getContext())
                        .setTitle("Upload new meme?")
                        .setIcon(R.drawable.ic_upload)
                        .setMessage("Are you sure you want to upload this " + HomeActivity.IncomingPostType + "?" /*+ returnCursor.getString(nameIndex)*/)
                        .setPositiveButton("Yes", (dialogInterface, i) -> uploadPostToFirebase(fileUri, HomeActivity.IncomingPostType))
                        .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss())
                        .create();
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().setBackgroundDrawableResource(R.drawable.custom_dialog_background);
                dialog.show();
            }


        }
    }

    @SuppressLint("Range")
    public static String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void notifyEveryoneAboutFirstPost(String postID) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    if (snap.child("name").getValue(String.class) != null
                            && snap.child("id").getValue(String.class) != null) {

                        String id = snap.child("id").getValue(String.class);
                        String name = snap.child("name").getValue(String.class);

                        if (snap.child("fcm_token").exists() &&  !name.equals(user.getDisplayName())) {

                            String token = snap.child("fcm_token").getValue(String.class);

                            String firestoreNotificationID = usersRef.push().getKey();
                            Map<String, Object> notification = new HashMap<>();
                            notification.put("token", token);
                            notification.put("postID", postID);
                            notification.put("title", user.getDisplayName() + " uploaded their first meme");
                            notification.put("message", "See the first meme from " + user.getDisplayName());
                            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                            firestore.collection("notifications")
                                    .document(firestoreNotificationID).set(notification);

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void sendNewPostNotificationToFollowers(String postID) {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(user.getUid()).child("followers").exists()) {
                    for (DataSnapshot follower : snapshot.child(user.getUid()).child("followers").getChildren()) {
                        // Check if token exists
                        if (snapshot.child(follower.getValue(String.class)).child("fcm_token").exists()) {
                            String userToken = snapshot.child(follower.getValue(String.class))
                                    .child("fcm_token").getValue(String.class);
                            // add notification to Firestore to send
                            // push notification from back-end
                            String firestoreNotificationID = usersRef.push().getKey();
                            Map<String, Object> notification = new HashMap<>();
                            notification.put("token", userToken);
                            notification.put("postID", postID);
                            notification.put("title", user.getDisplayName() + " just uploaded a new meme");
                            notification.put("message", "See the latest meme from " + user.getDisplayName());
                            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                            firestore.collection("notifications")
                                    .document(firestoreNotificationID).set(notification);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getFileExtension(Uri uri) {
        if (uri != null && isAdded()) {
            ContentResolver cr = getContext().getContentResolver();
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            return mime.getExtensionFromMimeType(cr.getType(uri));
        } else {
            return "no_extension";
        }
    }

    boolean isNightModeEnabled() {
        SharedPreferences sharedPref = getContext().getSharedPreferences("dark_mode", MODE_PRIVATE);
        return sharedPref.getBoolean("dark_mode", false);
    }

    private void uploadPostToFirebase(Uri uri, String type) {

        String postId = postsRef.push().getKey();
        AtomicBoolean uploadCancelled = new AtomicBoolean(false);

        if (type != null && postId != null) {
            if (type.equals("text")) {

                AlertDialog dialog;

                // Set dialog theme
                if (isNightModeEnabled()) {
                    dialog = new AlertDialog.Builder(getContext()).create();
                    Window window = dialog.getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setStatusBarColor(Color.BLACK);
                } else {
                    dialog = new AlertDialog.Builder(getContext()).create();
                    Window window = dialog.getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setStatusBarColor(Color.WHITE);
                }

                LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View dialogView = layoutInflater.inflate(R.layout.upload_text_dialog, null);

                EditText jokeTitleEditText = dialogView.findViewById(R.id.joke_title);
                EditText jokeContentEditText = dialogView.findViewById(R.id.joke_content);
                Button uploadJokeButton = dialogView.findViewById(R.id.upload_joke_btn);
                ImageButton dismissDialogButton = dialogView.findViewById(R.id.dismissDialogBtn);

                uploadJokeButton.setOnClickListener(view -> {

                    loadingDialog.show();

                    if (jokeTitleEditText.getText().toString().isEmpty() || jokeContentEditText.getText().toString().isEmpty()) {
                        SmartDialogBox.showErrorDialog(getActivity(), "Title or joke content cannot be empty", "OK");
                        loadingDialog.hide();
                    } else {
                        dialog.dismiss();
                        UploadPostModel model = new UploadPostModel();
                        postsRef.child(postId).setValue(model);
                        postsRef.child(postId).child("name").setValue(user.getDisplayName());
                        postsRef.child(postId).child("likes").setValue("0");
                        postsRef.child(postId).child("id").setValue(postId);
                        postsRef.child(postId).child("postType").setValue(type);
                        postsRef.child(postId).child("joke_title").setValue(jokeTitleEditText.getText().toString());
                        postsRef.child(postId).child("joke_content").setValue(jokeContentEditText.getText().toString());

                        if (user.getPhotoUrl() != null) {
                            postsRef.child(postId).child("authorProfilePictureURL").setValue(user.getPhotoUrl().toString());
                        } else {
                            postsRef.child(postId).child("authorProfilePictureURL").setValue("none")
                                    .addOnCompleteListener(task -> loadingDialog.hide());
                        }

                        PostModel postModel = new PostModel();
                        postModel.setId(postId);
                        postModel.setAuthorID(user.getUid());
                        postModel.setName(user.getDisplayName());
                        postModel.setJoke_title(jokeTitleEditText.getText().toString());
                        postModel.setJoke_content(jokeContentEditText.getText().toString());
                        postModel.setLikes("0");
                        postModel.setCommentsCount("0");
                        postModel.setPostType(type);

                        if (user.getPhotoUrl() == null) {
                            postModel.setAuthorProfilePictureURL("none");
                        } else {
                            postModel.setAuthorProfilePictureURL(user.getPhotoUrl().toString());
                        }

                        if (HomeActivity.savedPostsArrayList.size() != 0) {
                            HomeActivity.savedPostsArrayList.remove(HomeActivity.savedPostsArrayList.size() - 1);
                        }

                        HomeActivity.savedPostsArrayList.add(postModel);

                        // Add post's of the month view as RecyclerView item
                        // to avoid using ScrollView
                        PostModel postsOfTheMonthView = new PostModel();
                        postsOfTheMonthView.setPostType("postsOfTheMonth");
                        HomeActivity.savedPostsArrayList.add(postsOfTheMonthView);

                        if (!HomeActivity.userHasPosts) {
                            HomeActivity.userHasPosts = true;
                            notifyEveryoneAboutFirstPost(postId);
                        }

                        Toast.makeText(getActivity(), "Joke uploaded!", Toast.LENGTH_SHORT).show();
                        sendNewPostNotificationToFollowers(postId);
                        HomeActivity.bottomNavBar.setItemSelected(R.id.home_fragment, true);
                        loadingDialog.hide();

                        getActivity().startActivity(new Intent(getActivity(), HomeActivity.class));
                        CustomIntent.customType(getActivity(), "fadein-to-fadeout");
                        getActivity().finish();

                    }
                });

                dismissDialogButton.setOnClickListener(view -> dialog.dismiss());

                dialog.getWindow().getAttributes().windowAnimations = R.style.BottomSheetDialogAnimation;
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.setView(dialogView);
                dialog.show();

            }

            if (type.equals("audio")) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity())
                        .setTitle("Give a name to your audio")
                        .setCancelable(false);
                final EditText audioNameET = new EditText(getActivity());
                audioNameET.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                audioNameET.setHint("Write a name");
                dialog.setView(audioNameET);

                dialog.setPositiveButton("Ok", (dialogInterface, i) -> {
                    if (!audioNameET.getText().toString().isEmpty()) {

                        StorageReference fileRef = storageReference.child(postId + "." + getFileExtension(uri));
                        fileRef.putFile(uri).addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl()
                                .addOnSuccessListener(uri1 -> {
                                    loadingDialog.hide();

                                    UploadPostModel model = new UploadPostModel(uri1.toString());
                                    postsRef.child(postId).setValue(model);
                                    postsRef.child(postId).child("name").setValue(user.getDisplayName());
                                    postsRef.child(postId).child("likes").setValue("0");
                                    postsRef.child(postId).child("id").setValue(postId);
                                    postsRef.child(postId).child("postType").setValue(type);

                                    if (!audioNameET.getText().toString().isEmpty()) {
                                        postsRef.child(postId).child("audioName").setValue(audioNameET.getText().toString());
                                    }

                                    if (user.getPhotoUrl() != null) {
                                        postsRef.child(postId).child("authorProfilePictureURL").setValue(user.getPhotoUrl().toString());
                                    } else {
                                        postsRef.child(postId).child("authorProfilePictureURL").setValue("none");
                                    }

                                    PostModel postModel = new PostModel();
                                    postModel.setId(postId);
                                    postModel.setAuthorID(user.getUid());
                                    postModel.setName(user.getDisplayName());
                                    postModel.setAudioName(audioNameET.getText().toString());
                                    postModel.setLikes("0");
                                    postModel.setCommentsCount("0");
                                    postModel.setPostType(type);
                                    postModel.setImgUrl(uri1.toString());

                                    if (user.getPhotoUrl() == null) {
                                        postModel.setAuthorProfilePictureURL("none");
                                    }else {
                                        postModel.setAuthorProfilePictureURL(user.getPhotoUrl().toString());
                                    }

                                    if (HomeActivity.savedPostsArrayList.size() != 0) {
                                        HomeActivity.savedPostsArrayList.remove(HomeActivity.savedPostsArrayList.size() - 1);
                                    }

                                    HomeActivity.savedPostsArrayList.add(postModel);

                                    // Add post's of the month view as RecyclerView item
                                    // to avoid using ScrollView
                                    PostModel postsOfTheMonthView = new PostModel();
                                    postsOfTheMonthView.setPostType("postsOfTheMonth");
                                    HomeActivity.savedPostsArrayList.add(postsOfTheMonthView);

                                    if (!HomeActivity.userHasPosts) {
                                        HomeActivity.userHasPosts = true;
                                        notifyEveryoneAboutFirstPost(postId);
                                    }

                                    Toast.makeText(getActivity(), "Meme uploaded!", Toast.LENGTH_SHORT).show();
                                    sendNewPostNotificationToFollowers(postId);
                                    HomeActivity.bottomNavBar.setItemSelected(R.id.home_fragment, true);

                                    getActivity().startActivity(new Intent(getActivity(), HomeActivity.class));
                                    CustomIntent.customType(getActivity(), "fadein-to-fadeout");
                                    getActivity().finish();

                                })).addOnProgressListener(snapshot -> {
                            loadingDialog.show();
                        }).addOnFailureListener(e -> {
                            loadingDialog.hide();
                            Toast.makeText(getActivity(), "Upload fail: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });

                    } else {
                        Toast.makeText(getActivity(), "Please provide a name for your sound and try again", Toast.LENGTH_LONG).show();
                        dialogInterface.dismiss();
                    }
                }).setNegativeButton("Cancel", (dialogInterface, i) -> {
                    uploadCancelled.set(true);
                    dialogInterface.dismiss();
                }).show();

                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(550, FrameLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(50, 6, 30, 6);
                audioNameET.setLayoutParams(params);
            }

            else {

                if (uri != null) {
                    StorageReference fileRef = storageReference.child(postId + "." + getFileExtension(uri));
                    fileRef.putFile(uri).addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri1 -> {
                        loadingDialog.hide();

                        UploadPostModel model = new UploadPostModel(uri1.toString());
                        postsRef.child(postId).setValue(model);
                        postsRef.child(postId).child("name").setValue(user.getDisplayName());
                        postsRef.child(postId).child("likes").setValue("0");
                        postsRef.child(postId).child("id").setValue(postId);
                        postsRef.child(postId).child("postType").setValue(type);

                        if (user.getPhotoUrl() != null) {
                            postsRef.child(postId).child("authorProfilePictureURL").setValue(user.getPhotoUrl().toString());
                        } else {
                            postsRef.child(postId).child("authorProfilePictureURL").setValue("none");
                        }

                        PostModel postModel = new PostModel();
                        postModel.setId(postId);
                        postModel.setAuthorID(user.getUid());
                        postModel.setName(user.getDisplayName());
                        postModel.setImgUrl(uri1.toString());
                        postModel.setLikes("0");
                        postModel.setCommentsCount("0");
                        postModel.setPostType(type);

                        if (user.getPhotoUrl() == null) {
                            postModel.setAuthorProfilePictureURL("none");
                        } else {
                            postModel.setAuthorProfilePictureURL(user.getPhotoUrl().toString());
                        }

                        if (HomeActivity.savedPostsArrayList.size() != 0) {
                            HomeActivity.savedPostsArrayList.remove(HomeActivity.savedPostsArrayList.size() - 1);
                        }

                        HomeActivity.savedPostsArrayList.add(postModel);

                        // Add post's of the month view as RecyclerView item
                                // to avoid using ScrollView
                                PostModel postsOfTheMonthView = new PostModel();
                                postsOfTheMonthView.setPostType("postsOfTheMonth");
                                HomeActivity.savedPostsArrayList.add(postsOfTheMonthView);

                                if (!HomeActivity.userHasPosts) {
                                    HomeActivity.userHasPosts = true;
                                    notifyEveryoneAboutFirstPost(postId);
                                }

                                if (isAdded() && getContext() != null) {
                                    Toast.makeText(getContext(), "Meme uploaded!", Toast.LENGTH_SHORT).show();
                                }

                                sendNewPostNotificationToFollowers(postId);
                                HomeActivity.bottomNavBar.setItemSelected(R.id.home_fragment, true);

                                getActivity().startActivity(new Intent(getActivity(), HomeActivity.class));
                                CustomIntent.customType(getActivity(), "fadein-to-fadeout");
                                getActivity().finish();

                            })).addOnProgressListener(snapshot -> loadingDialog.show())
                            .addOnFailureListener(e -> {
                                loadingDialog.hide();
                                Toast.makeText(getActivity(), "Upload fail: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                }

            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_post, container, false);

        AdView mAdView = view.findViewById(R.id.adView2);
        AdRequest adRequest = new AdRequest.Builder().build();

        if (HomeActivity.show_banners) {
            mAdView.loadAd(adRequest);
        }

        loadingDialog = new KAlertDialog(getContext(), KAlertDialog.PROGRESS_TYPE);
        loadingDialog.getProgressHelper().setBarColor(R.color.main);
        loadingDialog.setTitleText("Uploading your meme...");
        loadingDialog.setCancelable(false);

        View selectPicture = view.findViewById(R.id.select_img_btn);
        View selectVideo = view.findViewById(R.id.select_video_btn);
        View selectAudio = view.findViewById(R.id.select_audio_btn);
        View uploadText = view.findViewById(R.id.upload_text_view);

        if (HomeActivity.UploadNewPost && HomeActivity.fileUri != null) {
            HomeActivity.UploadNewPost = false;
            uploadIncomingFile();
        }

        if (HomeActivity.singedInAnonymously) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Sign in required")
                    .setMessage("You need to login to upload memes!")
                    .setPositiveButton("Register/Login", (dialogInterface, i) -> {
                        getActivity().finish();
                        startActivity(new Intent(getActivity(), RegisterActivity.class));
                        CustomIntent.customType(getActivity(), "fadein-to-fadeout");
                    })
                    .setNegativeButton("Later", (dialog, which) -> {
                        getActivity().finish();
                        HomeActivity.singedInAnonymously = true;
                        startActivity(new Intent(getActivity(), HomeActivity.class));
                        CustomIntent.customType(getActivity(), "fadein-to-fadeout");
                        dialog.dismiss();
                    })
                    .setCancelable(false)
                    .show();
        }

        uploadText.setOnClickListener(v -> {
            mediaType = "text";
            uploadPostToFirebase(mediaUri, "text");
        });

        selectAudio.setOnClickListener(view12 -> {
            mediaType = "audio";
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("audio/*");
            activityResultLauncher.launch(intent);
        });

        selectVideo.setOnClickListener(view1 -> {
            mediaType = "video";
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("video/*");
            activityResultLauncher.launch(intent);
        });

        selectPicture.setOnClickListener(v -> {
            mediaType = "image";
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            activityResultLauncher.launch(intent);
        });

        return view;
    }
}