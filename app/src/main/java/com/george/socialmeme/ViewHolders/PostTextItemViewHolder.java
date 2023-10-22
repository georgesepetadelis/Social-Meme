package com.george.socialmeme.ViewHolders;

import static com.george.socialmeme.Helpers.NotificationHelper.sendNotification;
import static com.george.socialmeme.Helpers.PostHelper.followPostAuthor;
import static com.george.socialmeme.Helpers.PostHelper.showCommentsDialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.george.socialmeme.Activities.HomeActivity;
import com.george.socialmeme.Activities.UserProfileActivity;
import com.george.socialmeme.BuildConfig;
import com.george.socialmeme.Models.CommentModel;
import com.george.socialmeme.Models.PostModel;
import com.george.socialmeme.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import maes.tech.intentanim.CustomIntent;

public class PostTextItemViewHolder extends RecyclerView.ViewHolder {

    public CardView container;
    Activity activity;
    public Context context;
    public String postID, postAuthorID;
    public PostModel postModel;
    public HashMap<String, CommentModel> comments;
    public View openProfileView, openCommentsView;
    public TextView username, like_counter_tv, commentsCount, postTitle, postContentText, followBtn;
    public CircleImageView profilePicture;
    public ImageButton like_btn, postOptionsButton, shareBtn, commentsBtn, saveBtn;
    public boolean isPostLiked;
    public ConstraintLayout followBtnView;

    public PostTextItemViewHolder(@NonNull View itemView) {
        super(itemView);
        container = itemView.findViewById(R.id.post_text_container);
        profilePicture = itemView.findViewById(R.id.user_profile_image);
        username = itemView.findViewById(R.id.post_username);
        like_btn = itemView.findViewById(R.id.likeBtn);
        like_counter_tv = itemView.findViewById(R.id.like_counter);
        openProfileView = itemView.findViewById(R.id.view_profile);
        postOptionsButton = itemView.findViewById(R.id.imageButton15);
        commentsCount = itemView.findViewById(R.id.textView63);
        openCommentsView = itemView.findViewById(R.id.openCommentsViewImageItem);
        shareBtn = itemView.findViewById(R.id.imageButton13);
        commentsBtn = itemView.findViewById(R.id.show_comments_btn);
        postTitle = itemView.findViewById(R.id.textView79);
        postContentText = itemView.findViewById(R.id.textView80);
        followBtnView = itemView.findViewById(R.id.follow_btn_view);
        followBtn = itemView.findViewById(R.id.textView81);
        saveBtn = itemView.findViewById(R.id.imageButton24);

        openCommentsView.setOnClickListener(view -> showCommentsDialog(comments, username, commentsCount, context, postID));
        postOptionsButton.setOnClickListener(view -> showPostOptionsBottomSheet());
        followBtn.setOnClickListener(view -> followPostAuthor(context, postModel, followBtn, username));
        shareBtn.setOnClickListener(v -> showShareOptions());

        if (!HomeActivity.singedInAnonymously && !username.getText().toString().equals(user.getDisplayName())) {
            followBtnView.setVisibility(View.VISIBLE);
        } else {
            followBtnView.setVisibility(View.GONE);
        }

        if (HomeActivity.singedInAnonymously) {
            saveBtn.setVisibility(View.GONE);
        }

        saveBtn.setOnClickListener(v -> {
            saveBtn.setEnabled(false);
            saveBtn.setAlpha(0.5f);
            try {
                saveTextAsImageToDevice();
            } catch (IOException e) {
                Toast.makeText(activity, "Error saving file", Toast.LENGTH_SHORT).show();
            }
        });

        // Check if logged-in user follows post author
        // to hide follow btn
        if (user != null) {
            usersRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.hasChild("following")) {
                        // Logged-in user follows post author
                        // hide follow btn
                        if (postAuthorID != null) {
                            if (snapshot.child("following").child(postAuthorID).exists()) {
                                followBtnView.setVisibility(View.GONE);
                            }
                        } else {
                            followBtnView.setVisibility(View.GONE);
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        postTitle.setOnClickListener(v -> copyToClipboard(postTitle));
        postContentText.setOnClickListener(v -> copyToClipboard(postContentText));

        like_btn.setOnClickListener(v -> {

            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
            // Animate like button when clicked
            YoYo.with(Techniques.Shake)
                    .duration(500)
                    .repeat(0)
                    .playOn(like_btn);

            if (isPostLiked) {
                isPostLiked = false;
                like_btn.setImageResource(R.drawable.ic_like);
                likesRef.child(postID).child(user.getUid()).removeValue();
                updateLikesToDB(postID, false);
            } else {
                isPostLiked = true;
                like_btn.setImageResource(R.drawable.ic_like_filled);
                likesRef.child(postID).child(user.getUid()).setValue("true");
                updateLikesToDB(postID, true);

                if (!user.getUid().equals(postAuthorID)) {
                    sendNotification(context, username, postModel.getId(), "like", "");
                }
            }

        });

        if (HomeActivity.singedInAnonymously) {
            openProfileView.setEnabled(false);
        }

        openProfileView.setOnClickListener(v -> {

            if (postAuthorID != null) {
                if (postAuthorID.equals(user.getUid())) {
                    if (HomeActivity.bottomNavBar == null) {
                        Intent intent = new Intent(context, UserProfileActivity.class);
                        intent.putExtra("user_id", postAuthorID);
                        intent.putExtra("username", username.getText().toString());
                        context.startActivity(intent);
                        CustomIntent.customType(context, "left-to-right");
                    } else {
                        int selectedItemId = HomeActivity.bottomNavBar.getSelectedItemId();
                        if (selectedItemId != R.id.my_profile_fragment) {
                            HomeActivity.bottomNavBar.setItemSelected(R.id.my_profile_fragment, true);
                        }
                    }
                } else {
                    Intent intent = new Intent(context, UserProfileActivity.class);
                    intent.putExtra("user_id", postAuthorID);
                    intent.putExtra("username", username.getText().toString());
                    context.startActivity(intent);
                    CustomIntent.customType(context, "left-to-right");
                }
            }
        });

        openProfileView.setOnLongClickListener(view -> {
            copyUsernameToClipboard();
            return true;
        });

    }

    private static void scanFile(Context context, Uri imageUri) {
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        scanIntent.setData(imageUri);
        context.sendBroadcast(scanIntent);
    }

    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = auth.getCurrentUser();

    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
    DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("posts");
    DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference("likes");

    public void setContext(Context context) {
        this.context = context;
    }
    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public Bitmap createBitmapFromView(CardView cardView) {

        int width = cardView.getWidth();
        int height = cardView.getHeight();

        // Create a new bitmap with the same dimensions as the view.
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        cardView.draw(canvas);

        // Draw the rounded corners of the card view.
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        RectF rect = new RectF(0, 0, width, height);

        // Create a new bitmap with the rounded corners.
        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas outputCanvas = new Canvas(output);

        final Rect rect1 = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rect2 = new RectF(rect1);
        final float roundPx = cardView.getCardElevation();

        paint.setAntiAlias(true);
        outputCanvas.drawRoundRect(rect2, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        outputCanvas.drawBitmap(bitmap, rect1, rect1, paint);

        bitmap.setHasAlpha(true);
        output.setHasAlpha(true);

        return output;
    }

    void changePostInfoVisibilityForBitmap(int visible_state) {
        postOptionsButton.setVisibility(visible_state);
        shareBtn.setVisibility(visible_state);
    }

    private void shareToInstagramStory() throws IOException {

        // Download image of post and bg image in order to get the image uri
        // Deleting these images after activity launched
        changePostInfoVisibilityForBitmap(View.INVISIBLE);
        Bitmap bmp = createBitmapFromView(container);
        File storageLoc = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File file = new File(storageLoc, postID + ".png");

        try {

            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            scanFile(context, Uri.fromFile(file));

            // Saving story background to local storage
            Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.sm_instagram_story);
            File bg_file = new File(storageLoc, "bg_image.jpg");
            FileOutputStream fos_bg = new FileOutputStream(bg_file);
            bm.compress(Bitmap.CompressFormat.PNG, 100, fos_bg);
            fos.close();
            scanFile(context, Uri.fromFile(bg_file));

            Uri bg_uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", bg_file);
            Uri postImageUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);

            Intent intent = new Intent("com.instagram.share.ADD_TO_STORY");
            intent.setType("image/*");
            intent.setPackage("com.instagram.android");
            String sourceApplication = "650794227085896";
            intent.putExtra("source_application", sourceApplication);
            intent.setDataAndType(bg_uri, "image/*");
            intent.putExtra("interactive_asset_uri", postImageUri);
            intent.putExtra("content_url", "https://play.google.com/store/apps/details?id=com.george.socialmeme&hl=en&gl=US");
            intent.putExtra("top_background_color", "#33FF33");
            intent.putExtra("bottom_background_color", "#33FF33");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            activity.grantUriPermission(
                    "com.instagram.android", postImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
                activity.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(activity, "Instagram is not installed!", Toast.LENGTH_SHORT).show();
            }

            // Delete temp images with delay
            // so the instagram activity can access them before deletion
            new Handler().postDelayed(() -> {
                bg_file.delete();
                file.delete();
                changePostInfoVisibilityForBitmap(View.VISIBLE);
            }, 6500);


        } catch (FileNotFoundException e) {
            Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(context, "Error sharing to story", Toast.LENGTH_SHORT).show();
        }

    }

    void copyToClipboard(TextView textView) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("username", textView.getText().toString());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, "Text copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    private void deletePost() {
        postsRef.child(postID).removeValue().addOnCompleteListener(task -> {
            like_btn.setVisibility(View.GONE);
            like_counter_tv.setVisibility(View.GONE);
            openCommentsView.setEnabled(false);
            openCommentsView.setEnabled(false);
            username.setText("DELETED POST");
            postTitle.setText("DELETED POST");
            postContentText.setText("DELETED POST");
            profilePicture.setImageResource(R.drawable.user);
            openProfileView.setEnabled(false);
            postOptionsButton.setVisibility(View.GONE);
        });
    }

    public void saveTextAsImageToDevice() throws IOException {
        changePostInfoVisibilityForBitmap(View.INVISIBLE);
        Bitmap bmp = createBitmapFromView(container);
        File storageLoc = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File file = new File(storageLoc, postID + ".png");
        FileOutputStream fos = new FileOutputStream(file);
        bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
        fos.close();
        scanFile(context, Uri.fromFile(file));
        Toast.makeText(context, "Meme saved to " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        postOptionsButton.setVisibility(View.VISIBLE);
        sendNotification(context, username, postModel.getId(), "meme_saved", "");
    }

    public void showPostOptionsBottomSheet() {

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.post_options_bottom_sheet);

        View downloadMemeView = dialog.findViewById(R.id.view13);
        View reportPostView = dialog.findViewById(R.id.view15);
        View deletePostView = dialog.findViewById(R.id.view17);

        // Hide delete view if the current logged in user is not
        // the author of the current post
        if (!username.getText().toString().equals(user.getDisplayName())) {
            dialog.findViewById(R.id.delete_post_view).setVisibility(View.GONE);
        }

        downloadMemeView.setOnClickListener(view -> {
            try {
                saveTextAsImageToDevice();
            } catch (IOException e) {
                Toast.makeText(activity, "Error: Can't save this meme", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        });

        reportPostView.setOnClickListener(view -> {
            DatabaseReference reportsRef = FirebaseDatabase.getInstance().getReference("reportedPosts");
            FirebaseAuth auth = FirebaseAuth.getInstance();
            reportsRef.child(auth.getCurrentUser().getUid()).setValue(postID).addOnCompleteListener(task -> {
                like_btn.setVisibility(View.GONE);
                like_counter_tv.setVisibility(View.GONE);
                openCommentsView.setEnabled(false);
                openCommentsView.setEnabled(false);
                username.setText("REPORTED POST");
                postTitle.setText("REPORTED POST");
                postContentText.setText("REPORTED POST");
                profilePicture.setImageResource(R.drawable.user);
                openProfileView.setEnabled(false);
                postOptionsButton.setVisibility(View.GONE);
                reportsRef.child(postID);
                FirebaseDatabase.getInstance().getReference("posts").child(postID).child("reported").setValue("true");
                Toast.makeText(context, "Report received, thank you!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        });

        deletePostView.setOnClickListener(view -> {
            new AlertDialog.Builder(context)
                    .setTitle("Are you sure?")
                    .setMessage("Are you sure you want to delete this meme?. This action cannot be undone.")
                    .setIcon(R.drawable.ic_report)
                    .setPositiveButton("Yes", (dialogInterface, i) -> {
                        deletePost();
                        dialog.dismiss();
                    })
                    .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss())
                    .show();
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.BottomSheetDialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    void showShareOptions() {

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.share_options_bottom_sheet);

        View shareToStory = dialog.findViewById(R.id.view13);
        View copyImageLink = dialog.findViewById(R.id.view15);

        TextView copyImageLink_tv = dialog.findViewById(R.id.copy_img_url_text);
        ImageView copyImageLink_img = dialog.findViewById(R.id.imageView28);
        ConstraintLayout constraintLayout_copyImageLink = dialog.findViewById(R.id.copy_link);

        constraintLayout_copyImageLink.setVisibility(View.GONE);
        copyImageLink_img.setVisibility(View.GONE);
        copyImageLink_tv.setVisibility(View.GONE);
        copyImageLink.setVisibility(View.GONE);

        shareToStory.setOnClickListener(v -> {
            PackageManager pm = context.getPackageManager();
            if (HomeActivity.isInstagramInstalled(pm)) {
                try {
                    shareToInstagramStory();
                } catch (IOException e) {
                    Toast.makeText(activity, "error " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    //throw new RuntimeException(e);
                }
            } else {
                Toast.makeText(context, "Instagram is not installed!", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.BottomSheetDialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    public void updateLikesToDB(String postID, boolean isNotLiked) {

        String currentLikesToString = like_counter_tv.getText().toString();
        int currentLikesToInt = Integer.parseInt(currentLikesToString);

        if (isNotLiked) {

            int newCurrentLikes = currentLikesToInt + 1;
            String newCurrentLikesToString = Integer.toString(newCurrentLikes);

            // Update likes on Real-time DB
            postsRef.child(postID).child("likes").setValue(newCurrentLikesToString);

            // update likes on TextView
            like_counter_tv.setText(newCurrentLikesToString);

            // Animate like counter TextView
            YoYo.with(Techniques.FadeInUp)
                    .duration(500)
                    .repeat(0)
                    .playOn(like_counter_tv);

        } else {

            int newCurrentLikes = currentLikesToInt - 1;
            String newCurrentLikesToString = Integer.toString(newCurrentLikes);

            // Update likes on Real-time DB
            postsRef.child(postID).child("likes").setValue(newCurrentLikesToString);

            // update likes on TextView
            like_counter_tv.setText(newCurrentLikesToString);

            // Animate like counter TextView
            YoYo.with(Techniques.FadeInDown)
                    .duration(500)
                    .repeat(0)
                    .playOn(like_counter_tv);

        }

    }

    void copyUsernameToClipboard() {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("username", username.getText().toString());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, "Username copied to clipboard", Toast.LENGTH_SHORT).show();
    }

}
