package com.example.softwareengineering;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class GuestReservationActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest_reservation);

        LayoutBottomNav.setupBottomNav(this, findViewById(android.R.id.content));
        LayoutBottomNav.highlightSelected(this, findViewById(android.R.id.content), R.id.reservationsSection);

        Button bottomButton = findViewById(R.id.bottomButton);
        bottomButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, GuestReservationFormActivity.class);
            intent.putExtra("mode", "add");
            startActivity(intent);
        });


        Button editButton = findViewById(R.id.editButton);
        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, GuestReservationFormActivity.class);
            intent.putExtra("mode", "edit");
            startActivity(intent);
        });

        Button cancelReservationButton = findViewById(R.id.cancelReservationButton);
        cancelReservationButton.setOnClickListener(v -> {
            PopupHelper.showPopup(
                    this,
                    R.drawable.ic_cancel_circle,
                    getResources().getColor(R.color.my_danger, null),
                    "Cancel Reservation",
                    "Are you sure you want to cancel this reservation?",
                    () -> {
                        Toast.makeText(this, "Reservation cancelled successfully!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, GuestReservationActivity.class);
                        startActivity(intent);
                    }
            );
        });

    }
}
