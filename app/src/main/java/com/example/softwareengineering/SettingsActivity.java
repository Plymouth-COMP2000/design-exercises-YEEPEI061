package com.example.softwareengineering;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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

        SharedPreferences userSession = getSharedPreferences("UserSession", MODE_PRIVATE);
        String username = userSession.getString("username", "");
        String role = userSession.getString("role", "");
        String userId = userSession.getString("userId", "");

        usernameText.setText(username);

        Switch switchNew = findViewById(R.id.switchNew);
        Switch switchUpdate = findViewById(R.id.switchUpdate);
        Switch switchCancel = findViewById(R.id.switchCancel);

        String prefName;
        if ("staff".equalsIgnoreCase(role)) {
            prefName = "NotificationPrefs_staff_" + userId; // note staff_
        } else {
            prefName = "NotificationPrefs_" + userId;
        }

        SharedPreferences prefs = getSharedPreferences(prefName, MODE_PRIVATE);

        switchNew.setChecked(prefs.getBoolean("notif_new", true));
        switchUpdate.setChecked(prefs.getBoolean("notif_update", true));
        switchCancel.setChecked(prefs.getBoolean("notif_cancel", true));


        switchNew.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSwitchTimestamp("notif_new", isChecked);
            prefs.edit().putBoolean("notif_new", isChecked).apply();
        });

        switchUpdate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSwitchTimestamp("notif_update", isChecked);
            prefs.edit().putBoolean("notif_update", isChecked).apply();
        });

        switchCancel.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSwitchTimestamp("notif_cancel", isChecked);
            prefs.edit().putBoolean("notif_cancel", isChecked).apply();
        });


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

    private void saveSwitchTimestamp(String key, boolean isEnabled) {
        if (!isEnabled) return; // Only save when turning ON

        SharedPreferences session = getSharedPreferences("UserSession", MODE_PRIVATE);
        String userId = session.getString("userId", "");

        SharedPreferences prefs = getSharedPreferences(
                "NotificationPrefs_staff_" + userId, MODE_PRIVATE
        );

        prefs.edit().putLong(key + "_enabledTime", System.currentTimeMillis()).apply();
    }


}
