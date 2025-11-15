package com.example.softwareengineering;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StaffMenuActivity extends AppCompatActivity {

    LinearLayout foodTab, drinkTab;
    TextView foodText, drinkText;
    View foodUnderline, drinkUnderline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_menu);

        LayoutBottomNav.setupBottomNav(this, findViewById(android.R.id.content));
        LayoutBottomNav.highlightSelected(this, findViewById(android.R.id.content), R.id.menuSection);

        // Find Views
        foodTab = findViewById(R.id.foodTab);
        drinkTab = findViewById(R.id.drinkTab);
        foodText = findViewById(R.id.foodMenuTab);
        drinkText = findViewById(R.id.drinksMenuTab);
        foodUnderline = findViewById(R.id.foodUnderline);
        drinkUnderline = findViewById(R.id.drinkUnderline);

        loadFragment(new StaffFoodMenuFragment());
        setActiveTab(true);

        foodTab.setOnClickListener(v -> {
            loadFragment(new StaffFoodMenuFragment());
            setActiveTab(true);
        });

        drinkTab.setOnClickListener(v -> {
            loadFragment(new StaffDrinkMenuFragment());
            setActiveTab(false);
        });

        Button bottomButton = findViewById(R.id.bottomButton);

        bottomButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, StaffMenuItemFormActivity.class);
            intent.putExtra("mode", "add");
            startActivity(intent);
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }

    private void setActiveTab(boolean isFood) {
        if (isFood) {
            foodText.setTextColor(getColor(R.color.my_primary));
            foodText.setTypeface(null, android.graphics.Typeface.BOLD);
            drinkText.setTextColor(getColor(R.color.gray));
            drinkText.setTypeface(null, android.graphics.Typeface.NORMAL);

            foodUnderline.setVisibility(View.VISIBLE);
            drinkUnderline.setVisibility(View.GONE);
        } else {
            drinkText.setTextColor(getColor(R.color.my_primary));
            drinkText.setTypeface(null, android.graphics.Typeface.BOLD);
            foodText.setTextColor(getColor(R.color.gray));
            foodText.setTypeface(null, android.graphics.Typeface.NORMAL);

            drinkUnderline.setVisibility(View.VISIBLE);
            foodUnderline.setVisibility(View.GONE);
        }
    }
}
