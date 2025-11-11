package com.example.softwareengineering;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class GuestReservationFormActivity extends AppCompatActivity {

    private EditText dateInput;
    private EditText timeInput;
    private EditText guestCountInput;
    private Button saveButton;

    @SuppressLint("SetTextI18n")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest_reservation_form);

        Button cancelReservationButton = findViewById(R.id.cancelReservationButton);

        ImageButton backButton = findViewById(R.id.settingsButton);
        backButton.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        Button cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        dateInput = findViewById(R.id.dateInput);
        timeInput = findViewById(R.id.timeInput);
        guestCountInput = findViewById(R.id.guestCountInput);
        EditText requestInput = findViewById(R.id.requestInput);
        saveButton = findViewById(R.id.saveButton);
        TextView titleText = findViewById(R.id.titleText);

        Intent intent = getIntent();
        String mode = intent.getStringExtra("mode");

        if ("edit".equals(mode)) {
            titleText.setText("Edit Reservation");
            saveButton.setText("Save Changes");

            cancelReservationButton.setVisibility(View.VISIBLE);

            dateInput.setText(intent.getStringExtra("date"));
            timeInput.setText(intent.getStringExtra("time"));
            guestCountInput.setText(intent.getStringExtra("guestCount"));
            requestInput.setText(intent.getStringExtra("request"));

            saveButton.setOnClickListener(v -> {
                PopupHelper.showPopup(
                        this,
                        R.drawable.ic_check_circle,
                        getResources().getColor(R.color.green, null),
                        "Save Changes",
                        "Are you sure you want to save changes?",
                        () -> {
                            Toast.makeText(this, "Changes saved successfully!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                );
            });

            cancelReservationButton.setOnClickListener(v -> {
                PopupHelper.showPopup(
                        this,
                        R.drawable.ic_cancel_circle,
                        getResources().getColor(R.color.my_danger, null),
                        "Cancel Reservation",
                        "Are you sure you want to cancel this reservation?",
                        () -> {
                            Toast.makeText(this, "Reservation cancelled successfully!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                );
            });

        } else {
            titleText.setText("Make New Reservation");
            saveButton.setText("Add Reservation");

            cancelReservationButton.setVisibility(View.GONE);

            saveButton.setEnabled(false);
            saveButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.my_secondary));

            TextWatcher watcher = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void afterTextChanged(Editable s) {}
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
                    PopupHelper.showPopup(
                            this,
                            R.drawable.ic_info,
                            getResources().getColor(R.color.my_primary, null),
                            "Add Reservation",
                            "Confirm adding this reservation?",
                            () -> {
                                Toast.makeText(this, "Reservation added successfully!", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                    );
                }
            });
        }

    }

    private void checkFields() {
        boolean allFilled = !dateInput.getText().toString().trim().isEmpty()
                && !timeInput.getText().toString().trim().isEmpty()
                && !guestCountInput.getText().toString().trim().isEmpty();

        saveButton.setEnabled(allFilled);
        saveButton.setBackgroundTintList(ContextCompat.getColorStateList(
                this, allFilled ? R.color.my_primary : R.color.my_secondary
        ));
    }
}
