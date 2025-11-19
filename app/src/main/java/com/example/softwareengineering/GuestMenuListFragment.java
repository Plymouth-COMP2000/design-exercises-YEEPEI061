package com.example.softwareengineering;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GuestMenuListFragment extends Fragment {

    private String category;
    private MenuDatabaseHelper dbHelper;
    private GuestMenuAdapter adapter;
    private RecyclerView recyclerView;

    private List<Object> prepareSectionedMenu(String category) {
        List<MenuItemModel> allItems = dbHelper.getMenuItemsByCategory(category);
        List<Object> sectionedList = new ArrayList<>();

        Map<String, List<MenuItemModel>> grouped = new LinkedHashMap<>();
        for (MenuItemModel item : allItems) {
            if (!grouped.containsKey(item.getType())) {
                grouped.put(item.getType(), new ArrayList<>());
            }
            grouped.get(item.getType()).add(item);
        }

        for (String type : grouped.keySet()) {
            sectionedList.add(type);
            sectionedList.addAll(grouped.get(type));
        }
        return sectionedList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_guest_menu_list, container, false);

        Bundle args = getArguments();
        category = (args != null) ? args.getString("category", "food") : "food";

        dbHelper = new MenuDatabaseHelper(getContext());
        recyclerView = view.findViewById(R.id.menuGuestRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<Object> sectionedMenu = prepareSectionedMenu(category);
        adapter = new GuestMenuAdapter(getContext(), sectionedMenu);
        recyclerView.setAdapter(adapter);

        return view;
    }


    public static GuestMenuListFragment newInstance(String category) {
        GuestMenuListFragment fragment = new GuestMenuListFragment();
        Bundle bundle = new Bundle();
        bundle.putString("category", category);
        fragment.setArguments(bundle);
        return fragment;
    }
}
