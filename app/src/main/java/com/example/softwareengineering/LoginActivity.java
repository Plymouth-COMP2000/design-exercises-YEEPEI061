package com.example.softwareengineering;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    CardView staffRole, guestRole;
    ImageView tickStaff, tickGuest, togglePassword;
    private EditText passwordInput;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        staffRole = findViewById(R.id.staffRole);
        guestRole = findViewById(R.id.guestRole);
        tickStaff = findViewById(R.id.tickStaff);
        tickGuest = findViewById(R.id.tickGuest);

        staffRole.setOnClickListener(v -> selectRole(true));
        guestRole.setOnClickListener(v -> selectRole(false));

        // Toggle password
        passwordInput = findViewById(R.id.passwordInput);
        togglePassword = findViewById(R.id.togglePassword);

        togglePassword.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    // Show password
                    passwordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                            android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    togglePassword.setImageResource(R.drawable.ic_visibility);
                    passwordInput.setSelection(passwordInput.length());
                    v.performClick();
                    return true;

                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    // Hide password again
                    passwordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                            android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    togglePassword.setImageResource(R.drawable.ic_visibility_off);
                    passwordInput.setSelection(passwordInput.length());
                    v.performClick();
                    return true;
            }
            return false;
        });


        // Login button setup
        Button loginButton = findViewById(R.id.loginButton);
        EditText emailInput = findViewById(R.id.emailInput);

        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(LoginActivity.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            if (tickStaff.getVisibility() != View.VISIBLE && tickGuest.getVisibility() != View.VISIBLE) {
                Toast.makeText(LoginActivity.this, "Please select a role first", Toast.LENGTH_SHORT).show();
                return;
            }

            if (tickStaff.getVisibility() == View.VISIBLE) {
                Intent intent = new Intent(LoginActivity.this, StaffMenuActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(LoginActivity.this, GuestMenuActivity.class);
                startActivity(intent);
            }
        });

        // Sign up
        TextView signUpText = findViewById(R.id.signUpText);

        signUpText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
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

}
