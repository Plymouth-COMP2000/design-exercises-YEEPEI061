package com.example.softwareengineering;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditProfileActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Button  saveButton = findViewById(R.id. saveButton);
        saveButton.setOnClickListener(v -> {
            PopupHelper.showPopup(
                    this,
                    R.drawable.ic_info,
                    getResources().getColor(R.color.my_primary, null),
                    "Save Changes",
                    "Are you sure you want to save changes?",
                    () -> {
                        Toast.makeText(this, "Changes saved successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
            );
        });

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


    }
}
