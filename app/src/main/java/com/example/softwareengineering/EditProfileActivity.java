package com.example.softwareengineering;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
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

    private String userId, username;
    private String actualPassword;

    private EditText passwordInput, confirmPasswordInput, firstNameInput, lastNameInput,
            usernameInput, emailInput, contactInput;
    private String actualFirstName, actualLastName, actualUsername, actualEmail, actualContact;

    private FrameLayout loadingOverlay;
    private ImageView profileImage;

    private ActivityResultLauncher<Intent> galleryLauncher;
    private String tempProfileImagePath = null;
    private UserSignupDatabaseHelper dbHelper;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        profileImage = findViewById(R.id.profileImage);
        ImageButton changePhotoButton = findViewById(R.id.changePhotoButton);

        SharedPreferences sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE);
        dbHelper = new UserSignupDatabaseHelper(this);

        userId = sharedPref.getString("userId", "");
        username = sharedPref.getString("username", "");
        if (userId.isEmpty()) {
            Toast.makeText(this, "No logged-in user found", Toast.LENGTH_SHORT).show();
            finish();
        }

        String imagePath = dbHelper.getProfileImagePath(userId);

        if (imagePath != null) {
            setProfileImageFromFile(imagePath);
        } else {
            String gender = dbHelper.getProfileGender(userId);
            profileImage.setImageResource(
                    "boy".equals(gender)
                            ? R.drawable.sample_profile_boy
                            : R.drawable.sample_profile
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
        saveButton.setOnClickListener(v -> {
            PopupHelper.showPopup(
                    this,
                    R.drawable.ic_info,
                    getResources().getColor(R.color.my_primary, null),
                    "Save Changes",
                    "Are you sure you want to save changes?",
                    this::saveChanges
            );
        });

    }

    private void setupGalleryLauncher() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {


                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                        Uri imageUri = result.getData().getData();

                        if (imageUri == null) {
                            Toast.makeText(this, "Image URI is null", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        loadingOverlay.setVisibility(View.VISIBLE);

                        InputStream inputStream;
                        try {
                            inputStream = getContentResolver().openInputStream(imageUri);
                        } catch (Exception e) {
                            Toast.makeText(this, "Error getting selected image", Toast.LENGTH_SHORT).show();
                            loadingOverlay.setVisibility(View.GONE);
                            return;
                        }

                        if (inputStream == null) {
                            Toast.makeText(this, "Failed to read image", Toast.LENGTH_SHORT).show();
                            loadingOverlay.setVisibility(View.GONE);
                            return;
                        }

                        InputStream finalStream = inputStream;

                        new Thread(() -> {
                            tempProfileImagePath =
                                    saveImageToInternalStorage(finalStream, "temp_" + userId);


                            runOnUiThread(() -> {
                                if (tempProfileImagePath != null) {
                                    setProfileImageFromFile(tempProfileImagePath);
                                } else {
                                    Toast.makeText(this, "Unable to load image", Toast.LENGTH_SHORT).show();
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
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        } else {
            intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
        }
        galleryLauncher.launch(intent);
    }


    private String saveImageToInternalStorage(InputStream inputStream, String fileNamePrefix) {
        try {
            String fileName = fileNamePrefix + ".jpg";
            File file = new File(getFilesDir(), fileName);

            Log.d("PROFILE_DEBUG", "Saving to file = " + file.getAbsolutePath());

            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[4096];
            int len;
            int totalBytes = 0;

            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
                totalBytes += len;
            }


            outputStream.close();
            inputStream.close();

            if (totalBytes == 0) {
                return null;
            }

            return file.getAbsolutePath();

        } catch (Exception e) {
            return null;
        }
    }


    private void fetchUserData() {
        loadingOverlay.setVisibility(View.VISIBLE);
        String url = BASE_URL + "read_user/" + STUDENT_ID + "/" + username;

        JsonObjectRequest getRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        if (response.has("user")) {
                            JSONObject user = response.getJSONObject("user");
                            actualFirstName = user.getString("firstname");
                            actualLastName = user.getString("lastname");
                            actualUsername = user.getString("username");
                            actualEmail = user.getString("email");
                            actualContact = user.getString("contact");
                            actualPassword = user.getString("password");

                            firstNameInput.setText(actualFirstName);
                            lastNameInput.setText(actualLastName);
                            usernameInput.setText(actualUsername);
                            emailInput.setText(actualEmail);
                            contactInput.setText(actualContact);
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

        if (!password.isEmpty()) {
            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        loadingOverlay.setVisibility(View.VISIBLE);

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("firstname", firstName.isEmpty() ? actualFirstName : firstName);
            jsonBody.put("lastname", lastName.isEmpty() ? actualLastName : lastName);
            jsonBody.put("email", email.isEmpty() ? actualEmail : email);
            jsonBody.put("contact", contact.isEmpty() ? actualContact : contact);
            jsonBody.put("password", password.isEmpty() ? actualPassword : password);

            SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
            String usertype = prefs.getString("role", null);
            if (usertype != null) jsonBody.put("usertype", usertype);

            String finalUsername = newUsername.isEmpty() ? actualUsername : newUsername;
            jsonBody.put("username", finalUsername);

            if (finalUsername.equals(actualUsername)) {
                performUpdate(jsonBody, finalUsername);
            } else {
                checkUsernameAndUpdate(finalUsername, jsonBody);
            }

        } catch (Exception e) {
            loadingOverlay.setVisibility(View.GONE);
            Toast.makeText(this, "Unexpected error", Toast.LENGTH_SHORT).show();
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

    private void checkUsernameAndUpdate(String finalUsername, JSONObject jsonBody) {
        String url = BASE_URL + "read_all_users/" + STUDENT_ID;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        boolean usernameExists = false;

                        if (response.has("users")) {
                            for (int i = 0; i < response.getJSONArray("users").length(); i++) {
                                JSONObject user = response.getJSONArray("users").getJSONObject(i);
                                String existingUsername = user.getString("username");
                                String existingUserId = user.getString("_id");

                                // Ignore current user
                                if (existingUsername.equalsIgnoreCase(finalUsername)
                                        && !existingUserId.equals(userId)) {
                                    usernameExists = true;
                                    break;
                                }
                            }
                        }

                        if (usernameExists) {
                            loadingOverlay.setVisibility(View.GONE);
                            Toast.makeText(
                                    this,
                                    "Username already exists. Please choose another one.",
                                    Toast.LENGTH_LONG
                            ).show();
                        } else {
                            // Username is safe → update user
                            performUpdate(jsonBody, finalUsername);
                        }

                    } catch (Exception e) {
                        loadingOverlay.setVisibility(View.GONE);
                        Toast.makeText(this, "Error validating username", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    loadingOverlay.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to validate username", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void performUpdate(JSONObject jsonBody, String finalUsername) {
        String url = BASE_URL + "update_user/" + STUDENT_ID + "/" + username;

        JsonObjectRequest putRequest = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                jsonBody,
                response -> {
                    SharedPreferences.Editor sessionEditor =
                            getSharedPreferences("UserSession", MODE_PRIVATE).edit();
                    sessionEditor.putString("username", finalUsername);
                    sessionEditor.apply();

                    if (tempProfileImagePath != null) {
                        dbHelper.updateProfileImage(userId, tempProfileImagePath);
                    }

                    Toast.makeText(this, "Changes saved successfully!", Toast.LENGTH_SHORT).show();
                    loadingOverlay.setVisibility(View.GONE);
                    finish();
                },
                error -> {
                    loadingOverlay.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to update profile", Toast.LENGTH_LONG).show();
                }
        );

        Volley.newRequestQueue(this).add(putRequest);
    }


}
