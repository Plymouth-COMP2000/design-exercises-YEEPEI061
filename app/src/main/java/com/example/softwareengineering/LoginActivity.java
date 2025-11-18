package com.example.softwareengineering;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    CardView staffRole, guestRole;
    ImageView tickStaff, tickGuest, togglePassword;
    private EditText passwordInput;
    private static final String BASE_URL = "http://10.240.72.69/comp2000/coursework/";
    private static final String STUDENT_ID = "bsse2506028";

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
        EditText usernameInput = findViewById(R.id.usernameInput);

        loginButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String selectedRole = tickStaff.getVisibility() == View.VISIBLE ? "staff" : "guest";

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            if (tickStaff.getVisibility() != View.VISIBLE && tickGuest.getVisibility() != View.VISIBLE) {
                Toast.makeText(LoginActivity.this, "Please select a role first", Toast.LENGTH_SHORT).show();
                return;
            }

            String url = BASE_URL + "read_user/" + STUDENT_ID + "/" + username;

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.GET,
                    url,
                    null,
                    response -> {
                        try {
                            if (response.has("user")) {
                                JSONObject user = response.getJSONObject("user");
                                String correctPassword = user.getString("password");
                                String usertype = user.getString("usertype");

                                if (!password.equals(correctPassword)) {
                                    Toast.makeText(LoginActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                if (!selectedRole.equals(usertype)) {
                                    Toast.makeText(LoginActivity.this, "Selected role does not match account role", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();

                                if (selectedRole.equals("staff")) {
                                    saveUserSession(username);
                                    startActivity(new Intent(LoginActivity.this, StaffMenuActivity.class));
                                } else {
                                    saveUserSession(username);
                                    startActivity(new Intent(LoginActivity.this, GuestMenuActivity.class));
                                }

                            } else {
                                Toast.makeText(LoginActivity.this, "User not found. Please sign up first.", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
                            }

                        } catch (Exception e) {
                            Log.e("LoginError", "Exception parsing user data", e);
                            Toast.makeText(LoginActivity.this, "Error reading user data", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                            Toast.makeText(LoginActivity.this, "User not found. Please sign up first.", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
                        } else {
                            Toast.makeText(LoginActivity.this, "Login failed. Check network or user ID", Toast.LENGTH_LONG).show();
                            Log.e("LoginError", "Volley error", error);
                        }
                    }
            );

            Volley.newRequestQueue(LoginActivity.this).add(request);
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

    private void saveUserSession(String username) {
        getSharedPreferences("UserSession", MODE_PRIVATE)
                .edit()
                .putString("username", username)
                .apply();
    }


}
