package com.example.softwareengineering;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class StaffMenuActivity extends AppCompatActivity {

    LinearLayout foodTab, drinkTab;
    TextView foodText, drinkText;
    View foodUnderline, drinkUnderline;
    private String currentCategory = "food";
    private static final String CHANNEL_ID = "reservation_channel";
    private static final int REQ_POST_NOTIF = 101;
    private NotificationModel pendingNotification = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_menu);

        showPendingStaffNotifications();

        LayoutBottomNav.setupBottomNav(this, findViewById(android.R.id.content));
        LayoutBottomNav.highlightSelected(this, findViewById(android.R.id.content), R.id.menuSection);

        // Find Views
        foodTab = findViewById(R.id.foodTab);
        drinkTab = findViewById(R.id.drinkTab);
        foodText = findViewById(R.id.foodMenuTab);
        drinkText = findViewById(R.id.drinksMenuTab);
        foodUnderline = findViewById(R.id.foodUnderline);
        drinkUnderline = findViewById(R.id.drinkUnderline);

        loadFragment(StaffMenuListFragment.newInstance("food"));
        setActiveTab(true);

        foodTab.setOnClickListener(v -> {
            currentCategory = "food";
            loadFragment(StaffMenuListFragment.newInstance("food"));
            setActiveTab(true);
        });

        drinkTab.setOnClickListener(v -> {
            currentCategory = "drink";
            loadFragment(StaffMenuListFragment.newInstance("drink"));
            setActiveTab(false);
        });

        FloatingActionButton bottomButton = findViewById(R.id.bottomButton);

        bottomButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, StaffMenuItemFormActivity.class);
            intent.putExtra("mode", "add");
            intent.putExtra("category", currentCategory);
            startActivity(intent);
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }

    private void setActiveTab(boolean isFood) {
        if (isFood) {
            foodText.setTextColor(getColor(R.color.my_primary));
            foodText.setTypeface(null, android.graphics.Typeface.BOLD);
            drinkText.setTextColor(getColor(R.color.gray));
            drinkText.setTypeface(null, android.graphics.Typeface.NORMAL);

            foodUnderline.setVisibility(View.VISIBLE);
            drinkUnderline.setVisibility(View.GONE);
        } else {
            drinkText.setTextColor(getColor(R.color.my_primary));
            drinkText.setTypeface(null, android.graphics.Typeface.BOLD);
            foodText.setTextColor(getColor(R.color.gray));
            foodText.setTypeface(null, android.graphics.Typeface.NORMAL);

            drinkUnderline.setVisibility(View.VISIBLE);
            foodUnderline.setVisibility(View.GONE);
        }
    }

    private void showPendingStaffNotifications() {
        SharedPreferences sp = getSharedPreferences("Notifications_staff", MODE_PRIVATE);
        String json = sp.getString("list", "[]");

        Type type = new TypeToken<List<NotificationModel>>() {}.getType();
        List<NotificationModel> staffNotifications = new Gson().fromJson(json, type);

        if (staffNotifications == null || staffNotifications.isEmpty()) return;

        boolean updated = false;

        for (NotificationModel notif : staffNotifications) {
            if (notif.isUnread()) {
                checkPermissionAndNotify(notif);
                updated = true;
            }
        }

        if (updated) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("list", new Gson().toJson(staffNotifications));
            editor.apply();
        }
    }


    private void checkPermissionAndNotify(NotificationModel notif) {
        pendingNotification = notif;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQ_POST_NOTIF
                );

            } else {
                showNotification(notif);
            }
        } else {
            showNotification(notif);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_POST_NOTIF) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (pendingNotification != null) {
                    showNotification(pendingNotification);
                    pendingNotification = null;
                }
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void showNotification(NotificationModel notif) {
        Intent intent = new Intent(this, NotificationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ?
                        PendingIntent.FLAG_MUTABLE :
                        PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle(notif.getTitle())
                        .setContentText(notif.getMessage())
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_ALL);


        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }


}
