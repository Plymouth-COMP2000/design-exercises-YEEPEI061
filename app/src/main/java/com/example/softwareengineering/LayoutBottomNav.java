package com.example.softwareengineering;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

public class LayoutBottomNav {

    public static void setupBottomNav(Context context, View parentView) {
        LinearLayout bottomNav = parentView.findViewById(R.id.bottomNav);

        SharedPreferences prefs = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String role = prefs.getString("role", "");

        bottomNav.findViewById(R.id.menuSection).setOnClickListener(v -> {
            highlightSelected(context, parentView, R.id.menuSection);
            Intent intent = role.equals("staff")
                    ? new Intent(context, StaffMenuActivity.class)
                    : new Intent(context, GuestMenuActivity.class);
            context.startActivity(intent);
        });

        bottomNav.findViewById(R.id.reservationsSection).setOnClickListener(v -> {
            highlightSelected(context, parentView, R.id.reservationsSection);
            Intent intent = role.equals("staff")
                    ? new Intent(context, StaffReservationActivity.class)
                    : new Intent(context, GuestReservationActivity.class);
            context.startActivity(intent);
        });

        bottomNav.findViewById(R.id.notificationsSection).setOnClickListener(v -> {
            highlightSelected(context, parentView, R.id.notificationsSection);
            Intent intent = new Intent(context, NotificationActivity.class);
            context.startActivity(intent);
        });

        bottomNav.findViewById(R.id.settingsSection).setOnClickListener(v -> {
            highlightSelected(context, parentView, R.id.settingsSection);
            Intent intent = new Intent(context, SettingsActivity.class);
            context.startActivity(intent);
        });
    }

    public static void highlightSelected(Context context, View parentView, int selectedId) {
        int[][] ids = {
                {R.id.menuSection, R.id.menuIcon, R.id.menuLabel, R.drawable.ic_menu_book_outline, R.drawable.ic_menu_book_filled},
                {R.id.reservationsSection, R.id.reservationsIcon, R.id.reservationsLabel, R.drawable.ic_reservation_outline, R.drawable.ic_reservation_filled},
                {R.id.notificationsSection, R.id.notificationsIcon, R.id.notificationsLabel, R.drawable.ic_notification_outline, R.drawable.ic_notification_filled},
                {R.id.settingsSection, R.id.settingsIcon, R.id.settingsLabel, R.drawable.ic_setting_outline, R.drawable.ic_setting_filled}
        };

        int gray = ContextCompat.getColor(context, R.color.gray);
        int primary = ContextCompat.getColor(context, R.color.my_primary);

        for (int[] item : ids) {
            ImageView icon = parentView.findViewById(item[1]);
            TextView label = parentView.findViewById(item[2]);

            if (selectedId == item[0]) {
                icon.setImageResource(item[4]);
                icon.setColorFilter(primary);
                label.setTextColor(primary);
            } else {
                icon.setImageResource(item[3]);
                icon.setColorFilter(gray);
                label.setTextColor(gray);
            }
        }
    }

}
