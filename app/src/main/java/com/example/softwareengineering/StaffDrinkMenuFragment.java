package com.example.softwareengineering;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class StaffDrinkMenuFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_staff_drink_menu, container, false);

        ImageView deleteButton = view.findViewById(R.id.deleteButton);

        deleteButton.setOnClickListener(v -> {
            PopupHelper.showPopup(
                    requireContext(),
                    R.drawable.ic_warning,
                    getResources().getColor(R.color.my_danger, null),
                    "Delete Item",
                    "Are you sure you want to delete this item?",
                    this::deleteFoodItem
            );
        });

        ImageButton editButton = view.findViewById(R.id.editButton);
        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), StaffMenuItemFormActivity.class);
            intent.putExtra("mode", "edit");
            startActivity(intent);
        });

        return view;
    }

    private void deleteFoodItem() {
        // your actual delete logic here
        Toast.makeText(requireContext(), "Item deleted successfully!", Toast.LENGTH_SHORT).show();
    }
}
