package com.example.softwareengineering;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class EditProfileActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://10.240.72.69/comp2000/coursework/";
    private static final String STUDENT_ID = "bsse2506028";

    private String USER_ID;
    private String actualPassword;

    private EditText passwordInput, confirmPasswordInput, firstNameInput, lastNameInput,
            usernameInput, emailInput, contactInput;

    private FrameLayout loadingOverlay;
    private ImageView profileImage;
    private ImageButton changePhotoButton;

    private ActivityResultLauncher<Intent> galleryLauncher;
    private String tempProfileImagePath = null;

    private SharedPreferences profilePrefs;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        profileImage = findViewById(R.id.profileImage);
        changePhotoButton = findViewById(R.id.changePhotoButton);

        SharedPreferences sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE);
        profilePrefs = getSharedPreferences("ProfilePrefs", MODE_PRIVATE);

        USER_ID = sharedPref.getString("username", "");
        if (USER_ID.isEmpty()) {
            Toast.makeText(this, "No logged-in user found", Toast.LENGTH_SHORT).show();
            finish();
        }

        String savedPath = profilePrefs.getString("profileImagePath_" + USER_ID, null);
        if (savedPath != null) {
            setProfileImageFromFile(savedPath);
        } else {
            String gender = profilePrefs.getString("profile", "boy");
            profileImage.setImageResource(
                    "boy".equals(gender) ? R.drawable.sample_profile_boy : R.drawable.sample_profile
            );
        }

        setupGalleryLauncher();
        profileImage.setOnClickListener(v -> openGallery());
        changePhotoButton.setOnClickListener(v -> openGallery());

        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        ImageView togglePassword = findViewById(R.id.togglePassword);
        ImageView toggleConfirmPassword = findViewById(R.id.toggleConfirmPassword);

        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        usernameInput = findViewById(R.id.usernameInput);
        emailInput = findViewById(R.id.emailInput);
        contactInput = findViewById(R.id.contactInput);

        loadingOverlay = findViewById(R.id.loadingOverlay);
        loadingOverlay.setVisibility(View.GONE);

        setupPasswordToggle(passwordInput, togglePassword);
        setupPasswordToggle(confirmPasswordInput, toggleConfirmPassword);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
        Button cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> finish());

        fetchUserData();

        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> saveChanges());
    }

    private void setupGalleryLauncher() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();

                        loadingOverlay.setVisibility(View.VISIBLE);

                        new Thread(() -> {
                            tempProfileImagePath = saveImageToInternalStorage(imageUri, "temp_" + USER_ID);

                            runOnUiThread(() -> {
                                if (tempProfileImagePath != null) {
                                    setProfileImageFromFile(tempProfileImagePath);
                                }
                                loadingOverlay.setVisibility(View.GONE);
                            });
                        }).start();
                    }
                }
        );
    }

    private void setProfileImageFromFile(String path) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        if (bitmap != null) profileImage.setImageBitmap(bitmap);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private String saveImageToInternalStorage(Uri uri, String fileNamePrefix) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            String fileName = fileNamePrefix + ".jpg";
            File file = new File(getFilesDir(), fileName);
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[4096];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.close();
            inputStream.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            Toast.makeText(this, "Unable to load image", Toast.LENGTH_SHORT).show();
            return null;
        }
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
                        }
                    } catch (Exception ignored) {
                    } finally {
                        loadingOverlay.setVisibility(View.GONE);
                    }
                },
                error -> {
                    Toast.makeText(this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                    loadingOverlay.setVisibility(View.GONE);
                }
        );

        Volley.newRequestQueue(this).add(getRequest);
    }

    private void saveChanges() {
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String newUsername = usernameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String contact = contactInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();
        String oldUsername = USER_ID;

        if (!password.isEmpty() && !password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingOverlay.setVisibility(View.VISIBLE);

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("firstname", firstName);
            jsonBody.put("lastname", lastName);
            jsonBody.put("username", newUsername);
            jsonBody.put("email", email);
            jsonBody.put("contact", contact);
            jsonBody.put("password", password.isEmpty() ? actualPassword : password);

            SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
            String usertype = prefs.getString("role", null);
            if (usertype != null) jsonBody.put("usertype", usertype);

            String url = BASE_URL + "update_user/" + STUDENT_ID + "/" + USER_ID;

            boolean usernameChanged = !newUsername.equals(oldUsername);

            JsonObjectRequest putRequest = new JsonObjectRequest(
                    Request.Method.PUT,
                    url,
                    jsonBody,
                    response -> {
                        SharedPreferences.Editor sessionEditor = prefs.edit();
                        sessionEditor.putString("username", newUsername);
                        sessionEditor.apply();

                        // Only now save temp image to ProfilePrefs
                        SharedPreferences.Editor profileEditor = profilePrefs.edit();

                        // If username changed → move old image key to new username key
                        if (usernameChanged) {
                            String oldKey = "profileImagePath_" + oldUsername;
                            String newKey = "profileImagePath_" + newUsername;

                            // If user already had a saved image before editing username
                            String oldImagePath = profilePrefs.getString(oldKey, null);
                            if (oldImagePath != null) {
                                profileEditor.putString(newKey, oldImagePath);
                                profileEditor.remove(oldKey);
                            }
                        }

                        // If user uploaded a new image during editing
                        if (tempProfileImagePath != null) {
                            profileEditor.putString("profileImagePath_" + newUsername, tempProfileImagePath);
                        }

                        profileEditor.apply();


                        Toast.makeText(this, "Changes saved!", Toast.LENGTH_SHORT).show();
                        loadingOverlay.setVisibility(View.GONE);
                        finish();
                    },
                    error -> {
                        Toast.makeText(this, "Failed to update profile", Toast.LENGTH_LONG).show();
                        loadingOverlay.setVisibility(View.GONE);
                    }
            );

            Volley.newRequestQueue(this).add(putRequest);

        } catch (Exception e) {
            loadingOverlay.setVisibility(View.GONE);
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private void setupPasswordToggle(EditText inputField, ImageView toggleIcon) {
        toggleIcon.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    inputField.setInputType(
                            android.text.InputType.TYPE_CLASS_TEXT |
                                    android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    );
                    toggleIcon.setImageResource(R.drawable.ic_visibility);
                    inputField.setSelection(inputField.length());
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    inputField.setInputType(
                            android.text.InputType.TYPE_CLASS_TEXT |
                                    android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                    );
                    toggleIcon.setImageResource(R.drawable.ic_visibility_off);
                    inputField.setSelection(inputField.length());
                    return true;
            }
            return false;
        });
    }
}
