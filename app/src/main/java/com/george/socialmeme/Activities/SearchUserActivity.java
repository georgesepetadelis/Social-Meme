package com.george.socialmeme.Activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.developer.kalert.KAlertDialog;
import com.george.socialmeme.Adapters.UserRecyclerAdapter;
import com.george.socialmeme.Models.UserModel;
import com.george.socialmeme.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import maes.tech.intentanim.CustomIntent;

public class SearchUserActivity extends AppCompatActivity {

    KAlertDialog progressDialog;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        CustomIntent.customType(SearchUserActivity.this, "right-to-left");
    }

    void searchUser(UserRecyclerAdapter adapter, String query, List<UserModel> allUsers) {

        List<UserModel> filteredList = new ArrayList<>();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        for (UserModel user : allUsers) {
            if (user != null && user.getUsername() != null && !user.getUserID().equals(firebaseUser.getUid())) {
                if (user.getUsername().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(user);
                }
            }
        }

        adapter.setFilteredList(filteredList);

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
        setContentView(R.layout.activity_search_user);

        progressDialog = new KAlertDialog(SearchUserActivity.this, KAlertDialog.PROGRESS_TYPE);
        progressDialog.getProgressHelper().setBarColor(R.color.main);
        progressDialog.setTitleText("Loading memers...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        EditText searchView = findViewById(R.id.editTextTextPersonName);
        RecyclerView recyclerView = findViewById(R.id.userSearch);
        ImageView backBtn = findViewById(R.id.imageView25);

        List<UserModel> allUsers = new ArrayList<>();

        UserRecyclerAdapter adapter = new UserRecyclerAdapter(null, SearchUserActivity.this, allUsers);
        LinearLayoutManager layoutManager = new LinearLayoutManager(SearchUserActivity.this);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        backBtn.setOnClickListener(v -> onBackPressed());

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                progressDialog.hide();

                for (DataSnapshot snap : snapshot.getChildren()) {

                    if (snap.child("id").getValue(String.class) != null) {
                        int followersCount = (int) snapshot.child(snap.child("id").getValue(String.class)).child("followers").getChildrenCount();
                        UserModel userModel = new UserModel();
                        userModel.setUserID(snap.child("id").getValue(String.class));

                        switch (followersCount) {
                            case 0:
                                userModel.setFollowers("No followers");
                                break;
                            case 1:
                                userModel.setFollowers("1 follower");
                                break;
                            default:
                                userModel.setFollowers(followersCount + " followers");
                                break;
                        }

                        for (DataSnapshot usersSnapshot : snapshot.getChildren()) {

                            if (Objects.equals(usersSnapshot.child("id").getValue(String.class), snap.child("id").getValue(String.class))) {
                                userModel.setUsername(snapshot.child(usersSnapshot.child("id").getValue(String.class)).child("name").getValue(String.class));

                                if (snapshot.child(usersSnapshot.child("id").getValue(String.class)).child("profileImgUrl").exists()) {
                                    userModel.setProfilePictureURL(snapshot.child(usersSnapshot.child("id").getValue(String.class)).child("profileImgUrl").getValue(String.class));
                                } else if (snapshot.child(usersSnapshot.child("id").getValue(String.class)).child("profileImgUrl").getValue(String.class) == null) {
                                    userModel.setProfilePictureURL("none");
                                }

                                allUsers.add(userModel);
                                adapter.notifyDataSetChanged();

                            }

                        }
                    }

                    }
                }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.hide();
                onBackPressed();
                Toast.makeText(SearchUserActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty())  searchUser(adapter, s.toString(), allUsers);
            }
        });

    }
}