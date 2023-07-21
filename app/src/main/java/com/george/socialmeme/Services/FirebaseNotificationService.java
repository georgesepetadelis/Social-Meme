package com.george.socialmeme.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.media.AudioAttributes;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.george.socialmeme.Activities.HomeActivity;
import com.george.socialmeme.Activities.PostActivity;
import com.george.socialmeme.Activities.SplashScreenActivity;
import com.george.socialmeme.Activities.UserProfileActivity;
import com.george.socialmeme.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class FirebaseNotificationService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        // Update token on real-time DB
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(user.getUid());
            userRef.child("fcm_token").setValue(token);
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> dataMap = remoteMessage.getData();

        String notificationTitle = remoteMessage.getNotification().getTitle();
        String notificationBody = remoteMessage.getNotification().getBody();
        String type = dataMap.get("type");

        Intent homeIntent = new Intent(getApplicationContext(), SplashScreenActivity.class);

        if (type != null) {
            if (type.equals("user")) {
                homeIntent = new Intent(getApplicationContext(), UserProfileActivity.class);
                String userID = dataMap.get("userID");
                homeIntent.putExtra("user_id", userID);
                homeIntent.putExtra("allPosts", HomeActivity.savedPostsArrayList);
            } else if (type.equals("post")) {
                homeIntent = new Intent(getApplicationContext(), PostActivity.class);
                String postID = dataMap.get("postID");
                homeIntent.putExtra("post_id", postID);
            }
        }

        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 100, homeIntent,
                PendingIntent.FLAG_IMMUTABLE);

        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build();

        Uri sound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                getApplicationContext().getPackageName() + "/" + R.raw.notification_sound);

        final String CHANNEL_ID = "firebase_fcm";
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "firebase_fcm", NotificationManager.IMPORTANCE_HIGH);
        channel.enableVibration(true);
        channel.setImportance(NotificationManager.IMPORTANCE_HIGH);
        channel.setSound(sound, attributes);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);

        // Setting different notification ID to
        // prevent notification replacement from another notification
        int notificationID = ThreadLocalRandom.current().nextInt(2, 1000);

        int icon_res = R.drawable.app_logo;

        switch (notificationTitle) {

            case "New like":
                icon_res = R.drawable.ic_like_modern;
                break;

            case "Meme saved":
            case "Profile screenshot":
                icon_res = R.drawable.ic_camera_modern;
                break;

            case "New comment":
                icon_res = R.drawable.ic_comment;
                break;

            case "New profile visitor":
            case "You lost a follower :(":
            case "New follower":
                icon_res = R.drawable.user;
                break;

            default:
                icon_res = R.drawable.app_logo;

        }

        Bitmap icon = BitmapFactory.decodeResource(getResources(), icon_res);

        Notification.Builder notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .setSmallIcon(R.drawable.sm_notifications)
                .setLargeIcon(icon)
                .setAutoCancel(true);
        notification.setContentIntent(pendingIntent);
        NotificationManagerCompat.from(this).notify(notificationID, notification.build());

    }
}
