package com.example.softwareengineering;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    ImageView profileImage;

    private String USER_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        profileImage = findViewById(R.id.profileImage);

        LayoutBottomNav.setupBottomNav(this, findViewById(android.R.id.content));
        LayoutBottomNav.highlightSelected(this, findViewById(android.R.id.content), R.id.settingsSection);

        TextView usernameText = findViewById(R.id.username);
        TextView prefOne = findViewById(R.id.prefOne);
        TextView prefTwo = findViewById(R.id.prefTwo);
        TextView prefThree = findViewById(R.id.prefThree);

        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        String username = prefs.getString("username", "");
        String role = prefs.getString("role", "");

        usernameText.setText(username);

        if ("staff".equals(role)) {
            prefOne.setText("New Reservations");
            prefTwo.setText("Reservation Changes");
            prefThree.setText("Cancellations");
        }

        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v ->
                PopupHelper.showPopup(
                        this,
                        R.drawable.ic_warning,
                        ContextCompat.getColor(this, R.color.my_danger),
                        "Log Out",
                        "Are you sure you want to logout?",
                        this::logoutUser
                )
        );

        LinearLayout profileSection = findViewById(R.id.profileSection);
        profileSection.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        String updatedUsername = prefs.getString("username", "");

        TextView usernameText = findViewById(R.id.username);
        usernameText.setText(updatedUsername);

        SharedPreferences sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE);
        SharedPreferences profilePrefs = getSharedPreferences("ProfilePrefs", MODE_PRIVATE);
        USER_ID = sharedPref.getString("userId", "");

        String savedPath = profilePrefs.getString("profileImagePath_" + USER_ID, null);
        if (savedPath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(savedPath);
            if (bitmap != null) profileImage.setImageBitmap(bitmap);
        } else {
            String gender = profilePrefs.getString("profile", "boy");
            profileImage.setImageResource(
                    "boy".equals(gender) ? R.drawable.sample_profile_boy : R.drawable.sample_profile
            );
        }
    }



    private void logoutUser() {

        SharedPreferences prefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        finish();
    }

}
