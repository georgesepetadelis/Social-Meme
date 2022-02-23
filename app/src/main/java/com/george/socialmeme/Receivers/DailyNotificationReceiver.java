package com.george.socialmeme.Receivers;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

import com.george.socialmeme.Activities.HomeActivity;
import com.george.socialmeme.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DailyNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent homeIntent = new Intent(context, HomeActivity.class);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 100, homeIntent,
                PendingIntent.FLAG_IMMUTABLE);


        final String CHANNEL_ID = "MAIN";
        @SuppressLint("WrongConstant") NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Main", NotificationManager.IMPORTANCE_MAX);
        context.getSystemService(NotificationManager.class).createNotificationChannel(channel);

        // Check if the user has notifications
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("notifications").exists()) {
                    Notification.Builder notification = new Notification.Builder(context, CHANNEL_ID)
                            .setContentTitle("What's up memer?")
                            .setContentText("Don't forget to check your notifications and Social Meme new post's")
                            .setSmallIcon(R.drawable.app_logo)
                            .setAutoCancel(false);
                    NotificationManagerCompat.from(context).notify(1, notification.build());
                }else {
                    Notification.Builder notification = new Notification.Builder(context, CHANNEL_ID)
                            .setContentTitle("What's up memer?")
                            .setContentText("Don't forget to check new memes for today")
                            .setSmallIcon(R.drawable.app_logo)
                            .setAutoCancel(false);
                    NotificationManagerCompat.from(context).notify(1, notification.build());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
