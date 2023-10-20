package com.george.socialmeme.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.george.socialmeme.Activities.PostActivity;
import com.george.socialmeme.Activities.SplashScreenActivity;
import com.george.socialmeme.Activities.UserProfileActivity;
import com.george.socialmeme.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

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
        remoteMessage.getNotification().getImageUrl();

        String notificationTitle = remoteMessage.getNotification().getTitle();
        String notificationBody = remoteMessage.getNotification().getBody();
        String type = dataMap.get("type");
        String url = dataMap.get("url");

        Intent homeIntent = new Intent(getApplicationContext(), SplashScreenActivity.class);

        if (type != null) {
            if (type.equals("user")) {
                homeIntent = new Intent(getApplicationContext(), UserProfileActivity.class);
                String userID = dataMap.get("userID");
                homeIntent.putExtra("user_id", userID);
                //homeIntent.putExtra("allPosts", HomeActivity.savedPostsArrayList);
            } else if (type.equals("post")) {
                homeIntent = new Intent(getApplicationContext(), PostActivity.class);
                String postID = dataMap.get("postID");
                homeIntent.putExtra("post_id", postID);
                PostActivity.postID = postID;
            }
        }

        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 100, homeIntent,
                PendingIntent.FLAG_IMMUTABLE);

        if (url != null) {
            //HomeActivity.openURL(url);
            homeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            pendingIntent = PendingIntent.getActivity(this, 0,
                    homeIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pendingIntent = PendingIntent.getActivity(getApplicationContext(), 100, homeIntent,
                    PendingIntent.FLAG_IMMUTABLE);
        }

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

        int icon_res = switch (notificationTitle) {
            case "New like" -> R.drawable.ic_like_modern;
            case "Meme saved", "Profile screenshot" -> R.drawable.ic_camera_modern;
            case "New comment" -> R.drawable.ic_comment;
            case "New profile visitor", "You lost a follower :(", "New follower" -> R.drawable.user;
            default -> R.drawable.app_logo;
        };

        Bitmap icon = BitmapFactory.decodeResource(getResources(), icon_res);

        Notification.Builder notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .setChannelId("firebase_fcm")
                .setLargeIcon(icon)
                .setStyle(new Notification.DecoratedCustomViewStyle())
                .setSmallIcon(R.drawable.sm_notifications_1)
                .setAutoCancel(true);

        notification.setContentIntent(pendingIntent);
        NotificationManagerCompat.from(this).notify(notificationID, notification.build());

    }
}