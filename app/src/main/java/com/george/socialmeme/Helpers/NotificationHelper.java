package com.george.socialmeme.Helpers;

import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class NotificationHelper {

    public static void sendNotification(Context context, TextView username, String postID,
                                        String notificationType, String commentText) {

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        final String[] notification_message = {"none"};
        final String[] notification_title = {"none"};

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String notificationID = usersRef.push().getKey();
                String currentDate = String.valueOf(android.text.format.DateFormat.format("dd-MM-yyyy", new java.util.Date()));

                Calendar calendar = Calendar.getInstance();
                int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                int currentMinutes = calendar.get(Calendar.MINUTE);

                for (DataSnapshot snap : snapshot.getChildren()) {
                    if (snap.child("name").getValue(String.class) != null) {
                        if (snap.child("name").getValue().toString().equals(username.getText().toString())) {

                            String postAuthorID = snap.child("id").getValue().toString();
                            usersRef.child(postAuthorID).child("notifications").child(notificationID).child("date").setValue(currentDate);

                            if (notificationType.equals("like")) {
                                notification_title[0] = "New like";
                                notification_message[0] = user.getDisplayName() + " liked your post";
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("title").setValue(notification_title[0]);
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("type").setValue("like");
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("date").setValue(currentDate + "  " + currentHour + ":" + currentMinutes);
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("message").setValue(notification_message[0]);
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("post_id").setValue(postID);
                            }
                            if (notificationType.equals("meme_saved")) {
                                notification_title[0] = "Meme saved";
                                notification_message[0] = user.getDisplayName() + " has saved your post";
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("title").setValue("Meme saved");
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("type").setValue("post_save");
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("date").setValue(currentDate + "  " + currentHour + ":" + currentMinutes);
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("message").setValue(user.getDisplayName() + " has saved your post");
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("post_id").setValue(postID);
                            } else if (notificationType.equals("comment_added")) {
                                notification_title[0] = "New comment";
                                notification_message[0] = user.getDisplayName() + ": " + commentText;
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("title").setValue(notification_title[0]);
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("type").setValue("comment_added");
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("date").setValue(currentDate + "  " + currentHour + ":" + currentMinutes);
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("message").setValue(notification_message[0]);
                                usersRef.child(postAuthorID).child("notifications").child(notificationID).child("post_id").setValue(postID);
                            }
                            break;
                        }
                    }
                }

                if (!username.getText().toString().equals(AuthHelper.getCurrentUser().getDisplayName())) {
                    // Find user token from DB
                    // and add notification to Firestore
                    for (DataSnapshot userSnap : snapshot.getChildren()) {
                        if (userSnap.child("name").getValue(String.class) != null) {
                            if (userSnap.child("name").getValue(String.class).equals(username.getText().toString())) {
                                if (userSnap.child("fcm_token").exists()) {
                                    // Add notification to Firestore to send
                                    // push notification from back-end
                                    Map<String, Object> notification = new HashMap<>();
                                    notification.put("token", userSnap.child("fcm_token").getValue(String.class));
                                    notification.put("title", notification_title[0]);
                                    notification.put("message", notification_message[0]);
                                    notification.put("not_type", notificationType);

                                    if (notificationType.equals("follow")) {
                                        notification.put("userID", user.getUid());
                                    } else {
                                        notification.put("postID", postID);
                                    }

                                    notification.put("userID", user.getUid());

                                    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                                    firestore.collection("notifications")
                                            .document(notificationID).set(notification);
                                }
                                break;
                            }
                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
