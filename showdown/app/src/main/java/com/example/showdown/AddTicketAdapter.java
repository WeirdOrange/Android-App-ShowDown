package com.example.showdown;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AddTicketAdapter extends RecyclerView.Adapter<AddTicketAdapter.TicketViewHolder> {
    private List<AddTicket.TicketInfo> tickets;
    private OnTicketDeleteListener deleteListener;

    public interface OnTicketDeleteListener {
        void onTicketDelete(int position);
    }

    public AddTicketAdapter(OnTicketDeleteListener deleteListener) {
        this.tickets = new ArrayList<>();
        this.deleteListener = deleteListener;
    }

    public void setTickets(List<AddTicket.TicketInfo> tickets) {
        this.tickets = tickets;
        notifyDataSetChanged();
    }

    public void addTicket(AddTicket.TicketInfo ticket) {
        tickets.add(ticket);
        notifyItemInserted(tickets.size() - 1);
    }

    public void removeTicket(int position) {
        tickets.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, tickets.size());
    }

    public List<AddTicket.TicketInfo> getTickets() {
        return tickets;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ticket_slot, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        AddTicket.TicketInfo ticket = tickets.get(position);

        holder.tvDate.setText(ticket.displayDate);
        holder.tvTime.setText(ticket.displayTime);
        holder.tvAmount.setText(String.valueOf(ticket.availableTickets));

        holder.itemView.setOnClickListener(v -> {
            if (deleteListener != null) {
                int item = holder.getBindingAdapterPosition();
                if (item != RecyclerView.NO_POSITION) {
                    deleteListener.onTicketDelete(item);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return tickets.size();
    }

    static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvTime, tvAmount;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_ticket_date);
            tvTime = itemView.findViewById(R.id.tv_ticket_time);
            tvAmount = itemView.findViewById(R.id.tv_available_tickets);
        }
    }
}