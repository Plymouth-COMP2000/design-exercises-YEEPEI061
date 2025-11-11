package com.example.softwareengineering;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class StaffReservationActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_staff_reservation);

        LayoutBottomNav.setupBottomNav(this, findViewById(android.R.id.content));
        LayoutBottomNav.highlightSelected(this, findViewById(android.R.id.content), R.id.reservationsSection);

        ImageButton cancelButton = findViewById(R.id.cancelButton);

        cancelButton.setOnClickListener(v -> {
            PopupHelper.showPopup(
                   this,
                    R.drawable.ic_cancel_circle,
                    getResources().getColor(R.color.my_danger, null),
                    "Cancel Reservation",
                    "Are you sure you want to cancel this reservation?",
                    this::cancelReservation
            );
        });
    }

    private void cancelReservation() {
        // your actual delete logic here
        Toast.makeText(this, "Reservation cancelled successfully!", Toast.LENGTH_SHORT).show();
    }
}
