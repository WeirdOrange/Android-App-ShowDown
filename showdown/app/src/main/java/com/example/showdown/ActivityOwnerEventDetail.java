package com.example.showdown;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActivityOwnerEventDetail extends AppCompatActivity {
    private Button btnEditEvent;
    private ImageButton btnBack;
    private ImageView ivEventImage;
    private TextView tvEventTitle, tvEventDescription, tvEventLocation;
    private TextView tvEventStartDate, tvEventEndDate, tvTotalTickets, tvBookedTickets, tvAvailableTickets;
    private RecyclerView rvBookings;
    private TextView tvNoBookings;

    private AppDatabase db;
    private ExecutorService executorService;
    private SimpleDateFormat dateFormat;
    private int eventId = -1;
    private DBEvent currentEvent;
    private MyEventBookingAdapter bookingsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        db = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();
        dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

        // Get event ID from intent
        Intent intent = getIntent();
        if (intent.hasExtra("Event_ID")) {
            eventId = intent.getIntExtra("Event_ID", -1);
        }

        if (eventId == -1) {
            Toast.makeText(this, "Invalid event", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupRecyclerView();
        loadEventDetails();
        loadBookings();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btn_back);
        ivEventImage = findViewById(R.id.iv_event_details_image);
        tvEventTitle = findViewById(R.id.tv_event_details_title);
        tvEventDescription = findViewById(R.id.tv_event_details_description);
        tvEventLocation = findViewById(R.id.tv_event_details_location);
        tvEventStartDate = findViewById(R.id.tv_event_details_start_date);
        tvEventEndDate = findViewById(R.id.tv_event_details_end_date);
        tvTotalTickets = findViewById(R.id.tv_total_tickets);
        tvBookedTickets = findViewById(R.id.tv_booked_tickets);
        tvAvailableTickets = findViewById(R.id.tv_available_tickets);
        rvBookings = findViewById(R.id.rv_bookings);
        tvNoBookings = findViewById(R.id.tv_no_bookings);
        btnEditEvent = findViewById(R.id.btn_edit_event);

        btnBack.setOnClickListener(v -> finish());
        btnEditEvent.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityOwnerEventDetail.this, ActivityEditEvent.class);
            intent.putExtra("Event_ID", eventId);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        bookingsAdapter = new MyEventBookingAdapter();
        rvBookings.setLayoutManager(new LinearLayoutManager(this));
        rvBookings.setAdapter(bookingsAdapter);
    }

    private void loadEventDetails() {
        executorService.execute(() -> {
            try {
                List<DBEvent> events = db.eventsDao().getAllBlocking();
                for (DBEvent event : events) {
                    if (event.id == eventId) {
                        currentEvent = event;
                        break;
                    }
                }

                if (currentEvent != null) {
                    // Calculate ticket statistics
                    Integer totalTickets = db.eventTicketDao().getTotalAvailableTickets(eventId);
                    int bookedCount = db.bookedTicketDao().getBookedCountByEvent(eventId);
                    int availableCount = (totalTickets != null ? totalTickets : 0) - bookedCount;

                    int finalTotalTickets = totalTickets != null ? totalTickets : 0;
                    int finalBookedCount = bookedCount;
                    int finalAvailableCount = availableCount;

                    runOnUiThread(() -> {
                        tvEventTitle.setText(currentEvent.title);
                        tvEventDescription.setText(currentEvent.description);
                        tvEventLocation.setText(currentEvent.location);
                        tvEventStartDate.setText(dateFormat.format(new Date(currentEvent.startDate)));
                        tvEventEndDate.setText(dateFormat.format(new Date(currentEvent.endDate)));

                        tvTotalTickets.setText(String.valueOf(finalTotalTickets));
                        tvBookedTickets.setText(String.valueOf(finalBookedCount));
                        tvAvailableTickets.setText(String.valueOf(finalAvailableCount));

                        if (currentEvent.image != null) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(
                                    currentEvent.image, 0, currentEvent.image.length);
                            ivEventImage.setImageBitmap(bitmap);
                        }
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Error loading event: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void loadBookings() {
        executorService.execute(() -> {
            try {
                List<BookingInfo> bookingInfoList = new ArrayList<>();

                // Get all booked tickets for this event
                List<DBBookedTicket> allBookings = db.bookedTicketDao().getAllBlocking();

                for (DBBookedTicket booking : allBookings) {
                    // Get the ticket to check if it belongs to this event
                    List<DBEventTickets> tickets = db.eventTicketDao().getAllBlocking();
                    for (DBEventTickets ticket : tickets) {
                        if (ticket.id == booking.ticketsId && ticket.eventsID == eventId) {
                            // Get user information
                            DBUser user = db.userDao().getUserById(booking.userId);

                            if (user != null) {
                                BookingInfo info = new BookingInfo();
                                info.userName = user.name;
                                info.userPhone = user.phoneNumber != null && !user.phoneNumber.isEmpty()
                                        ? user.phoneNumber : "Not provided";
                                info.bookingDate = dateFormat.format(new Date(booking.dateCreated));
                                info.eventDateTime = dateFormat.format(new Date(booking.ticketDateTime));
                                info.ticketCount = booking.availableTickets;

                                bookingInfoList.add(info);
                            }
                            break;
                        }
                    }
                }

                runOnUiThread(() -> {
                    if (bookingInfoList.isEmpty()) {
                        rvBookings.setVisibility(View.GONE);
                        tvNoBookings.setVisibility(View.VISIBLE);
                    } else {
                        rvBookings.setVisibility(View.VISIBLE);
                        tvNoBookings.setVisibility(View.GONE);
                        bookingsAdapter.setBookings(bookingInfoList);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Error loading bookings: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    public static class BookingInfo {
        public String userName;
        public String userPhone;
        public String bookingDate;
        public String eventDateTime;
        public int ticketCount;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}