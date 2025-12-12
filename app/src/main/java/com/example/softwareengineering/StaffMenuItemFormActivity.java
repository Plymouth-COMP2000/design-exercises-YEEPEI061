package com.example.softwareengineering;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class StaffMenuItemFormActivity extends AppCompatActivity {

    private EditText itemNameInput, priceInput, descriptionInput;
    private Button saveButton;
    private ImageView uploadedImage;

    private Uri selectedImageUri;
    private int itemId = -1;
    private TextView uploadHint;
    private TextView uploadFormat;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_menu_item_form);

        // UI elements
        itemNameInput = findViewById(R.id.itemNameInput);
        priceInput = findViewById(R.id.priceInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        Spinner typeSpinner = findViewById(R.id.categorySpinner);
        saveButton = findViewById(R.id.saveButton);
        uploadedImage = findViewById(R.id.uploadedImage);
        TextView titleText = findViewById(R.id.titleText);
        uploadHint = findViewById(R.id.uploadHint);
        uploadFormat = findViewById(R.id.uploadFormat);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
        findViewById(R.id.cancelButton).setOnClickListener(v -> finish());

        Intent intent = getIntent();
        String mode = intent.getStringExtra("mode");
        itemId = intent.getIntExtra("id", -1);
        String category = intent.getStringExtra("category");

        String[] foodTypes = {"Appetizer", "Main", "Dessert"};
        String[] drinkTypes = {"Coffee", "Tea", "Cocktail"};
        String[] typesToUse = "food".equalsIgnoreCase(category) ? foodTypes : drinkTypes;

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, typesToUse);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);

        if ("edit".equalsIgnoreCase(mode)) {
            titleText.setText("Edit Menu Item");
            saveButton.setText("Save Changes");

            itemNameInput.setText(intent.getStringExtra("name"));
            priceInput.setText(intent.getStringExtra("price"));
            descriptionInput.setText(intent.getStringExtra("description"));

            String itemType = intent.getStringExtra("type");
            if (itemType != null) {
                for (int i = 0; i < typesToUse.length; i++) {
                    if (typesToUse[i].equalsIgnoreCase(itemType)) {
                        typeSpinner.setSelection(i);
                        break;
                    }
                }
            }

            String imageUriString = intent.getStringExtra("imageUri");
            if (imageUriString != null && !imageUriString.isEmpty()) {
                selectedImageUri = Uri.parse(imageUriString);
                uploadedImage.setImageURI(selectedImageUri);

                uploadHint.setVisibility(View.GONE);
                uploadFormat.setVisibility(View.GONE);
            } else {
                uploadedImage.setImageResource(R.drawable.ic_image);
            }


            saveButton.setOnClickListener(v -> {

                String name = itemNameInput.getText().toString().trim();
                String priceStr = priceInput.getText().toString().trim();
                String description = descriptionInput.getText().toString().trim();

                if (name.isEmpty() || priceStr.isEmpty() || description.isEmpty()) {
                    Toast.makeText(this, "Please fill all required fields.", Toast.LENGTH_SHORT).show();
                    return; // stop here, do not show popup
                }

                PopupHelper.showPopup(
                        this,
                        R.drawable.ic_info,
                        getResources().getColor(R.color.my_primary, null),
                        "Save Changes",
                        "Are you sure you want to save changes?",
                        () -> saveMenuItem(true, category, typeSpinner)
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
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    checkFields();
                }
            };

            itemNameInput.addTextChangedListener(watcher);
            priceInput.addTextChangedListener(watcher);
            descriptionInput.addTextChangedListener(watcher);


            saveButton.setOnClickListener(v -> PopupHelper.showPopup(
                    this,
                    R.drawable.ic_info,
                    getResources().getColor(R.color.my_primary, null),
                    "Add Item",
                    "Confirm adding this new item?",
                    () -> saveMenuItem(false, category, typeSpinner)
            ));
        }

        uploadedImage.setOnClickListener(v -> openGallery());
    }

    private void checkFields() {
        boolean allFilled = !itemNameInput.getText().toString().trim().isEmpty()
                && !priceInput.getText().toString().trim().isEmpty()
                && !descriptionInput.getText().toString().trim().isEmpty();
        saveButton.setEnabled(allFilled);
        saveButton.setBackgroundTintList(ContextCompat.getColorStateList(this,
                allFilled ? R.color.my_primary : R.color.my_secondary));
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            selectedImageUri = result.getData().getData();
                            uploadedImage.setImageURI(selectedImageUri);

                            uploadHint.setVisibility(View.GONE);
                            uploadFormat.setVisibility(View.GONE);
                        }
                    }
            );

    private void saveMenuItem(boolean isEdit, String category, Spinner typeSpinner) {
        String name = itemNameInput.getText().toString().trim();
        String type = typeSpinner.getSelectedItem().toString();
        String description = descriptionInput.getText().toString().trim();
        double price = Double.parseDouble(priceInput.getText().toString().trim());
        String imageUriString = selectedImageUri != null ? selectedImageUri.toString() : "";

        MenuItemModel item;
        MenuDatabaseHelper dbHelper = new MenuDatabaseHelper(this);

        if (isEdit) {
            item = new MenuItemModel(itemId, name, category, type, price, imageUriString, description);

            Log.d("DEBUG", "Updating item with ID = " + item.getId());

            dbHelper.updateMenuItem(item);
            Toast.makeText(this, "Changes saved successfully!", Toast.LENGTH_SHORT).show();
        } else {
            item = new MenuItemModel(name, category, type, price, imageUriString, description);
            boolean success = dbHelper.addMenuItem(item);
            Toast.makeText(this, success ? "Item added successfully!" : "Failed to add item.", Toast.LENGTH_SHORT).show();
        }

        finish();
    }
}
