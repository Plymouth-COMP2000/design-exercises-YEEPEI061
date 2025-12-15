package com.example.softwareengineering;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import android.util.Log;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

public class SignUpActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://10.240.72.69/comp2000/coursework/";
    private static final String STUDENT_ID = "bsse2506028";
    private FrameLayout loadingOverlay;

    @SuppressLint("SetTextI18n")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        EditText passwordInput = findViewById(R.id.passwordInput);
        ImageView togglePassword = findViewById(R.id.togglePassword);
        EditText confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        ImageView toggleConfirmPassword = findViewById(R.id.toggleConfirmPassword);

        setupPasswordToggle(passwordInput, togglePassword);
        setupPasswordToggle(confirmPasswordInput, toggleConfirmPassword);

        loadingOverlay = findViewById(R.id.loadingOverlay);

        ImageButton backButton = findViewById(R.id.backButton);
        boolean fromStaffManagement = getIntent().getBooleanExtra("fromStaffManagement", false);

        // Sign up button
        Button signupButton = findViewById(R.id.signupButton);
        EditText firstNameInput = findViewById(R.id.firstNameInput);
        EditText lastNameInput = findViewById(R.id.lastNameInput);
        EditText usernameInput = findViewById(R.id.usernameInput);
        EditText emailInput = findViewById(R.id.emailInput);
        EditText contactInput = findViewById(R.id.contactInput);

        signupButton.setOnClickListener(v -> {
            String firstName = firstNameInput.getText().toString().trim();
            String lastName = lastNameInput.getText().toString().trim();
            String username = usernameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String contact = contactInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();
            String selectedRole = fromStaffManagement ? "staff" : "guest";

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || username.isEmpty() ||
                    password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(SignUpActivity.this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(SignUpActivity.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(SignUpActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(SignUpActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            loadingOverlay.setVisibility(View.VISIBLE);
            long signupTime = System.currentTimeMillis();

            try {
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("username", username);
                jsonBody.put("password", password);
                jsonBody.put("firstname", firstName);
                jsonBody.put("lastname", lastName);
                jsonBody.put("email", email);
                jsonBody.put("contact", contact);
                jsonBody.put("usertype", selectedRole);

                String createUserUrl = BASE_URL + "create_user/" + STUDENT_ID;

                JsonObjectRequest createRequest = new JsonObjectRequest(
                        Request.Method.POST,
                        createUserUrl,
                        jsonBody,
                        response -> {
                            String readUserUrl = BASE_URL + "read_user/" + STUDENT_ID + "/" + username;

                            JsonObjectRequest readRequest = new JsonObjectRequest(
                                    Request.Method.GET,
                                    readUserUrl,
                                    null,
                                    readResponse -> {
                                        try {
                                            if (readResponse.has("user")) {
                                                JSONObject user = readResponse.getJSONObject("user");
                                                String userId = user.getString("_id");

                                                UserSignupDbHelper dbHelper = new UserSignupDbHelper(SignUpActivity.this);
                                                dbHelper.saveSignupTime(userId, signupTime);

                                                loadingOverlay.setVisibility(View.GONE);

                                                if (fromStaffManagement) {
                                                    Toast.makeText(SignUpActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(SignUpActivity.this, SettingsActivity.class));
                                                } else {
                                                    Toast.makeText(SignUpActivity.this, "Account created! Please choose your profile", Toast.LENGTH_SHORT).show();

                                                    SharedPreferences session = getSharedPreferences("UserSession", MODE_PRIVATE);
                                                    SharedPreferences.Editor editor = session.edit();
                                                    editor.putString("username", username);
                                                    editor.putString("role", selectedRole);
                                                    editor.putLong("signupTime", signupTime);
                                                    editor.apply();

                                                    startActivity(new Intent(SignUpActivity.this, ChooseProfileActivity.class));
                                                }

                                                finish();
                                            } else {
                                                loadingOverlay.setVisibility(View.GONE);
                                                Toast.makeText(SignUpActivity.this, "Failed to get user ID", Toast.LENGTH_LONG).show();
                                            }
                                        } catch (Exception e) {
                                            loadingOverlay.setVisibility(View.GONE);
                                            Log.e("SignUpActivity", "Error parsing user after creation", e);
                                            Toast.makeText(SignUpActivity.this, "Unexpected error", Toast.LENGTH_SHORT).show();
                                        }
                                    },
                                    error -> {
                                        loadingOverlay.setVisibility(View.GONE);
                                        Toast.makeText(SignUpActivity.this, "Failed to retrieve user ID", Toast.LENGTH_LONG).show();
                                        Log.e("SignUpActivity", "Read user error", error);
                                    }
                            );

                            Volley.newRequestQueue(SignUpActivity.this).add(readRequest);

                        },
                        error -> {
                            loadingOverlay.setVisibility(View.GONE);
                            String errorMsg = "Error creating user";

                            if (error.networkResponse != null) {
                                int statusCode = error.networkResponse.statusCode;

                                if (statusCode == 400) {
                                    errorMsg = "This account already exists. Please log in or use a different username.";
                                } else if (statusCode == 500) {
                                    errorMsg = "Server error. Please try again later.";
                                } else {
                                    errorMsg += " (" + statusCode + ")";
                                }
                            } else {
                                errorMsg += " (No network response)";
                            }

                            Toast.makeText(SignUpActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        }
                );

                Volley.newRequestQueue(SignUpActivity.this).add(createRequest);

            } catch (Exception e) {
                loadingOverlay.setVisibility(View.GONE);
                Log.e("SignUpActivity", "Unexpected error during signup", e);
                Toast.makeText(SignUpActivity.this, "Unexpected error", Toast.LENGTH_SHORT).show();
            }
        });


        TextView loginText = findViewById(R.id.loginText);
        TextView alreadyAccountText = findViewById(R.id.alreadyAccountText);
        TextView signupSubtitle = findViewById(R.id.signupSubtitle);


        if (fromStaffManagement) {
            loginText.setVisibility(View.GONE);
            alreadyAccountText.setVisibility(View.GONE);
            signupSubtitle.setText("Staff account registration");
            signupButton.setText("Create");
            backButton.setVisibility(View.VISIBLE);
            backButton.setOnClickListener(v -> finish());
        }

        loginText.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
        });

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
