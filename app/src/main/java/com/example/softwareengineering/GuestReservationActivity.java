package com.example.softwareengineering;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GuestReservationActivity extends AppCompatActivity {

    private ReservationDatabaseHelper dbHelper;
    private GuestReservationAdapter adapter;
    private List<ReservationModel> reservations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest_reservation);

        // Setup bottom navigation (if used)
        LayoutBottomNav.setupBottomNav(this, findViewById(android.R.id.content));
        LayoutBottomNav.highlightSelected(this, findViewById(android.R.id.content), R.id.reservationsSection);

        dbHelper = new ReservationDatabaseHelper(this);

        RecyclerView recyclerView = findViewById(R.id.recyclerReservations);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load reservations
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

    // Get the logged-in username from SharedPreferences
    private String getLoggedInUsername() {
        return getSharedPreferences("UserSession", MODE_PRIVATE)
                .getString("username", "");
    }

    // Refresh the reservation list for this user
    private void refreshList() {
        String username = getLoggedInUsername();
        List<ReservationModel> allReservations = dbHelper.getAllReservations();

        // Filter reservations belonging to the logged-in user
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

    // Filter reservations by customerName
    private List<ReservationModel> filterReservationsByUser(List<ReservationModel> list, String username) {
        List<ReservationModel> filtered = new ArrayList<>();
        for (ReservationModel r : list) {
            if (username.equals(r.getCustomerName())) {
                filtered.add(r);
            }
        }
        return filtered;
    }

    // Sort Upcoming first
    private void sortReservations() {
        Collections.sort(reservations, (r1, r2) -> {
            if (r1.getStatus().equals(r2.getStatus())) return 0;
            return r1.getStatus().equals("Upcoming") ? -1 : 1;
        });
    }
}
