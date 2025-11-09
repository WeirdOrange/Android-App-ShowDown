package CalendarView;

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

import com.example.showdown.DBEvent;
import com.example.showdown.EventWithDetails;
import com.example.showdown.R;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EventRecyclerView extends RecyclerView.Adapter<EventRecyclerView.EventViewHolder> {
    private List<EventWithDetails> events;
    private OnEventClickListener listener;
    private SimpleDateFormat mDateFormat, mTimeFormat;

    public interface OnEventClickListener {
        void onBookClick(EventWithDetails event);
    }

    public EventRecyclerView(OnEventClickListener listener) {
        this.events = new ArrayList<>();
        this.listener = listener;
        this.mDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        this.mTimeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
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
        TextView tvTitle, tvDescription, tvEventDate, tvEventTime, tvLocation, tvAvailableTickets;
        ImageView tvCardImage;
        Button btnBook;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_event_title);
            tvDescription = itemView.findViewById(R.id.tv_event_description);
            tvEventDate = itemView.findViewById(R.id.tv_event_date);
            tvEventTime = itemView.findViewById(R.id.tv_event_time);
            tvLocation = itemView.findViewById(R.id.tv_event_location);
            tvCardImage = itemView.findViewById(R.id.card_bg_image);
            tvAvailableTickets = itemView.findViewById(R.id.tv_available_tickets);
            btnBook = itemView.findViewById(R.id.btn_book_ticket);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventWithDetails eventDetails = events.get(position);
        DBEvent event = eventDetails.event;
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        holder.tvTitle.setText(event.title);
        holder.tvDescription.setText(event.description);
        holder.tvLocation.setText(event.location);
        holder.tvEventDate.setText(mDateFormat.format(eventDetails.datetime));
        holder.tvEventTime.setText(mTimeFormat.format(eventDetails.datetime));

        int available = eventDetails.availableTickets;
        if (available > 0){
            holder.tvAvailableTickets.setText(available + " tickets available");
            holder.tvAvailableTickets.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_green_dark)
            );
            holder.btnBook.setEnabled(true);
        } else {
            holder.tvAvailableTickets.setText("Sold out");
            holder.tvAvailableTickets.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_red_dark)
            );
            holder.btnBook.setEnabled(false);
        }

        if (event.image != null) {
            Bitmap bmp = BitmapFactory.decodeByteArray(event.image, 0, event.image.length);
            holder.tvCardImage.setImageBitmap(bmp);
        }

        holder.btnBook.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookClick(eventDetails);
            }
        });

    }
}
