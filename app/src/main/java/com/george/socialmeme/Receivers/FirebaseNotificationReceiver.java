package com.george.socialmeme.Receivers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

import com.george.socialmeme.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseNotificationReceiver extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String notificationTitle = remoteMessage.getNotification().getTitle();
        String notificationBody = remoteMessage.getNotification().getBody();

        final String CHANNEL_ID = "MAIN";
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Main", NotificationManager.IMPORTANCE_HIGH);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);

        Notification.Builder notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .setSmallIcon(R.drawable.app_logo)
                .setAutoCancel(false);
        NotificationManagerCompat.from(this).notify(1, notification.build());


    }
}
