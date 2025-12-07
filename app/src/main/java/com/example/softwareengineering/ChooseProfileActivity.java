package com.example.softwareengineering;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ChooseProfileActivity extends AppCompatActivity {

    private CardView boyCard, girlCard;
    private ImageView tickBoy, tickGirl, previewUploadedImage;
    private LinearLayout profileSelector, uploadedImageContainer;

    private Button uploadImageButton, confirmButton, skipButton;

    private Uri uploadedImageUri = null;
    private String selectedGender = null;
    private FrameLayout loadingOverlay;

    ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_profile);

        boyCard = findViewById(R.id.boyCard);
        girlCard = findViewById(R.id.girlCard);
        tickBoy = findViewById(R.id.tickBoy);
        tickGirl = findViewById(R.id.tickGirl);

        profileSelector = findViewById(R.id.profileSelector);
        uploadedImageContainer = findViewById(R.id.uploadedImageContainer);
        previewUploadedImage = findViewById(R.id.previewUploadedImage);

        uploadImageButton = findViewById(R.id.uploadImageButton);
        confirmButton = findViewById(R.id.confirmButton);
        skipButton = findViewById(R.id.skipButton);
        loadingOverlay = findViewById(R.id.loadingOverlay);

        uploadedImageContainer.setVisibility(View.GONE);

        boyCard.setOnClickListener(v -> selectGender("boy"));
        girlCard.setOnClickListener(v -> selectGender("girl"));


        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                        Uri imageUri = result.getData().getData();

                        loadingOverlay.setVisibility(View.VISIBLE);

                        new Thread(() -> {
                            Bitmap processedBitmap = null;
                            try {
                                Bitmap original = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                                processedBitmap = Bitmap.createScaledBitmap(original, 600, 600, true);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            Bitmap finalBitmap = processedBitmap;

                            runOnUiThread(() -> {
                                if (finalBitmap != null) {
                                    previewUploadedImage.setImageBitmap(finalBitmap);
                                }

                                uploadedImageUri = imageUri;
                                uploadedImageContainer.setVisibility(View.VISIBLE);
                                profileSelector.setVisibility(View.GONE);

                                selectedGender = null;
                                tickBoy.setVisibility(View.GONE);
                                tickGirl.setVisibility(View.GONE);

                                loadingOverlay.setVisibility(View.GONE);
                            });

                        }).start();
                    }


                }
        );

        uploadImageButton.setOnClickListener(v -> openGallery());
        confirmButton.setOnClickListener(v -> saveAndProceed());

        skipButton.setOnClickListener(v -> {
            Toast.makeText(this, "Please login now.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ChooseProfileActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void selectGender(String gender) {

        uploadedImageUri = null;
        uploadedImageContainer.setVisibility(View.GONE);
        profileSelector.setVisibility(View.VISIBLE);

        selectedGender = gender;

        if (gender.equals("boy")) {
            tickBoy.setVisibility(View.VISIBLE);
            tickGirl.setVisibility(View.GONE);
            boyCard.setCardBackgroundColor(getColor(R.color.my_tertiary));
            girlCard.setCardBackgroundColor(getColor(R.color.white));
        } else {
            tickGirl.setVisibility(View.VISIBLE);
            tickBoy.setVisibility(View.GONE);
            girlCard.setCardBackgroundColor(getColor(R.color.my_tertiary));
            boyCard.setCardBackgroundColor(getColor(R.color.white));
        }
    }

    private void openGallery() {
        Intent intent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        } else {
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        101
                );
                return;
            }
        }

        galleryLauncher.launch(intent);
    }

    private void saveAndProceed() {

        SharedPreferences profilePrefs = getSharedPreferences("ProfilePrefs", MODE_PRIVATE);
        SharedPreferences session = getSharedPreferences("UserSession", MODE_PRIVATE);
        String username = session.getString("username", null);

        if (username == null) {
            Toast.makeText(this, "No user detected.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (uploadedImageUri == null && selectedGender == null) {
            Toast.makeText(this, "Please choose a profile.", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingOverlay.setVisibility(View.VISIBLE);

        new Thread(() -> {
            SharedPreferences.Editor editor = profilePrefs.edit();

            if (uploadedImageUri != null) {
                String filePath = saveImageToInternalStorage(uploadedImageUri, "profile_" + username);
                editor.putString("profileImagePath_" + username, filePath);
            } else {
                editor.putString("profileGender_" + username, selectedGender);
            }

            editor.apply();

            runOnUiThread(() -> {
                loadingOverlay.setVisibility(View.GONE);
                Toast.makeText(ChooseProfileActivity.this, "Profile set! Please login now.", Toast.LENGTH_SHORT).show();

                startActivity(new Intent(ChooseProfileActivity.this, LoginActivity.class));
                finish();
            });
        }).start();
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
            e.printStackTrace();
            return null;
        }
    }
}
