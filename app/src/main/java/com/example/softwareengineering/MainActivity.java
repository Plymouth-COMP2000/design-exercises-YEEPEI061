package com.example.softwareengineering;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "reservation_channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }, 3000); // 3 seconds delay
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);

            if (manager != null) {

                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "Reservation Notifications",
                        NotificationManager.IMPORTANCE_HIGH
                );

                channel.enableLights(true);
                channel.setLightColor(Color.BLUE);
                channel.enableVibration(true);
                channel.setDescription("Reservation alerts with popup notifications");

                manager.createNotificationChannel(channel);

                Log.d("NotifDebug", "Notification channel created/recreated");
            }
        }
    }
}