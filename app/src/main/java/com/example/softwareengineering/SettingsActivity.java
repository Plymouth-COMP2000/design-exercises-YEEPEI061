package com.example.softwareengineering;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        LayoutBottomNav.setupBottomNav(this, findViewById(android.R.id.content));
        LayoutBottomNav.highlightSelected(this, findViewById(android.R.id.content), R.id.settingsSection);

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

    private void logoutUser() {

        SharedPreferences prefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        finish();
    }

}
