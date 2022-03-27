package com.george.socialmeme.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.george.socialmeme.Adapters.NotificationRecyclerAdapter;
import com.george.socialmeme.Adapters.PostRecyclerAdapter;
import com.george.socialmeme.Models.NotificationModel;
import com.george.socialmeme.Models.PostModel;
import com.george.socialmeme.R;
import com.github.loadingview.LoadingDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import maes.tech.intentanim.CustomIntent;

public class NotificationsActivity extends AppCompatActivity {

    ProgressBar progressBar;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        CustomIntent.customType(NotificationsActivity.this, "right-to-left");
    }
    boolean isNightModeEnabled() {
        SharedPreferences sharedPref = getSharedPreferences("dark_mode", MODE_PRIVATE);
        return sharedPref.getBoolean("dark_mode", false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (isNightModeEnabled()) {
            setTheme(R.style.AppTheme_Base_Night);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        ImageButton backBtn = findViewById(R.id.imageButton6);
        ImageButton clearNotificationsBtn = findViewById(R.id.clear_notifications_button);
        progressBar = findViewById(R.id.notificationsProgressBar);

        backBtn.setOnClickListener(v -> onBackPressed());

        final RecyclerView recyclerView = findViewById(R.id.notifications_recycler_view);
        final ArrayList<NotificationModel> notificationModelArrayList = new ArrayList<>();
        final LinearLayoutManager layoutManager = new LinearLayoutManager(NotificationsActivity.this);
        final RecyclerView.Adapter recyclerAdapter = new NotificationRecyclerAdapter(notificationModelArrayList, NotificationsActivity.this);

        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());

        progressBar.setVisibility(View.VISIBLE);

        clearNotificationsBtn.setOnClickListener(v ->

                new AlertDialog.Builder(NotificationsActivity.this)
                .setTitle("Clear notifications?")
                .setMessage("Are you sure you want to delete your notifications?")
                .setPositiveButton("Yes", (dialog, which) -> {

                    progressBar.setVisibility(View.GONE);

                    // Delete user notifications
                    userRef.child("notifications").removeValue().addOnCompleteListener(task -> {

                        progressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            onBackPressed();
                            Toast.makeText(NotificationsActivity.this, "Notifications cleared!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(NotificationsActivity.this, "Error: Can't clear notifications", Toast.LENGTH_SHORT).show();
                        }
                    });

                }).setNegativeButton("No", (dialog, which) -> dialog.dismiss()).show());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                progressBar.setVisibility(View.GONE);

                if (snapshot.child("notifications").exists()) {

                    for (DataSnapshot notificationSnap : snapshot.child("notifications").getChildren()) {

                        if (!notificationSnap.child("type").exists()) {
                            continue;
                        }else {
                            NotificationModel notificationModel = new NotificationModel();
                            notificationModel.setTitle(notificationSnap.child("title").getValue(String.class));
                            notificationModel.setMessage(notificationSnap.child("message").getValue(String.class));
                            notificationModel.setType(notificationSnap.child("type").getValue(String.class));
                            notificationModel.setDate(notificationSnap.child("date").getValue(String.class));
                            notificationModelArrayList.add(notificationModel);
                        }

                    }

                    recyclerAdapter.notifyDataSetChanged();

                } else {
                    new AlertDialog.Builder(NotificationsActivity.this)
                            .setTitle("No notifications")
                            .setMessage("No recent notifications available")
                            .setPositiveButton("Ok", (dialog, which) -> onBackPressed()).setCancelable(false)
                            .show();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NotificationsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}