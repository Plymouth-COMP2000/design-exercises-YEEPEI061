package com.example.softwareengineering;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
public class StaffMenuAdapter extends RecyclerView.Adapter<StaffMenuAdapter.MenuViewHolder> {

    private List<MenuItemModel> menuList;
    private Context context;
    private MenuItemListener listener;

    public interface MenuItemListener {
        void onEdit(MenuItemModel item);
        void onDelete(MenuItemModel item);
    }

    public StaffMenuAdapter(Context context, List<MenuItemModel> menuList, MenuItemListener listener) {
        this.context = context;
        this.menuList = menuList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_menu_staff, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        MenuItemModel item = menuList.get(position);
        holder.itemName.setText(item.getName());
        double price = item.getPrice();
        String priceText = (price % 1 == 0)
                ? String.format("%d", (int) price)
                : String.format("%.2f", price);
        holder.itemPrice.setText("RM " + priceText);
        holder.itemCategory.setText(item.getType());
        String imageUriString = item.getImageUri();
        if (imageUriString != null && !imageUriString.isEmpty()) {
            holder.itemImage.setImageURI(Uri.parse(imageUriString));
        } else {
            holder.itemImage.setImageResource(R.drawable.ic_image);
        }

        if (position == menuList.size() - 1) {
            holder.divider.setVisibility(View.GONE);
        } else {
            holder.divider.setVisibility(View.VISIBLE);
        }

        holder.editButton.setOnClickListener(v -> listener.onEdit(item));
        holder.deleteButton.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    public static class MenuViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        TextView itemName, itemPrice, itemCategory;
        ImageButton editButton, deleteButton;
        View divider;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.itemImage);
            itemName = itemView.findViewById(R.id.itemName);
            itemPrice = itemView.findViewById(R.id.itemPrice);
            itemCategory = itemView.findViewById(R.id.itemCategory);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            divider = itemView.findViewById(R.id.divider);
        }
    }

    public void updateList(List<MenuItemModel> newList) {
        menuList.clear();
        menuList.addAll(newList);
        notifyDataSetChanged();
    }


}
