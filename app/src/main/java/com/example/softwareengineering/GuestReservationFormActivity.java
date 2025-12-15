package com.example.softwareengineering;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Build;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class GuestReservationFormActivity extends AppCompatActivity {

    private EditText dateInput;
    private EditText timeInput;
    private EditText guestCountInput;
    private EditText requestInput;
    private Button saveButton;
    private int reservationId = -1;
    private String mode;
    private static final String CHANNEL_ID = "reservation_channel";
    private static final int REQ_POST_NOTIF = 101;
    private NotificationModel pendingNotification;

    // Table selection
    private enum TableStatus {AVAILABLE, OCCUPIED, SELECTED}

    private final Map<FrameLayout, TableStatus> tableMap = new HashMap<>();
    private FrameLayout selectedTable = null;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest_reservation_form);

        Button cancelReservationButton = findViewById(R.id.cancelReservationButton);
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
        dateInput.addTextChangedListener(refreshWatcher);
        timeInput.addTextChangedListener(refreshWatcher);


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
            final String oldDate = intent.getStringExtra("date");
            final String oldTime = intent.getStringExtra("time");

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

            checkFields();

            if ("edit".equals(mode)) {
                titleText.setText("Edit Reservation");
                saveButton.setText("Save");
                cancelReservationButton.setVisibility(View.VISIBLE);

                saveButton.setOnClickListener(v -> saveReservation());

                cancelReservationButton.setOnClickListener(v -> {
                    SharedPreferences userSession = getSharedPreferences("UserSession", MODE_PRIVATE);
                    String username = userSession.getString("username", "");

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

                                long currentTime = System.currentTimeMillis();

                                if (isNotificationAllowed("notif_cancel")) {
                                    NotificationModel notif = new NotificationModel(
                                            "Reservation Cancelled",
                                            "You have successfully cancelled your reservation for " + oldDate + " at " + oldTime + ".",
                                            currentTime,
                                            true,
                                            R.drawable.ic_cancel_circle,
                                            getResources().getColor(R.color.my_danger, null),
                                            getResources().getColor(R.color.soft_red, null)
                                    );

                                    addNotification(notif);
                                    checkPermissionAndNotify(notif);
                                }

                                String staffMessage = username + " cancelled their reservation for " + oldDate + " at " + oldTime + ".";
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


        boolean dateValid = true;

        if (allFilled) {
            try {
                String dateStr = dateInput.getText().toString().trim();
                String timeStr = timeInput.getText().toString().trim();
                int year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
                String fullDateStr = dateStr + " " + year + " " + timeStr;

                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm a");
                sdf.setLenient(false);
                Date selectedDate = sdf.parse(fullDateStr);
                assert selectedDate != null;
                dateValid = !selectedDate.before(new Date());


            } catch (Exception e) {
                dateValid = false;
            }
        }

        boolean canSave = allFilled && dateValid;

        saveButton.setEnabled(canSave);
        saveButton.setBackgroundTintList(ContextCompat.getColorStateList(
                this, canSave ? R.color.my_primary : R.color.my_secondary
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
            int tintColor = Objects.requireNonNull(table.getBackgroundTintList()).getDefaultColor();
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

        SharedPreferences userSession = getSharedPreferences("UserSession", MODE_PRIVATE);
        String username = userSession.getString("username", "");
        String userId = userSession.getString("userId", "");

        String tableName = "Table " + getResources()
                .getResourceEntryName(selectedTable.getId())
                .replace("tableT", "");
        String date = dateInput.getText().toString().trim();

        String time = timeInput.getText().toString().trim();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("h:mm a"); // matches "9:00 PM", "10:00 AM"
        try {
            Date d = sdf.parse(time);

            @SuppressLint("SimpleDateFormat") SimpleDateFormat hourFormat = new SimpleDateFormat("H");   // 24-hour hour
            @SuppressLint("SimpleDateFormat") SimpleDateFormat minuteFormat = new SimpleDateFormat("m"); // minute
            assert d != null;
            int hour = Integer.parseInt(hourFormat.format(d));
            int minute = Integer.parseInt(minuteFormat.format(d));

            // 10:00 to 21:00 inclusive
            if (hour < 10 || hour > 21 || (hour == 21 && minute > 0)) {
                Toast.makeText(this, "Reservation time must be between 10:00 AM and 9:00 PM", Toast.LENGTH_SHORT).show();
                return;
            }

        } catch (Exception e) {
            Toast.makeText(this, "Invalid time format", Toast.LENGTH_SHORT).show();
            return;
        }

        int guests = Integer.parseInt(guestCountInput.getText().toString().trim());
        if (guests < 1 || guests > 10) {
            Toast.makeText(this, "Number of guests must be between 1 and 10", Toast.LENGTH_SHORT).show();
            return;
        }

        String request = requestInput.getText().toString().trim();

        ReservationDatabaseHelper db = new ReservationDatabaseHelper(this);

        String title, message;
        if ("edit".equals(mode)) {
            title = "Save Changes";
            message = "Are you sure you want to save changes?";
        } else if ("bookAgain".equals(mode)) {
            title = "Book Reservation";
            message = "Confirm booking this reservation?";
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
                    boolean isEdit = "edit".equals(mode);

                    if (isEdit) {
                        success = db.updateReservation(reservationId, date, time, guests, request, tableName, username);
                    } else {
                        success = db.addReservation(date, time, guests, request, tableName, username, userId);
                    }

                    if (success) {
                        Toast.makeText(this,
                                "Reservation " + (isEdit ? "updated" : "booked") + " successfully!",
                                Toast.LENGTH_SHORT).show();

                        long currentTime = System.currentTimeMillis();

                        String prefKey = isEdit ? "notif_update" : "notif_new";

                        if (isNotificationAllowed(prefKey)) {
                            NotificationModel guestNotif = new NotificationModel(
                                    isEdit ? "Reservation Modified" : "Reservation Successful",
                                    isEdit ?
                                            "Your reservation has been successfully changed to " + guests + " people at " + time + " on " + date + " (" + tableName + ").":
                                            "Your reservation for " + guests + " people on " + date + " at " + time + " is successfully reserved (" + tableName + ").",
                                    currentTime,
                                    true,
                                    isEdit ? R.drawable.ic_modify : R.drawable.ic_check_circle,
                                    isEdit ? getResources().getColor(R.color.my_primary, null) : getResources().getColor(R.color.green, null),
                                    isEdit ? getResources().getColor(R.color.my_light_secondary, null) : getResources().getColor(R.color.soft_green, null)
                            );

                            addNotification(guestNotif);
                            checkPermissionAndNotify(guestNotif);
                        }

                        String staffMessage = isEdit ?
                                username + " updated a reservation for " + guests + " people at " + time + " on " + date + " (" + tableName + ").":
                                username + " booked a reservation for " + guests + " people at " + time + " on " + date + " (" + tableName + ").";

                        NotificationModel staffNotif = new NotificationModel(
                                isEdit ? "Reservation Updated" : "New Reservation",
                                staffMessage,
                                currentTime,
                                true,
                                isEdit ? R.drawable.ic_modify : R.drawable.ic_restaurant,
                                isEdit ? getResources().getColor(R.color.my_primary, null) : getResources().getColor(R.color.green, null),
                                isEdit ? getResources().getColor(R.color.my_light_secondary, null) : getResources().getColor(R.color.soft_green, null)
                        );

                        addNotificationForStaff(staffNotif);

                        db.close();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, "Error saving reservation", Toast.LENGTH_SHORT).show();
                    }
                }
        );

    }

    private void addNotification(NotificationModel notification) {
        SharedPreferences userSession = getSharedPreferences("UserSession", MODE_PRIVATE);
        String userId = userSession.getString("userId", "");

        SharedPreferences sp = getSharedPreferences("Notifications_" + userId, MODE_PRIVATE);
        String json = sp.getString("list", "[]");

        Type type = new TypeToken<List<NotificationModel>>() {}.getType();
        List<NotificationModel> notifications = new Gson().fromJson(json, type);

        notifications.add(0, notification);
        sp.edit().putString("list", new Gson().toJson(notifications)).apply();
    }

    private void checkPermissionAndNotify(NotificationModel notif) {

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
        notif.setDisplayed(true);
        saveNotificationImmediate(notif);
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

    private boolean isNotificationAllowed(String key) {
        SharedPreferences userSession = getSharedPreferences("UserSession", MODE_PRIVATE);
        String userId = userSession.getString("userId", "");

        SharedPreferences prefs = getSharedPreferences("NotificationPrefs_" + userId, MODE_PRIVATE);

        return prefs.getBoolean(key, true);
    }

    private void addNotificationForStaff(NotificationModel notif) {
        SharedPreferences sp = getSharedPreferences("Notifications_staff", MODE_PRIVATE);
        String json = sp.getString("list", "[]");

        Type type = new TypeToken<List<NotificationModel>>() {}.getType();
        List<NotificationModel> notifications = new Gson().fromJson(json, type);

        notifications.add(0, notif);
        sp.edit().putString("list", new Gson().toJson(notifications)).apply();
    }

    TextWatcher refreshWatcher = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void afterTextChanged(Editable s) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            updateTableAvailability();
        }
    };

    private void updateTableAvailability() {
        String date = dateInput.getText().toString().trim();
        String time = timeInput.getText().toString().trim();

        if (date.isEmpty() || time.isEmpty()) return;

        ReservationDatabaseHelper db = new ReservationDatabaseHelper(this);
        List<ReservationModel> reservations = db.getAllReservationsWithDateTime();
        db.close();

        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm a", Locale.ENGLISH);
        Date selectedDateTime;

        try {
            int year = Calendar.getInstance().get(Calendar.YEAR);
            String combined = date + " " + year + " " + time;
            selectedDateTime = sdf.parse(combined);

        } catch (Exception e) {
            return;
        }

        assert selectedDateTime != null;
        long selectedStart = selectedDateTime.getTime();
        long selectedEnd = selectedStart + (45 * 60 * 1000);

        for (Map.Entry<FrameLayout, TableStatus> entry : tableMap.entrySet()) {
            entry.getKey().setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.green)
            ));
            tableMap.put(entry.getKey(), TableStatus.AVAILABLE);
        }

        for (ReservationModel r : reservations) {

            long rStart = r.getDateTimeMillis();
            long rEnd = rStart + (45 * 60 * 1000);

            boolean overlap = (selectedStart < rEnd && rStart < selectedEnd);

            if (overlap) {
                for (FrameLayout table : tableMap.keySet()) {
                    String tableName =
                            "Table " + getResources()
                                    .getResourceEntryName(table.getId())
                                    .replace("tableT", "");

                    if (tableName.equals(r.getTable())) {

                        table.setBackgroundTintList(ColorStateList.valueOf(
                                ContextCompat.getColor(this, R.color.gray)
                        ));
                        tableMap.put(table, TableStatus.OCCUPIED);
                    }
                }
            }
        }

        // Keep selected table blue
        if (selectedTable != null && tableMap.get(selectedTable) == TableStatus.AVAILABLE) {
            selectedTable.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.my_primary)
            ));
            tableMap.put(selectedTable, TableStatus.SELECTED);
        }
    }

    private void saveNotificationImmediate(NotificationModel notif) {
        SharedPreferences userSession = getSharedPreferences("UserSession", MODE_PRIVATE);
        String userId = userSession.getString("userId", "");

        String prefName = "Notifications_" + userId;
        SharedPreferences sp = getSharedPreferences(prefName, MODE_PRIVATE);
        String json = sp.getString("list", "[]");

        Type type = new TypeToken<List<NotificationModel>>() {}.getType();
        List<NotificationModel> notifications = new Gson().fromJson(json, type);

        for (NotificationModel n : notifications) {
            if (n.getTimestamp() == notif.getTimestamp() && n.getTitle().equals(notif.getTitle())) {
                n.setDisplayed(true);
                break;
            }
        }

        sp.edit().putString("list", new Gson().toJson(notifications)).apply();
    }

}
