package com.george.socialmeme.Services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.george.socialmeme.Activities.HomeActivity;
import com.george.socialmeme.BuildConfig;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UpdateService extends Service {

    private DatabaseReference mDatabaseReference;
    private Handler handler;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        final boolean[] updateFound = {false};

        // Create a handler
        handler = new Handler();

        // Schedule a runnable to run every 10 seconds
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (!updateFound[0]) {
                    mDatabaseReference.child("latest_version_code").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String value = dataSnapshot.getValue(String.class);

                            int appVersionCode = BuildConfig.VERSION_CODE;
                            int latestVersionOnDb = Integer.parseInt(value);

                            if (appVersionCode < latestVersionOnDb) {
                                HomeActivity.showUpdateDialog();
                                updateFound[0] = true;
                                stopSelf(); // sometimes not stopping so we're using updateFound
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    // Reschedule the runnable to run again in 20 seconds
                    handler.postDelayed(this, 20000);
                }
            }
        }, 10000);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("UPDATE SERVICE: ", "UPDATE SERVICE KILLED");
        //Toast.makeText(this, "update service killed", Toast.LENGTH_SHORT).show();
        //handler.removeCallbacksAndMessages(null);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
