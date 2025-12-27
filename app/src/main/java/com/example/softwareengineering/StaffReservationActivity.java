package com.example.softwareengineering;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Objects;

public class StaffReservationActivity extends AppCompatActivity {

    private RecyclerView staffRecycler;
    private TextView noReservationText;
    private ImageButton filterButton;
    private StaffReservationAdapter adapter;
    private ReservationDatabaseHelper dbHelper;

    private Integer selectedDay = null, selectedMonth = null, selectedYear = null;
    private Integer selectedGuests = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_staff_reservation);

        LayoutBottomNav.setupBottomNav(this, findViewById(android.R.id.content));
        LayoutBottomNav.highlightSelected(this, findViewById(android.R.id.content), R.id.reservationsSection);

        dbHelper = new ReservationDatabaseHelper(this);

        staffRecycler = findViewById(R.id.staffReservationRecycler);
        noReservationText = findViewById(R.id.noReservationText);
        filterButton = findViewById(R.id.filterButton);

        staffRecycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new StaffReservationAdapter(this, new ArrayList<>(), reservation -> PopupHelper.showPopup(
                StaffReservationActivity.this,
                R.drawable.ic_cancel_circle,
                getResources().getColor(R.color.my_danger, null),
                "Cancel Reservation",
                "Are you sure you want to cancel this reservation?",
                () -> cancelReservation(reservation)
        ));

        staffRecycler.setAdapter(adapter);

        setupFilterDialog();
        loadReservations();
    }

    private void setupFilterDialog() {
        filterButton.setOnClickListener(v -> {
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            int padding = (int) (16 * getResources().getDisplayMetrics().density);
            layout.setPadding(padding, padding, padding, padding);

            Button datePickerBtn = new Button(this);
            datePickerBtn.setText(selectedDay != null ? formatDate(selectedDay, selectedMonth, selectedYear) : "Select Date");
            datePickerBtn.setAllCaps(false);
            datePickerBtn.setBackgroundColor(getResources().getColor(R.color.my_medium, null));
            datePickerBtn.setTextColor(getResources().getColor(android.R.color.white, null));
            layout.addView(datePickerBtn);

            Button guestsBtn = new Button(this);
            guestsBtn.setText(selectedGuests != null ? selectedGuests + " guests" : "Select Guests");
            guestsBtn.setAllCaps(false);
            guestsBtn.setBackgroundColor(getResources().getColor(R.color.my_secondary, null));
            guestsBtn.setTextColor(getResources().getColor(android.R.color.white, null));
            layout.addView(guestsBtn);

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Filter Reservations")
                    .setView(layout)
                    .setPositiveButton("Apply", (d, which) -> applyFilters())
                    .setNegativeButton("Clear", (d, which) -> {
                        selectedDay = selectedMonth = selectedYear = selectedGuests = null;
                        loadReservations();
                    })
                    .create();

            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.card_bg);

            datePickerBtn.setOnClickListener(v1 -> {
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        StaffReservationActivity.this,
                        (view, year, month, dayOfMonth) -> {
                            selectedDay = dayOfMonth;
                            selectedMonth = month;
                            selectedYear = year;
                            datePickerBtn.setText(formatDate(dayOfMonth, month, year));
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                );
                datePickerDialog.show();
            });

            guestsBtn.setOnClickListener(v1 -> {
                String[] guestOptions = {"1 guest", "2 guests", "3 guests", "4 guests", "5+ guests"};
                new AlertDialog.Builder(StaffReservationActivity.this)
                        .setTitle("Select number of guests")
                        .setItems(guestOptions, (dialogInterface, which) -> {
                            selectedGuests = which + 1;
                            guestsBtn.setText(guestOptions[which]);
                        })
                        .show();
            });

            dialog.show();
        });
    }


    private void applyFilters() {
        List<ReservationModel> all = dbHelper.getAllReservations();
        List<ReservationModel> filtered = new ArrayList<>();

        for (ReservationModel r : all) {
            boolean matchesDate = true;
            boolean matchesGuests = true;

            if (selectedDay != null && selectedMonth != null && selectedYear != null) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy", Locale.ENGLISH);
                    int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                    Date resDate = sdf.parse(r.getDate() + " " + currentYear);
                    Calendar resCal = Calendar.getInstance();
                    assert resDate != null;
                    resCal.setTime(resDate);

                    matchesDate = (resCal.get(Calendar.DAY_OF_MONTH) == selectedDay &&
                            resCal.get(Calendar.MONTH) == selectedMonth &&
                            resCal.get(Calendar.YEAR) == selectedYear);
                } catch (Exception e) {
                    Log.e("ReservationFilter", "Error parsing reservation date", e);
                    matchesDate = false;
                }
            }

            if (selectedGuests != null) {
                matchesGuests = (r.getGuestCount() == selectedGuests ||
                        (selectedGuests == 5 && r.getGuestCount() > 4));
            }

            if (matchesDate && matchesGuests) {
                filtered.add(r);
            }
        }

        if (filtered.isEmpty()) {
            noReservationText.setVisibility(TextView.VISIBLE);
            staffRecycler.setVisibility(RecyclerView.GONE);
        } else {
            noReservationText.setVisibility(TextView.GONE);
            staffRecycler.setVisibility(RecyclerView.VISIBLE);
        }

        adapter.updateList(filtered);
    }


    private String formatDate(int day, int month, int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM", Locale.ENGLISH);
        return sdf.format(cal.getTime());
    }

    private void loadReservations() {
        List<ReservationModel> reservations = dbHelper.getAllReservations();
        if (reservations.isEmpty()) {
            noReservationText.setVisibility(TextView.VISIBLE);
            staffRecycler.setVisibility(RecyclerView.GONE);
            return;
        }

        noReservationText.setVisibility(TextView.GONE);
        staffRecycler.setVisibility(RecyclerView.VISIBLE);
        adapter.updateList(reservations);
    }


    private void cancelReservation(ReservationModel reservation) {
        dbHelper.deleteReservation(reservation.getId());
        loadReservations();

        long currentTime = System.currentTimeMillis();

        String guestId = reservation.getGuestId();

        if (guestId == null || guestId.isEmpty()) {
            Toast.makeText(this, "Failed to notify guest: invalid guest ID", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences guestSp = getSharedPreferences("Notifications_" + guestId, MODE_PRIVATE);
        String guestJson = guestSp.getString("list", "[]");

        Type type = new TypeToken<List<NotificationModel>>() {}.getType();
        List<NotificationModel> guestList = new Gson().fromJson(guestJson, type);

        if (guestList == null) {
            guestList = new ArrayList<>();
        }

        NotificationModel guestNotif = new NotificationModel(
                "Reservation Cancelled",
                "Your reservation for " + reservation.getDate() + " at " + reservation.getTime() +
                        " has been cancelled by staff.",
                currentTime,
                true,
                R.drawable.ic_cancel_circle,
                getResources().getColor(R.color.my_danger, null),
                getResources().getColor(R.color.soft_red, null)
        );

        guestList.add(0, guestNotif);
        guestSp.edit().putString("list", new Gson().toJson(guestList)).apply();

        Toast.makeText(this, "Reservation cancelled successfully!", Toast.LENGTH_SHORT).show();
    }

}
