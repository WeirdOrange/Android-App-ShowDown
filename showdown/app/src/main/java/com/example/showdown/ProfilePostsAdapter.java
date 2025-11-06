package com.example.showdown;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProfilePostsAdapter extends RecyclerView.Adapter<ProfilePostsAdapter.EventViewHolder> {
    private List<EventWithDetails> events;
    private OnEventClickListener listener;
    private SimpleDateFormat mDateFormat;

    public interface OnEventClickListener {
        void onBookClick(EventWithDetails event);
    }

    public ProfilePostsAdapter(OnEventClickListener listener) {
        this.events = new ArrayList<>();
        this.listener = listener;
        this.mDateFormat = new SimpleDateFormat("dd MM yyyy", Locale.getDefault());
    }

    public void setEvents(List<EventWithDetails> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_card, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvEventStartDate, tvEventEndDate, tvLocation, tvAvailableTickets;
        Button btnBook;
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_event_title);
            tvEventStartDate = itemView.findViewById(R.id.post_start_date);
            tvEventEndDate = itemView.findViewById(R.id.post_end_date);

        }
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventWithDetails eventDetails = events.get(position);
        DBEvent event = eventDetails.event;

        holder.tvTitle.setText(event.title);
        holder.tvDescription.setText(event.description);
        holder.tvLocation.setText(event.location);


        int available = eventDetails.availableTickets;
        if (available > 0) {
            holder.tvAvailableTickets.setText(available + "tickets available");
            holder.tvAvailableTickets.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_green_dark)
            );
        } else {
            holder.tvAvailableTickets.setText("Sold out");
            holder.tvAvailableTickets.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_red_dark)
            );
            holder.btnBook.setEnabled(false);
        }

        holder.btnBook.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookClick(eventDetails);
            }
        });
    }
}