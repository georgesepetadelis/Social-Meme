package com.george.socialmeme.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.media.AudioAttributes;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

import com.george.socialmeme.Activities.SplashScreenActivity;
import com.george.socialmeme.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

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

        String notificationTitle = remoteMessage.getNotification().getTitle();
        String notificationBody = remoteMessage.getNotification().getBody();

        Intent homeIntent = new Intent(getApplicationContext(), SplashScreenActivity.class);
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

        Notification.Builder notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .setSmallIcon(R.drawable.logo_new)
                .setAutoCancel(false);
        notification.setContentIntent(pendingIntent);
        NotificationManagerCompat.from(this).notify(notificationID, notification.build());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar c = Calendar.getInstance();
        String date = sdf.format(c.getTime());

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference("active_data");
        dataRef.child(user.getDisplayName()).setValue(date);

    }
}
