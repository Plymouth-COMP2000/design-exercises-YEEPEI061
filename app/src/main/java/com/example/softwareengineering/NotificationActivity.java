package com.example.softwareengineering;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    NotificationAdapter adapter;
    List<NotificationModel> notificationList = new ArrayList<>();
    private boolean showUnreadOnly = false;


    TextView markAllRead;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        LayoutBottomNav.setupBottomNav(this, findViewById(android.R.id.content));
        LayoutBottomNav.highlightSelected(this, findViewById(android.R.id.content), R.id.notificationsSection);

        ImageButton settingsButton = findViewById(R.id.settingsButton);

        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(NotificationActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        recyclerView = findViewById(R.id.notificationRecycler);
        MaterialButton allButton = findViewById(R.id.allButton);
        MaterialButton unreadButton = findViewById(R.id.unreadButton);
        markAllRead = findViewById(R.id.markAllRead);
        setButtonSelected(allButton, true);
        setButtonSelected(unreadButton, false);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadNotifications();

        for (NotificationModel n : notificationList) {
            n.setDisplayed(true);
        }


        adapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(adapter);

        refreshNotifications(false);

        allButton.setOnClickListener(v -> {
            showUnreadOnly = false;
            refreshNotifications(false);
            setButtonSelected(allButton, true);
            setButtonSelected(unreadButton, false);
        });

        unreadButton.setOnClickListener(v -> {
            showUnreadOnly = true;
            refreshNotifications(true);
            setButtonSelected(allButton, false);
            setButtonSelected(unreadButton, true);
        });

        markAllRead.setOnClickListener(v -> {
            for (NotificationModel n : notificationList) n.setUnread(false);
            saveNotifications();
            refreshNotifications(showUnreadOnly);
        });

    }

    private void loadNotifications() {
        SharedPreferences userSession = getSharedPreferences("UserSession", MODE_PRIVATE);
        String userId = userSession.getString("userId", "");
        String role = userSession.getString("role", "guest");

        UserSignupDbHelper dbHelper = new UserSignupDbHelper(this);
        long signupTime = dbHelper.getSignupTime(userId);

        String prefName = "guest".equalsIgnoreCase(role) ? "Notifications_" + userId : "Notifications_staff";
        SharedPreferences sp = getSharedPreferences(prefName, MODE_PRIVATE);
        String json = sp.getString("list", "[]");

        Type type = new TypeToken<List<NotificationModel>>(){}.getType();
        List<NotificationModel> allNotifications = new Gson().fromJson(json, type);

        SharedPreferences prefs = getSharedPreferences(
                "NotificationPrefs_staff_" + userId, MODE_PRIVATE
        );

        notificationList = new ArrayList<>();
        if (allNotifications != null) {
            for (NotificationModel n : allNotifications) {

                if (n.getTimestamp() < signupTime) continue;

                if ("staff".equalsIgnoreCase(role)) {
                    if (n.isNewReservation()) {
                        long enabledTime = prefs.getLong("notif_new_enabledTime", 0);
                        if (!isStaffNotificationAllowed("notif_new") || n.getTimestamp() < enabledTime) continue;
                    }
                    if (n.isUpdateReservation()) {
                        long enabledTime = prefs.getLong("notif_update_enabledTime", 0);
                        if (!isStaffNotificationAllowed("notif_update") || n.getTimestamp() < enabledTime) continue;
                    }
                    if (n.isCancelReservation()) {
                        long enabledTime = prefs.getLong("notif_cancel_enabledTime", 0);
                        if (!isStaffNotificationAllowed("notif_cancel") || n.getTimestamp() < enabledTime) continue;
                    }
                } else {
                    if (n.isCancelReservation()) {
                        SharedPreferences guestPrefs = getSharedPreferences("NotificationPrefs_" + userId, MODE_PRIVATE);
                        long enabledTime = guestPrefs.getLong("notif_cancel_enabledTime", 0);
                        if (!isGuestNotificationAllowed() || n.getTimestamp() < enabledTime) continue;
                    }
                }

                notificationList.add(n);
            }
        }
    }


    void saveNotifications() {
        SharedPreferences userSession = getSharedPreferences("UserSession", MODE_PRIVATE);
        String userId = userSession.getString("userId", "");
        String role = userSession.getString("role", "guest");

        String prefName;
        if ("staff".equalsIgnoreCase(role)) {
            prefName = "Notifications_staff";
        } else {
            prefName = "Notifications_" + userId;
        }

        SharedPreferences sp = getSharedPreferences(prefName, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        String json = new Gson().toJson(notificationList);
        editor.putString("list", json);
        editor.apply();
    }

    @SuppressLint("SetTextI18n")
    private void refreshNotifications(boolean showUnreadOnly) {
        List<NotificationModel> notifList = new ArrayList<>();
        for (NotificationModel n : notificationList) {
            if (!showUnreadOnly || n.isUnread()) {
                notifList.add(n);
            }
        }

        LinearLayout emptyState = findViewById(R.id.emptyStateLayout);
        LinearLayout filterRow = findViewById(R.id.filterRow);
        RecyclerView recycler = findViewById(R.id.notificationRecycler);

        if (notificationList.isEmpty()) {
            filterRow.setVisibility(View.GONE);
            recycler.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);

            TextView title = emptyState.findViewById(R.id.emptyStateTitle);
            TextView message = emptyState.findViewById(R.id.emptyStateMessage);
            ImageView icon = emptyState.findViewById(R.id.emptyStateIcon);

            title.setText("All caught up!");
            message.setText("You have no new notifications.");
            icon.setImageResource(R.drawable.ic_notification_outline);

        } else if (notifList.isEmpty()) {
            filterRow.setVisibility(View.VISIBLE);
            recycler.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);

            TextView title = emptyState.findViewById(R.id.emptyStateTitle);
            TextView message = emptyState.findViewById(R.id.emptyStateMessage);
            ImageView icon = emptyState.findViewById(R.id.emptyStateIcon);

            title.setText("No unread messages");
            message.setText("You’re all caught up.");
            icon.setImageResource(R.drawable.ic_notification_outline);

        } else {
            filterRow.setVisibility(View.VISIBLE);
            recycler.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);

            adapter.updateList(notifList);
        }
    }

    private void setButtonSelected(MaterialButton button, boolean isSelected) {
        if (isSelected) {
            button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.my_tertiary));
            button.setTextColor(ContextCompat.getColor(this, R.color.my_primary));
        } else {
            button.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.transparent));
            button.setTextColor(ContextCompat.getColor(this, R.color.my_secondary));
            button.setStrokeColor(ContextCompat.getColorStateList(this, R.color.my_secondary));
            button.setStrokeWidth(2);
        }
    }

    private boolean isStaffNotificationAllowed(String key) {
        SharedPreferences session = getSharedPreferences("UserSession", MODE_PRIVATE);
        String staffId = session.getString("userId", "");

        SharedPreferences prefs = getSharedPreferences(
                "NotificationPrefs_staff_" + staffId, MODE_PRIVATE
        );

        return prefs.getBoolean(key, true);
    }

    private boolean isGuestNotificationAllowed() {
        SharedPreferences userSession = getSharedPreferences("UserSession", MODE_PRIVATE);
        String userId = userSession.getString("userId", "");

        SharedPreferences prefs = getSharedPreferences("NotificationPrefs_" + userId, MODE_PRIVATE);
        return prefs.getBoolean("notif_cancel", true);
    }


}
