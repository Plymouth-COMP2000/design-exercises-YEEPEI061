package com.example.softwareengineering;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GuestReservationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_RESERVATION = 1;

    public interface OnReservationClickListener {
        void onCancelClick(ReservationModel reservation);
        void onEditClick(ReservationModel reservation);
        void onBookAgainClick(ReservationModel reservation);
    }

    private Context context;
    private List<Object> items;
    private OnReservationClickListener listener;

    public GuestReservationAdapter(Context context, List<ReservationModel> reservations, OnReservationClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.items = generateItemsWithHeaders(reservations);
    }

    private List<Object> generateItemsWithHeaders(List<ReservationModel> reservations) {
        List<Object> result = new ArrayList<>();

        Collections.sort(reservations, (r1, r2) -> r1.getStatus().equals(r2.getStatus()) ? 0 :
                r1.getStatus().equals("Upcoming") ? -1 : 1);

        // Upcoming
        List<ReservationModel> upcoming = new ArrayList<>();
        for (ReservationModel r : reservations) if (r.getStatus().equals("Upcoming")) upcoming.add(r);

        result.add("Upcoming");

        if (!upcoming.isEmpty()) {
            result.addAll(upcoming);
        } else {
            result.add(new EmptyReservationItem("Upcoming"));
        }

        // Past
        List<ReservationModel> past = new ArrayList<>();
        for (ReservationModel r : reservations) if (r.getStatus().equals("Past")) past.add(r);

        result.add("Past");

        if (!past.isEmpty()) {
            result.addAll(past);
        } else {
            result.add(new EmptyReservationItem("Past"));
        }

        return result;
    }


    public class EmptyReservationItem {
        private final String type;
        public EmptyReservationItem(String type) { this.type = type; }
        public String getType() { return type; }
    }

    private static final int VIEW_TYPE_EMPTY = 2;

    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);
        if (item instanceof String) return VIEW_TYPE_HEADER;
        if (item instanceof ReservationModel) return VIEW_TYPE_RESERVATION;
        if (item instanceof EmptyReservationItem) return VIEW_TYPE_EMPTY;
        return VIEW_TYPE_RESERVATION;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_reservation_header, parent, false);
            return new HeaderViewHolder(view);
        } else if (viewType == VIEW_TYPE_EMPTY) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_reservation_guest_empty, parent, false);
            return new EmptyViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_reservation_guest, parent, false);
            return new ReservationViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);

        if (getItemViewType(position) == VIEW_TYPE_HEADER) {
            String header = (String) item;
            ((HeaderViewHolder) holder).headerText.setText(header);

            if (header.startsWith("No ")) {
                ((HeaderViewHolder) holder).headerText.setTextColor(
                        context.getResources().getColor(R.color.my_medium)
                );
                ((HeaderViewHolder) holder).headerText.setTextSize(15f);
            } else {
                ((HeaderViewHolder) holder).headerText.setTextColor(
                        context.getResources().getColor(R.color.my_primary)
                );
            }

        } else if (item instanceof ReservationModel) {
            ReservationModel res = (ReservationModel) item;
            ReservationViewHolder vh = (ReservationViewHolder) holder;

            if (res.getStatus().equals("Upcoming")) {
                vh.upcomingContainer.setVisibility(View.VISIBLE);
                vh.pastContainer.setVisibility(View.GONE);

                vh.dateText.setText(res.getDate());
                vh.timeGuestText.setText(res.getTime() + " - " + res.getGuestCount() + " guests");
                vh.tableText.setText(res.getTable());

                vh.cancelReservationButton.setOnClickListener(v -> {
                    if (listener != null) listener.onCancelClick(res);
                });
                vh.editButton.setOnClickListener(v -> {
                    if (listener != null) listener.onEditClick(res);
                });

            } else { // Past
                vh.upcomingContainer.setVisibility(View.GONE);
                vh.pastContainer.setVisibility(View.VISIBLE);

                vh.dateTextPast.setText(res.getDate());
                vh.timeGuestTextPast.setText(res.getTime() + " - " + res.getGuestCount() + " guests");
                vh.tableTextPast.setText(res.getTable());

                vh.bookAgainButtonPast.setOnClickListener(v -> {
                    if (listener != null) listener.onBookAgainClick(res);
                });
            }
        }
    }

    static class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerText;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            headerText = itemView.findViewById(R.id.headerText);
        }
    }

    static class ReservationViewHolder extends RecyclerView.ViewHolder {
        // Upcoming
        LinearLayout upcomingContainer;
        TextView dateText, timeGuestText, tableText;
        Button cancelReservationButton, editButton;

        // Past
        LinearLayout pastContainer;
        TextView dateTextPast, timeGuestTextPast, tableTextPast;
        Button bookAgainButtonPast;

        public ReservationViewHolder(@NonNull View itemView) {
            super(itemView);
            // Upcoming
            upcomingContainer = itemView.findViewById(R.id.upcomingContainer);
            dateText = itemView.findViewById(R.id.dateText);
            timeGuestText = itemView.findViewById(R.id.timeGuestText);
            tableText = itemView.findViewById(R.id.tableText);
            cancelReservationButton = itemView.findViewById(R.id.cancelReservationButton);
            editButton = itemView.findViewById(R.id.editButton);

            // Past
            pastContainer = itemView.findViewById(R.id.pastContainer);
            dateTextPast = itemView.findViewById(R.id.dateTextPast);
            timeGuestTextPast = itemView.findViewById(R.id.timeGuestTextPast);
            tableTextPast = itemView.findViewById(R.id.tableTextPast);
            bookAgainButtonPast = itemView.findViewById(R.id.bookAgainButtonPast);
        }
    }

    public void updateReservations(List<ReservationModel> newList) {
        this.items.clear();
        this.items.addAll(generateItemsWithHeaders(newList));
        notifyDataSetChanged();
    }
}
