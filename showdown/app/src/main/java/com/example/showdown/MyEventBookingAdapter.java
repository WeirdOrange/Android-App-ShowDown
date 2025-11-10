package com.example.showdown;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MyEventBookingAdapter extends RecyclerView.Adapter<MyEventBookingAdapter.BookingViewHolder> {
    private List<ActivityOwnerEventDetail.BookingInfo> bookings;

    public MyEventBookingAdapter() {
        this.bookings = new ArrayList<>();
    }

    public void setBookings(List<ActivityOwnerEventDetail.BookingInfo> bookings) {
        this.bookings = bookings;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        ActivityOwnerEventDetail.BookingInfo booking = bookings.get(position);

        holder.tvUserName.setText(booking.userName);
        holder.tvUserPhone.setText(booking.userPhone);
        holder.tvBookingDate.setText("Booked on: " + booking.bookingDate);
        holder.tvEventDateTime.setText("Event date: " + booking.eventDateTime);
        holder.tvTicketCount.setText(booking.ticketCount + " ticket(s)");

        // WhatsApp button
        holder.btnWhatsApp.setOnClickListener(v -> {
            if (!booking.userPhone.equals("Not provided")) {
                String phoneNumber = booking.userPhone.replaceAll("[^0-9]", "");

                // Add country code if not present
                if (!phoneNumber.startsWith("60")) {
                    phoneNumber = "60" + phoneNumber;
                }

                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(
                            "https://api.whatsapp.com/send?phone=" + phoneNumber
                    ));
                    v.getContext().startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(v.getContext(), "WhatsApp not installed",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(v.getContext(), "Phone number not provided",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Call button
        holder.btnCall.setOnClickListener(v -> {
            if (!booking.userPhone.equals("Not provided")) {
                try {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + booking.userPhone));
                    v.getContext().startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(v.getContext(), "Error opening dialer",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(v.getContext(), "Phone number not provided",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvUserPhone, tvBookingDate, tvEventDateTime, tvTicketCount;
        ImageButton btnWhatsApp, btnCall;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tv_booking_user_name);
            tvUserPhone = itemView.findViewById(R.id.tv_booking_user_phone);
            tvBookingDate = itemView.findViewById(R.id.tv_booking_date);
            tvEventDateTime = itemView.findViewById(R.id.tv_booking_event_date);
            tvTicketCount = itemView.findViewById(R.id.tv_booking_ticket_count);
            btnWhatsApp = itemView.findViewById(R.id.btn_booking_whatsapp);
            btnCall = itemView.findViewById(R.id.btn_booking_call);
        }
    }
}