package com.example.softwareengineering;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class SignUpActivity extends AppCompatActivity {

    CardView staffRole, guestRole;
    ImageView tickStaff, tickGuest;
    private EditText passwordInput;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        staffRole = findViewById(R.id.staffRole);
        guestRole = findViewById(R.id.guestRole);
        tickStaff = findViewById(R.id.tickStaff);
        tickGuest = findViewById(R.id.tickGuest);

        staffRole.setOnClickListener(v -> selectRole(true));
        guestRole.setOnClickListener(v -> selectRole(false));

        passwordInput = findViewById(R.id.passwordInput);
        ImageView togglePassword = findViewById(R.id.togglePassword);
        EditText confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        ImageView toggleConfirmPassword = findViewById(R.id.toggleConfirmPassword);

        setupPasswordToggle(passwordInput, togglePassword);
        setupPasswordToggle(confirmPasswordInput, toggleConfirmPassword);

        // Sign up button
        Button signupButton = findViewById(R.id.signupButton);
        EditText firstNameInput = findViewById(R.id.firstNameInput);
        EditText lastNameInput = findViewById(R.id.lastNameInput);
        EditText emailInput = findViewById(R.id.emailInput);

        signupButton.setOnClickListener(v -> {
            String firstName = firstNameInput.getText().toString().trim();
            String lastName = lastNameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();

            // Validate required fields
            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() ||
                    password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(SignUpActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate email format
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(SignUpActivity.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate password match
            if (!password.equals(confirmPassword)) {
                Toast.makeText(SignUpActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate role selection
            if (tickStaff.getVisibility() != View.VISIBLE && tickGuest.getVisibility() != View.VISIBLE) {
                Toast.makeText(SignUpActivity.this, "Please select a role first", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(SignUpActivity.this, "Sign up successful", Toast.LENGTH_SHORT).show();

            // All checks passed → go to LoginActivity
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            Toast.makeText(SignUpActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
        });

        // Sign up
        TextView loginText = findViewById(R.id.loginText);

        loginText.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
        });

    }

    private void selectRole(boolean isStaff) {
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if (isStaff) {
            tickStaff.setVisibility(View.VISIBLE);
            tickGuest.setVisibility(View.GONE);

            staffRole.setCardBackgroundColor(getColor(R.color.my_tertiary));
            guestRole.setCardBackgroundColor(getColor(R.color.white));

            editor.putString("role", "staff");
        } else {
            tickGuest.setVisibility(View.VISIBLE);
            tickStaff.setVisibility(View.GONE);

            guestRole.setCardBackgroundColor(getColor(R.color.my_tertiary));
            staffRole.setCardBackgroundColor(getColor(R.color.white));

            editor.putString("role", "guest");
        }

        editor.apply();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupPasswordToggle(EditText inputField, ImageView toggleIcon) {
        toggleIcon.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    inputField.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                            android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    toggleIcon.setImageResource(R.drawable.ic_visibility);
                    inputField.setSelection(inputField.length());
                    v.performClick();
                    return true;

                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    inputField.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                            android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    toggleIcon.setImageResource(R.drawable.ic_visibility_off);
                    inputField.setSelection(inputField.length());
                    v.performClick();
                    return true;
            }
            return false;
        });
    }
}
