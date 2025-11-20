package com.example.softwareengineering;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class GuestReservationFormActivity extends AppCompatActivity {

    private EditText dateInput;
    private EditText timeInput;
    private EditText guestCountInput;
    private EditText requestInput;
    private Button saveButton;
    private Button cancelReservationButton;
    private int reservationId = -1;
    private String mode;


    // Table selection
    private enum TableStatus {AVAILABLE, OCCUPIED, SELECTED}

    private Map<FrameLayout, TableStatus> tableMap = new HashMap<>();
    private FrameLayout selectedTable = null;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest_reservation_form);

        // Views
        cancelReservationButton = findViewById(R.id.cancelReservationButton);
        ImageButton backButton = findViewById(R.id.settingsButton);
        backButton.setOnClickListener(v -> finish());

        Button cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> finish());

        dateInput = findViewById(R.id.dateInput);
        timeInput = findViewById(R.id.timeInput);
        guestCountInput = findViewById(R.id.guestCountInput);
        requestInput = findViewById(R.id.requestInput);
        saveButton = findViewById(R.id.saveButton);
        TextView titleText = findViewById(R.id.titleText);

        dateInput.setFocusable(false);
        dateInput.setClickable(true);
        dateInput.setOnClickListener(v -> showDatePicker());

        timeInput.setFocusable(false);
        timeInput.setClickable(true);
        timeInput.setOnClickListener(v -> showTimePicker());

        setupTables();

        Intent intent = getIntent();

        mode = intent.getStringExtra("mode");
        reservationId = intent.getIntExtra("reservationId", -1);

        if ("edit".equals(mode) || "bookAgain".equals(mode)) {
            dateInput.setText(intent.getStringExtra("date"));
            timeInput.setText(intent.getStringExtra("time"));
            guestCountInput.setText(String.valueOf(intent.getIntExtra("guestCount", 1)));
            requestInput.setText(intent.getStringExtra("specialRequest"));

            String bookedTable = intent.getStringExtra("table");
            if (bookedTable != null) {
                for (Map.Entry<FrameLayout, TableStatus> entry : tableMap.entrySet()) {
                    String tableNumber = "Table " + getResources()
                            .getResourceEntryName(entry.getKey().getId())
                            .replace("tableT", "");
                    if (tableNumber.equals(bookedTable)) {
                        selectedTable = entry.getKey();
                        selectedTable.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.my_primary));
                        tableMap.put(selectedTable, TableStatus.SELECTED);
                    }
                }
            }

            if ("edit".equals(mode)) {
                titleText.setText("Edit Reservation");
                saveButton.setText("Save Changes");
                cancelReservationButton.setVisibility(View.VISIBLE);

                saveButton.setOnClickListener(v -> saveReservation());

                cancelReservationButton.setOnClickListener(v -> {
                    PopupHelper.showPopup(
                            this,
                            R.drawable.ic_cancel_circle,
                            getResources().getColor(R.color.my_danger, null),
                            "Cancel Reservation",
                            "Are you sure you want to cancel this reservation?",
                            () -> {
                                ReservationDatabaseHelper dbHelper = new ReservationDatabaseHelper(this);
                                dbHelper.deleteReservation(reservationId);
                                dbHelper.close();

                                Toast.makeText(this, "Reservation cancelled successfully!", Toast.LENGTH_SHORT).show();

                                setResult(RESULT_OK);
                                finish();
                            }
                    );
                });

            } else {
                titleText.setText("Book Again");
                saveButton.setText("Book Reservation");
                cancelReservationButton.setVisibility(View.GONE);

                saveButton.setOnClickListener(v -> saveReservation());
            }

        } else {
            titleText.setText("Make New Reservation");
            saveButton.setText("Book Reservation");
            cancelReservationButton.setVisibility(View.GONE);
            saveButton.setEnabled(false);
            saveButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.my_secondary));

            TextWatcher watcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    checkFields();
                }
            };

            dateInput.addTextChangedListener(watcher);
            timeInput.addTextChangedListener(watcher);
            guestCountInput.addTextChangedListener(watcher);

            saveButton.setOnClickListener(v -> {
                if (!saveButton.isEnabled()) {
                    Toast.makeText(this, "Please fill out all required fields!", Toast.LENGTH_SHORT).show();
                } else {
                    saveReservation();
                }
            });
        }
    }

    private void checkFields() {
        boolean allFilled = !dateInput.getText().toString().trim().isEmpty()
                && !timeInput.getText().toString().trim().isEmpty()
                && !guestCountInput.getText().toString().trim().isEmpty()
                && selectedTable != null;

        saveButton.setEnabled(allFilled);
        saveButton.setBackgroundTintList(ContextCompat.getColorStateList(
                this, allFilled ? R.color.my_primary : R.color.my_secondary
        ));
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedCal = Calendar.getInstance();
                    selectedCal.set(selectedYear, selectedMonth, selectedDay);

                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEE, dd MMM", java.util.Locale.getDefault());
                    String formattedDate = sdf.format(selectedCal.getTime());

                    dateInput.setText(formattedDate);
                }, year, month, day);
        datePicker.show();
    }

    private void showTimePicker() {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePicker = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    Calendar selectedCal = Calendar.getInstance();
                    selectedCal.set(Calendar.HOUR_OF_DAY, selectedHour);
                    selectedCal.set(Calendar.MINUTE, selectedMinute);

                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault());
                    String formattedTime = sdf.format(selectedCal.getTime());

                    timeInput.setText(formattedTime);
                }, hour, minute, false);
        timePicker.show();
    }

    // Table Selection
    private void setupTables() {
        FrameLayout[] tables = new FrameLayout[]{
                findViewById(R.id.tableT1),
                findViewById(R.id.tableT2),
                findViewById(R.id.tableT3),
                findViewById(R.id.tableT4),
                findViewById(R.id.tableT5),
                findViewById(R.id.tableT6),
                findViewById(R.id.tableT7),
                findViewById(R.id.tableT8),
                findViewById(R.id.tableT9),
                findViewById(R.id.tableT10),
                findViewById(R.id.tableT11),
                findViewById(R.id.tableT12)
        };

        for (FrameLayout table : tables) {
            int tintColor = table.getBackgroundTintList().getDefaultColor();
            if (tintColor == ContextCompat.getColor(this, R.color.green)) {
                tableMap.put(table, TableStatus.AVAILABLE);
            } else {
                tableMap.put(table, TableStatus.OCCUPIED);
            }

            table.setOnClickListener(v -> handleTableClick(table));
        }
    }

    private void handleTableClick(FrameLayout table) {
        TableStatus status = tableMap.get(table);

        if (status == TableStatus.OCCUPIED) {
            Toast.makeText(this, "This table is occupied", Toast.LENGTH_SHORT).show();
            return;
        }

        if (status == TableStatus.AVAILABLE) {
            if (selectedTable != null) {
                selectedTable.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green)));
                tableMap.put(selectedTable, TableStatus.AVAILABLE);
            }

            table.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.my_primary)));
            tableMap.put(table, TableStatus.SELECTED);
            selectedTable = table;
        } else if (status == TableStatus.SELECTED) {
            table.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green)));
            tableMap.put(table, TableStatus.AVAILABLE);
            selectedTable = null;
        }

        checkFields();
    }

    private void saveReservation() {
        if (selectedTable == null) {
            Toast.makeText(this, "Please select a table!", Toast.LENGTH_SHORT).show();
            return;
        }

        String username = getSharedPreferences("UserSession", MODE_PRIVATE)
                .getString("username", "");


        String tableName = "Table " + getResources()
                .getResourceEntryName(selectedTable.getId())
                .replace("tableT", "");
        String date = dateInput.getText().toString().trim();
        String time = timeInput.getText().toString().trim();
        int guests = Integer.parseInt(guestCountInput.getText().toString().trim());
        String request = requestInput.getText().toString().trim();

        ReservationDatabaseHelper db = new ReservationDatabaseHelper(this);

        String title, message;
        if ("edit".equals(mode)) {
            title = "Save Changes";
            message = "Are you sure you want to save changes?";
        } else if ("bookAgain".equals(mode)) {
            title = "Book Reservation";
            message = "Confirm booking this reservation again?";
        } else {
            title = "Book Reservation";
            message = "Confirm booking this reservation?";
        }

        PopupHelper.showPopup(
                this,
                R.drawable.ic_info,
                getResources().getColor(R.color.my_primary, null),
                title,
                message,
                () -> {
                    boolean success;
                    if ("edit".equals(mode)) {
                        success = db.updateReservation(reservationId, date, time, guests, request, tableName, username);
                    } else {
                        success = db.addReservation(date, time, guests, request, tableName, username);
                    }

                    if (success) {
                        Toast.makeText(this,
                                "Reservation " + ("edit".equals(mode) ? "updated" : "booked") + " successfully!",
                                Toast.LENGTH_SHORT).show();
                        db.close();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, "Error saving reservation", Toast.LENGTH_SHORT).show();
                    }
                }
        );

    }



}
