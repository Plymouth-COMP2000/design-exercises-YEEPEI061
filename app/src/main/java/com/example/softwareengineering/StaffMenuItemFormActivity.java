package com.example.softwareengineering;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class StaffMenuItemFormActivity extends AppCompatActivity {

    private EditText itemNameInput, priceInput, descriptionInput;
    private Button saveButton;

    @SuppressLint("SetTextI18n")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_menu_item_form);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        Button cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        itemNameInput = findViewById(R.id.itemNameInput);
        priceInput = findViewById(R.id.priceInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        Spinner categorySpinner = findViewById(R.id.categorySpinner);
        saveButton = findViewById(R.id.saveButton);
        TextView titleText = findViewById(R.id.titleText);

        Intent intent = getIntent();
        String mode = intent.getStringExtra("mode");

        if ("edit".equals(mode)) {
            titleText.setText("Edit Menu Item");
            saveButton.setText("Save Changes");

            itemNameInput.setText(intent.getStringExtra("name"));
            priceInput.setText(intent.getStringExtra("price"));
            descriptionInput.setText(intent.getStringExtra("description"));
            // TODO: set spinner selection if needed

            saveButton.setOnClickListener(v -> {
                PopupHelper.showPopup(
                        this,
                        R.drawable.ic_check_circle,
                        getResources().getColor(R.color.green, null),
                        "Are you sure you want to save changes?",
                        () -> {
                            Toast.makeText(this, "Changes saved successfully!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                );
            });

        } else {
            titleText.setText("Add Menu Item");
            saveButton.setText("Add Item");

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

            itemNameInput.addTextChangedListener(watcher);
            priceInput.addTextChangedListener(watcher);
            descriptionInput.addTextChangedListener(watcher);

            saveButton.setOnClickListener(v -> {
                if (!saveButton.isEnabled()) {
                    Toast.makeText(this, "Please fill out all fields!", Toast.LENGTH_SHORT).show();
                } else {
                    PopupHelper.showPopup(
                            this,
                            R.drawable.ic_check_circle,
                            getResources().getColor(R.color.green, null),
                            "Confirm adding this new item?",
                            () -> {
                                Toast.makeText(this, "Item added successfully!", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                    );
                }
            });
        }
    }

    private void checkFields() {
        boolean allFilled = !itemNameInput.getText().toString().trim().isEmpty()
                && !priceInput.getText().toString().trim().isEmpty()
                && !descriptionInput.getText().toString().trim().isEmpty();

        saveButton.setEnabled(allFilled);
        saveButton.setBackgroundTintList(ContextCompat.getColorStateList(
                this, allFilled ? R.color.my_primary : R.color.my_secondary
        ));
    }
}

