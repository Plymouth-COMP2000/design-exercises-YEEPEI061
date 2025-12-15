package com.example.softwareengineering;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
public class GuestMenuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private final Context context;
    private final List<Object> items;

    public GuestMenuAdapter(Context context, List<Object> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getItemViewType(int position) {
        return (items.get(position) instanceof String) ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_menu_guest_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_menu_guest, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_HEADER) {
            ((HeaderViewHolder) holder).header.setText((String) items.get(position));
        } else {
            MenuItemModel item = (MenuItemModel) items.get(position);
            ItemViewHolder itemHolder = (ItemViewHolder) holder;

            itemHolder.name.setText(item.getName());
            itemHolder.description.setText(item.getDescription());
            @SuppressLint("DefaultLocale") String priceText = (item.getPrice() % 1 == 0)
                    ? String.format("%d", (int) item.getPrice())
                    : String.format("%.2f", item.getPrice());
            itemHolder.price.setText("RM " + priceText);

            if (item.getImageUri() != null && !item.getImageUri().isEmpty()) {
                itemHolder.image.setImageURI(android.net.Uri.parse(item.getImageUri()));
            } else {
                itemHolder.image.setImageResource(R.drawable.ic_image);
            }

            boolean isLastInCategory = true;

            for (int i = position + 1; i < items.size(); i++) {
                if (items.get(i) instanceof MenuItemModel) {
                    MenuItemModel nextItem = (MenuItemModel) items.get(i);
                    if (nextItem.getType().equals(item.getType())) {
                        isLastInCategory = false;
                    }
                    break;
                }
            }

            // Hide divider if this is the last item in the list
            if (position == items.size() - 1) {
                isLastInCategory = false;
            }

            itemHolder.divider.setVisibility(isLastInCategory ? View.VISIBLE : View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView header;
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            header = itemView.findViewById(R.id.headerText);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView name, description, price;
        ImageView image;
        View divider;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.itemName);
            description = itemView.findViewById(R.id.itemDescription);
            price = itemView.findViewById(R.id.itemPrice);
            image = itemView.findViewById(R.id.itemImage);
            divider = itemView.findViewById(R.id.divider);
        }
    }

}
