package com.example.showdown;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class EventCarouselAdapter extends RecyclerView.Adapter<EventCarouselAdapter.CarouselViewHolder>{
    private List<EventWithDetails> events;
    private OnEventClickListener listener;
    private int activePosition = 0;

    public interface OnEventClickListener {
        void onEventClick(EventWithDetails event);
    }

    public EventCarouselAdapter(OnEventClickListener listener) {
        this.events = new ArrayList<>();
        this.listener = listener;
    }

    public void setEvents(List<EventWithDetails> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    public void setActivePosition(int position) {
        int oldPosition = activePosition;
        activePosition = position;
        notifyItemChanged(oldPosition);
        notifyItemChanged(activePosition);
    }

    static class CarouselViewHolder extends RecyclerView.ViewHolder {
        CardView cardview;
        ImageView eventImage;

        public CarouselViewHolder(@NonNull View itemView) {
            super(itemView);
            cardview = (CardView) itemView;
            eventImage = itemView.findViewById(R.id.carousel_image);
        }
    }

    @NonNull
    @Override
    public CarouselViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.main_event_carousel, parent, false);
        return new CarouselViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarouselViewHolder holder, int position){
        EventWithDetails eventDetails = events.get(position);
        DBEvent event = eventDetails.event;

        // Load Image
        if (event.image != null && event.image.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(event.image, 0, event.image.length);
            holder.eventImage.setImageBitmap(bitmap);
        }

        // set isActive to true if position is the same as activePosition
        boolean isActive = position == activePosition;

        if (isActive) {
            holder.eventImage.setColorFilter(null);
            holder.eventImage.setAlpha(1.0f);
            holder.cardview.setCardElevation(12f);
            holder.cardview.setScaleX(1.0f);
            holder.cardview.setScaleY(1.0f);
        } else {
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0.3f);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            holder.eventImage.setColorFilter(filter);
            holder.eventImage.setAlpha(0.5f);
            holder.cardview.setCardElevation(4f);
            holder.cardview.setScaleY(0.75f);
            holder.cardview.setScaleX(0.75f);
        }

        holder.cardview.setOnClickListener(v -> {
            if (listener != null) {
                setActivePosition(position);
                listener.onEventClick(eventDetails);
            }
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }
}
