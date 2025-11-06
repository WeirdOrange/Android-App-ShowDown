package com.example.showdown;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProfilePostsAdapter extends RecyclerView.Adapter<ProfilePostsAdapter.EventViewHolder> {
    private List<EventWithDetails> events;
    private final OnEventClickListener listener;
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

    public void updateEvents(List<EventWithDetails> newEvents) {
        this.events = newEvents;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.profile_post_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvEventStartDate, tvEventEndDate, tvLocation, tvAvailableTickets, tvPostedOn;
        Button btnBook;
        ImageView eventImage;
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_event_title);
            tvEventStartDate = itemView.findViewById(R.id.post_start_date);
            tvEventEndDate = itemView.findViewById(R.id.post_end_date);
            eventImage = itemView.findViewById(R.id.iv_post_image);
            tvPostedOn = itemView.findViewById(R.id.posted_on);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventWithDetails eventDetails = events.get(position);
        DBEvent event = eventDetails.event;
        String startDate = mDateFormat.format(new Date(event.startDate));
        String endDate = mDateFormat.format(new Date(event.endDate));
        String publishedOn = mDateFormat.format(new Date(event.publishedDate));

        holder.tvTitle.setText(event.title);
        holder.tvEventEndDate.setText(startDate);
        holder.tvEventEndDate.setText(endDate);
        holder.tvPostedOn.setText("Posted On: " + publishedOn);

        if (event.image != null) {
            Bitmap bmp = BitmapFactory.decodeByteArray(event.image, 0, event.image.length);
            holder.eventImage.setImageBitmap(bmp);
        }
        holder.itemView.setOnClickListener(v -> listener.onBookClick(eventDetails));
    }
}