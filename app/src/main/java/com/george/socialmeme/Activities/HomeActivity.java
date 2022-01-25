package com.george.socialmeme.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.george.socialmeme.R;
import com.george.socialmeme.Fragments.HomeFragment;
import com.george.socialmeme.Fragments.NewPostFragment;
import com.george.socialmeme.Fragments.MyProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity {

    public static boolean anonymous = false;

    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("posts");
    DatabaseReference uref = FirebaseDatabase.getInstance().getReference("users");


    private final BottomNavigationView.OnNavigationItemSelectedListener navListener = menuItem -> {
        Fragment selectedFragment = null;

        switch (menuItem.getItemId()) {
            case R.id.home_fragment:
                selectedFragment = new HomeFragment();
                break;
            case R.id.new_post_fragment:
                selectedFragment = new NewPostFragment();
                break;
            case R.id.my_profile_fragment:
                selectedFragment = new MyProfileFragment();
                break;
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
        return true;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        final FirebaseAuth mAuth = FirebaseAuth.getInstance();

        final BottomNavigationView bottomNavBar = findViewById(R.id.bottom_nav);

        bottomNavBar.setOnNavigationItemSelectedListener(navListener);

        /*
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    String name = snap.child("name").getValue(String.class);
                    String postId = snap.child("id").getValue(String.class);
                    uref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot2) {
                            for (DataSnapshot snapshot1 : snapshot2.getChildren()) {
                                if (snapshot1.child("name").getValue(String.class).equals(name)) {
                                    ref.child(postId).child("authorProfilePictureURL").setValue(snapshot1.child("profileImgUrl").getValue().toString());
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/


        if (anonymous) {
            /*
            MenuItem postItem = bottomNavBar.getMenu().findItem(R.id.new_post_fragment);
            MenuItem myProfileItem = bottomNavBar.getMenu().findItem(R.id.my_profile_fragment);

            postItem.setVisible(false);
            myProfileItem.setVisible(false);*/

            bottomNavBar.setVisibility(View.GONE);

        }

        // Disable dark mode
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Load default fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();

        if (!anonymous) {
            final FirebaseUser user = mAuth.getCurrentUser();
            user.reload().addOnFailureListener(e -> new AlertDialog.Builder(HomeActivity.this)
                    .setTitle("Error")
                    .setMessage(e.getMessage())
                    .setCancelable(false)
                    .setPositiveButton("Ok", (dialog, which) -> {
                        finish();
                        startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                    }).show());
        }


    }
}