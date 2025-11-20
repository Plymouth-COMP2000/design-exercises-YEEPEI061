package com.example.softwareengineering;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StaffReservationAdapter extends RecyclerView.Adapter<StaffReservationAdapter.ViewHolder> {

    private Context context;
    private List<ReservationModel> list;
    private OnStaffReservationListener listener;

    public interface OnStaffReservationListener {
        void onCancel(ReservationModel reservation);
    }

    public StaffReservationAdapter(Context context, List<ReservationModel> list, OnStaffReservationListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_reservation_staff, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReservationModel r = list.get(position);

        holder.customerName.setText(r.getCustomerName() + " - " + r.getGuestCount() + " Guests");
        holder.timeTableText.setText(r.getTime() + " • " + r.getTable());
        holder.dateText.setText(r.getDate());

        // Show divider for all except last item
        holder.divider.setVisibility(position == list.size() - 1 ? View.GONE : View.VISIBLE);

        String status = calculateStatus(r.getDate(), r.getTime());

        if ("Past".equals(status)) {
            holder.cancelButton.setImageResource(R.drawable.ic_check_circle);
            holder.cancelButton.setColorFilter(ContextCompat.getColor(context, R.color.green));
            holder.cancelButton.setEnabled(false);
        } else {
            holder.cancelButton.setImageResource(R.drawable.ic_cancel_circle);
            holder.cancelButton.setColorFilter(ContextCompat.getColor(context, R.color.my_primary));
            holder.cancelButton.setEnabled(true);
            holder.cancelButton.setOnClickListener(v -> {
                if (listener != null) listener.onCancel(r);
            });
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView customerImage;
        TextView customerName, timeTableText, dateText;
        ImageButton cancelButton;
        View divider;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            customerImage = itemView.findViewById(R.id.customerImage);
            customerName = itemView.findViewById(R.id.customerName);
            timeTableText = itemView.findViewById(R.id.timeTableText);
            dateText = itemView.findViewById(R.id.dateText);
            cancelButton = itemView.findViewById(R.id.cancelButton);
            divider = itemView.findViewById(R.id.divider);
        }
    }

    public void updateList(List<ReservationModel> newList) {
        Collections.sort(newList, (r1, r2) -> {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy h:mm a", Locale.ENGLISH);
                int currentYear = Calendar.getInstance().get(Calendar.YEAR);

                Date date1 = sdf.parse(r1.getDate() + " " + currentYear + " " + r1.getTime());
                Date date2 = sdf.parse(r2.getDate() + " " + currentYear + " " + r2.getTime());

                Date now = new Date();

                boolean r1Past = date1.before(now);
                boolean r2Past = date2.before(now);

                if (r1Past && !r2Past) return 1;
                if (!r1Past && r2Past) return -1; // upcoming goes before past

                // If both are same status, sort ascending
                return date1.compareTo(date2);

            } catch (Exception e) {
                return 0;
            }
        });

        this.list = newList;
        notifyDataSetChanged();
    }



    private String calculateStatus(String date, String time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy h:mm a", Locale.ENGLISH);

            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            String fullString = date + " " + currentYear + " " + time;

            Date reservationDate = sdf.parse(fullString);
            Date now = new Date();

            return reservationDate.before(now) ? "Past" : "Upcoming";

        } catch (Exception e) {
            return "Upcoming";
        }
    }

}
