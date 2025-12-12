package com.example.softwareengineering;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GuestReservationActivity extends AppCompatActivity {

    private ReservationDatabaseHelper dbHelper;
    private GuestReservationAdapter adapter;
    private List<ReservationModel> reservations;
    private static final String CHANNEL_ID = "reservation_channel";
    private static final int REQ_POST_NOTIF = 101;
    private NotificationModel pendingNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest_reservation);

        LayoutBottomNav.setupBottomNav(this, findViewById(android.R.id.content));
        LayoutBottomNav.highlightSelected(this, findViewById(android.R.id.content), R.id.reservationsSection);

        dbHelper = new ReservationDatabaseHelper(this);

        RecyclerView recyclerView = findViewById(R.id.recyclerReservations);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        refreshList();

        adapter = new GuestReservationAdapter(this, reservations, new GuestReservationAdapter.OnReservationClickListener() {
            @Override
            public void onCancelClick(ReservationModel reservation) {
                PopupHelper.showPopup(
                        GuestReservationActivity.this,
                        R.drawable.ic_cancel_circle,
                        getResources().getColor(R.color.my_danger, null),
                        "Cancel Reservation",
                        "Are you sure you want to cancel this reservation?",
                        () -> {
                            dbHelper.deleteReservation(reservation.getId());
                            refreshList();

                            long currentTime = System.currentTimeMillis();

                            if (isNotificationAllowed()) {
                                NotificationModel notif = new NotificationModel(
                                        "Reservation Cancelled",
                                        "You have cancelled your reservation for " +
                                                reservation.getDate() + " at " + reservation.getTime() + ".",
                                        currentTime,
                                        true,
                                        R.drawable.ic_cancel_circle,
                                        getResources().getColor(R.color.my_danger, null),
                                        getResources().getColor(R.color.soft_red, null)
                                );
                                addNotification(notif);
                                checkPermissionAndNotify(notif);
                            }

                            SharedPreferences userSession = getSharedPreferences("UserSession", MODE_PRIVATE);
                            String username = userSession.getString("username", "Guest");

                            String staffMessage = username + " cancelled their reservation for " +
                                    reservation.getDate() + " at " + reservation.getTime() + ".";
                            NotificationModel staffNotif = new NotificationModel(
                                    "Reservation Cancelled",
                                    staffMessage,
                                    currentTime,
                                    true,
                                    R.drawable.ic_cancel_circle,
                                    getResources().getColor(R.color.my_danger, null),
                                    getResources().getColor(R.color.soft_red, null)
                            );
                            addNotificationForStaff(staffNotif);

                            Toast.makeText(GuestReservationActivity.this, "Reservation cancelled successfully!", Toast.LENGTH_SHORT).show();
                        }
                );
            }

            @Override
            public void onEditClick(ReservationModel reservation) {
                Intent intent = new Intent(GuestReservationActivity.this, GuestReservationFormActivity.class);
                intent.putExtra("mode", "edit");
                intent.putExtra("reservationId", reservation.getId());
                intent.putExtra("date", reservation.getDate());
                intent.putExtra("time", reservation.getTime());
                intent.putExtra("guestCount", reservation.getGuestCount());
                intent.putExtra("table", reservation.getTable());
                intent.putExtra("status", reservation.getStatus());
                intent.putExtra("specialRequest", reservation.getSpecialRequest());
                startActivity(intent);
            }

            @Override
            public void onBookAgainClick(ReservationModel reservation) {
                Intent intent = new Intent(GuestReservationActivity.this, GuestReservationFormActivity.class);
                intent.putExtra("mode", "bookAgain");
                intent.putExtra("date", reservation.getDate());
                intent.putExtra("time", reservation.getTime());
                intent.putExtra("guestCount", reservation.getGuestCount());
                intent.putExtra("table", reservation.getTable());
                intent.putExtra("specialRequest", reservation.getSpecialRequest());
                startActivity(intent);
            }
        });

        recyclerView.setAdapter(adapter);

        FloatingActionButton bottomButton = findViewById(R.id.bottomButton);
        bottomButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, GuestReservationFormActivity.class);
            intent.putExtra("mode", "add");
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }

    private String getLoggedInUsername() {
        return getSharedPreferences("UserSession", MODE_PRIVATE)
                .getString("username", "");
    }

    private void refreshList() {
        String username = getLoggedInUsername();
        List<ReservationModel> allReservations = dbHelper.getAllReservations();

        reservations = filterReservationsByUser(allReservations, username);

        if (adapter != null) {
            adapter.updateReservations(reservations);
        } else {
            adapter = new GuestReservationAdapter(this, reservations, new GuestReservationAdapter.OnReservationClickListener() {
                @Override
                public void onCancelClick(ReservationModel reservation) { }
                @Override
                public void onEditClick(ReservationModel reservation) { }
                @Override
                public void onBookAgainClick(ReservationModel reservation) { }
            });
        }

        sortReservations();
    }

    private List<ReservationModel> filterReservationsByUser(List<ReservationModel> list, String username) {
        List<ReservationModel> filtered = new ArrayList<>();
        for (ReservationModel r : list) {
            if (username.equals(r.getCustomerName())) {
                filtered.add(r);
            }
        }
        return filtered;
    }

    private void sortReservations() {
        Collections.sort(reservations, (r1, r2) -> {
            if (r1.getStatus().equals(r2.getStatus())) return 0;
            return r1.getStatus().equals("Upcoming") ? -1 : 1;
        });
    }

    private void addNotification(NotificationModel notification) {
        SharedPreferences userSession = getSharedPreferences("UserSession", MODE_PRIVATE);
        String userId = userSession.getString("userId", "");

        SharedPreferences sp = getSharedPreferences("Notifications_" + userId, MODE_PRIVATE); // separate by user
        String json = sp.getString("list", "[]");

        Type type = new TypeToken<List<NotificationModel>>() {}.getType();
        List<NotificationModel> notifications = new Gson().fromJson(json, type);

        notifications.add(0, notification);
        sp.edit().putString("list", new Gson().toJson(notifications)).apply();
    }

    private void checkPermissionAndNotify(NotificationModel notif) {
        Log.d("NotifDebug", "Permission granted, showing notification");

        pendingNotification = notif;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
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

    private void showNotification(NotificationModel notif) {
        Log.d("NotifDebug", "showNotification called with title: " + notif.getTitle());
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

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_POST_NOTIF) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showNotification(pendingNotification);
                pendingNotification = null;
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addNotificationForStaff(NotificationModel notif) {
        SharedPreferences sp = getSharedPreferences("Notifications_staff", MODE_PRIVATE);
        String json = sp.getString("list", "[]");

        Type type = new TypeToken<List<NotificationModel>>() {}.getType();
        List<NotificationModel> staffList = new Gson().fromJson(json, type);

        staffList.add(0, notif); // add at top
        sp.edit().putString("list", new Gson().toJson(staffList)).apply();
    }

    private boolean isNotificationAllowed() {
        SharedPreferences userSession = getSharedPreferences("UserSession", MODE_PRIVATE);
        String userId = userSession.getString("userId", "");

        SharedPreferences prefs = getSharedPreferences("NotificationPrefs_" + userId, MODE_PRIVATE);

        return prefs.getBoolean("notif_cancel", true);
    }



}
