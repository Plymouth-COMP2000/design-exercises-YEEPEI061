package com.example.softwareengineering;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class EditProfileActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://10.240.72.69/comp2000/coursework/";
    private static final String STUDENT_ID = "bsse2506028";
    private String USER_ID;
    private String actualPassword;

    private EditText passwordInput, confirmPasswordInput, firstNameInput, lastNameInput,
            usernameInput, emailInput, contactInput;
    private FrameLayout loadingOverlay;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        passwordInput = findViewById(R.id.passwordInput);
        ImageView togglePassword = findViewById(R.id.togglePassword);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        ImageView toggleConfirmPassword = findViewById(R.id.toggleConfirmPassword);

        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        usernameInput = findViewById(R.id.usernameInput);
        emailInput = findViewById(R.id.emailInput);
        contactInput = findViewById(R.id.contactInput);
        USER_ID = getSharedPreferences("UserSession", MODE_PRIVATE)
                .getString("username", "");
        if (USER_ID.isEmpty()) {
            Toast.makeText(this, "No logged-in user found", Toast.LENGTH_SHORT).show();
            finish(); // close activity
        }

        loadingOverlay = findViewById(R.id.loadingOverlay);
        loadingOverlay.setVisibility(View.GONE);

        setupPasswordToggle(passwordInput, togglePassword);
        setupPasswordToggle(confirmPasswordInput, toggleConfirmPassword);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        Button cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> finish());

        fetchUserData();

        // Save button
        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> saveChanges());
    }

    private void fetchUserData() {
        loadingOverlay.setVisibility(View.VISIBLE);
        String url = BASE_URL + "read_user/" + STUDENT_ID + "/" + USER_ID;

        JsonObjectRequest getRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        if (response.has("user")) {
                            JSONObject user = response.getJSONObject("user");
                            firstNameInput.setText(user.getString("firstname"));
                            lastNameInput.setText(user.getString("lastname"));
                            usernameInput.setText(user.getString("username"));
                            emailInput.setText(user.getString("email"));
                            contactInput.setText(user.getString("contact"));
                            actualPassword = user.getString("password");
                        } else {
                            Toast.makeText(EditProfileActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(EditProfileActivity.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                    } finally {
                        loadingOverlay.setVisibility(View.GONE);
                    }
                },
                error -> {
                    Toast.makeText(EditProfileActivity.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                    loadingOverlay.setVisibility(View.GONE);
                }
        );

        Volley.newRequestQueue(this).add(getRequest);
    }
    private void saveChanges() {
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String username = usernameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String contact = contactInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        if (!password.isEmpty() && !password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingOverlay.setVisibility(View.VISIBLE);

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("firstname", firstName);
            jsonBody.put("lastname", lastName);
            jsonBody.put("username", username);
            jsonBody.put("email", email);
            jsonBody.put("contact", contact);

            jsonBody.put("password", password.isEmpty() ? actualPassword : password);

            SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
            String usertype = prefs.getString("role", null);

            if (usertype != null) {
                jsonBody.put("usertype", usertype);
            } else {
                loadingOverlay.setVisibility(View.GONE);
                return;
            }

            String url = BASE_URL + "update_user/" + STUDENT_ID + "/" + USER_ID;

            JsonObjectRequest putRequest = new JsonObjectRequest(
                    Request.Method.PUT,
                    url,
                    jsonBody,
                    response -> {
                        Toast.makeText(EditProfileActivity.this, "Changes saved successfully!", Toast.LENGTH_SHORT).show();
                        loadingOverlay.setVisibility(View.GONE);
                        finish();
                    },
                    error -> {
                        Toast.makeText(EditProfileActivity.this, "Failed to update profile", Toast.LENGTH_LONG).show();
                        loadingOverlay.setVisibility(View.GONE);
                    }
            );

            Volley.newRequestQueue(this).add(putRequest);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Unexpected error", Toast.LENGTH_SHORT).show();
            loadingOverlay.setVisibility(View.GONE);
        }
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
