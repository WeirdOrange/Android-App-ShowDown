package com.example.showdown;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TicketSlotAdapter extends RecyclerView.Adapter<TicketSlotAdapter.ViewHolder> {
    private List<BookTicketDialog.TicketSlotInfo> tickets;
    private OnTicketClickListener listener;

    public interface OnTicketClickListener {
        void onBookClick(BookTicketDialog.TicketSlotInfo ticket);
    }

    public TicketSlotAdapter(List<BookTicketDialog.TicketSlotInfo> tickets, OnTicketClickListener listener) {
        this.tickets = tickets;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ticket_booking_slot, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookTicketDialog.TicketSlotInfo ticketInfo = tickets.get(position);

        holder.tvDate.setText(ticketInfo.dateDisplay);
        holder.tvTime.setText(ticketInfo.timeDisplay);
        holder.tvAvailable.setText(ticketInfo.availableCount + " tickets available");

        holder.btnBook.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookClick(ticketInfo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tickets.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvTime, tvAvailable;
        Button btnBook;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_slot_date);
            tvTime = itemView.findViewById(R.id.tv_slot_time);
            tvAvailable = itemView.findViewById(R.id.tv_slot_available);
            btnBook = itemView.findViewById(R.id.btn_book_slot);
        }
    }
}