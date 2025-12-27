package com.example.softwareengineering;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.PopupMenu;


import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StaffMenuListFragment extends Fragment {

    private String category;
    private MenuDatabaseHelper dbHelper;
    private StaffMenuAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_staff_menu_list, container, false);

        Bundle args = getArguments();
        category = (args != null) ? args.getString("category", "food") : "food";

        dbHelper = new MenuDatabaseHelper(getContext());
        RecyclerView recyclerView = view.findViewById(R.id.menuRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ImageButton filterButton = view.findViewById(R.id.filterButton);
        filterButton.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), filterButton);
            String[] types;
            if ("food".equalsIgnoreCase(category)) {
                types = new String[]{"All", "Appetizer", "Main Course", "Dessert"};
            } else {
                types = new String[]{"All", "Coffee", "Tea", "Cocktail"};
            }

            for (String type : types) {
                popup.getMenu().add(type);
            }

            popup.setOnMenuItemClickListener(menuItem -> {
                String selectedType = Objects.requireNonNull(menuItem.getTitle()).toString();
                filterByType(selectedType);
                return true;
            });

            popup.show();
        });

        // Start with an empty list
        adapter = new StaffMenuAdapter(getContext(), new ArrayList<>(), new StaffMenuAdapter.MenuItemListener() {

            @Override
            public void onEdit(MenuItemModel item) {
                Intent intent = new Intent(getContext(), StaffMenuItemFormActivity.class);
                intent.putExtra("mode", "edit");
                intent.putExtra("id", item.getId());
                intent.putExtra("name", item.getName());
                intent.putExtra("price", String.valueOf(item.getPrice()));
                intent.putExtra("description", item.getDescription());
                intent.putExtra("category", item.getCategory());
                intent.putExtra("type", item.getType());
                intent.putExtra("imageUri", item.getImageUri());
                startActivity(intent);
            }

            @Override
            public void onDelete(MenuItemModel item) {
                PopupHelper.showPopup(
                        requireContext(),
                        R.drawable.ic_warning,
                        getResources().getColor(R.color.my_danger, null),
                        "Delete Item",
                        "Are you sure you want to delete this item?",
                        () -> {
                            dbHelper.deleteMenuItem(item.getId());
                            refreshList();
                            Toast.makeText(requireContext(), "Item deleted successfully!", Toast.LENGTH_SHORT).show();
                        }
                );
            }


        });

        recyclerView.setAdapter(adapter);

        refreshList();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshList();
    }

    public void refreshList() {
        List<MenuItemModel> items = dbHelper.getMenuItemsByCategory(category);
        items.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        adapter.updateList(items);
    }

    public static StaffMenuListFragment newInstance(String category) {
        StaffMenuListFragment fragment = new StaffMenuListFragment();
        Bundle bundle = new Bundle();
        bundle.putString("category", category);
        fragment.setArguments(bundle);
        return fragment;
    }

    private void filterByType(String type) {
        List<MenuItemModel> items = dbHelper.getMenuItemsByCategory(category);

        if (!"All".equalsIgnoreCase(type)) {
            List<MenuItemModel> filtered = new ArrayList<>();
            for (MenuItemModel item : items) {
                if (item.getType().equalsIgnoreCase(type)) {
                    filtered.add(item);
                }
            }
            adapter.updateList(filtered);
        } else {
            adapter.updateList(items);
        }
    }

}
