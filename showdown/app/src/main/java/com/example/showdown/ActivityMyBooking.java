package com.example.showdown;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActivityMyBooking extends AppCompatActivity {
    private ImageButton btnBack;
    private ImageView ivEventImage;
    private TextView tvEventTitle, tvEventDescription, tvEventLocation;
    private TextView tvEventStartDate, tvEventEndDate;
    private TextView tvOrganizerName, tvOrganizerPhone;
    private TextView tvMyTicketDate, tvMyTicketTime, tvMyTicketCount, tvBookingDate;
    private Button btnWhatsApp, btnCall, btnCancel;

    private AppDatabase db;
    private ExecutorService executorService;
    private SimpleDateFormat dateFormat, timeFormat;
    private int eventId = -1;
    private int userId = -1;
    private DBBookedTicket currentBooking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_booking);

        db = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();
        dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        // Get event ID and user ID from intent
        Intent intent = getIntent();
        if (intent.hasExtra("Event_ID")) {
            eventId = intent.getIntExtra("Event_ID", -1);
        }
        if (intent.hasExtra("User_ID")) {
            userId = intent.getIntExtra("User_ID", -1);
        }

        if (eventId == -1 || userId == -1) {
            Toast.makeText(this, "Invalid booking", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        loadBookingDetails();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btn_back_booking);
        ivEventImage = findViewById(R.id.iv_booking_event_image);
        tvEventTitle = findViewById(R.id.tv_booking_event_title);
        tvEventDescription = findViewById(R.id.tv_booking_event_description);
        tvEventLocation = findViewById(R.id.tv_booking_event_location);
        tvEventStartDate = findViewById(R.id.tv_booking_event_start_date);
        tvEventEndDate = findViewById(R.id.tv_booking_event_end_date);
        tvOrganizerName = findViewById(R.id.tv_booking_organizer_name);
        tvOrganizerPhone = findViewById(R.id.tv_booking_organizer_phone);
        tvMyTicketDate = findViewById(R.id.tv_my_ticket_date);
        tvMyTicketTime = findViewById(R.id.tv_my_ticket_time);
        tvMyTicketCount = findViewById(R.id.tv_my_ticket_count);
        tvBookingDate = findViewById(R.id.tv_my_booking_date);
        btnWhatsApp = findViewById(R.id.btn_booking_whatsapp);
        btnCall = findViewById(R.id.btn_booking_call);
        btnCancel = findViewById(R.id.btn_cancel_booking);

        btnBack.setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> showCancelBookingDialog());
    }

    private void showCancelBookingDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Cancel Booking")
                .setMessage("Are you sure you want to cancel this booking? This action cannot be undone.")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> cancelBooking())
                .setNegativeButton("No, Keep Booking", null)
                .show();
    }

    private void cancelBooking() {
        executorService.execute(() -> {
            try {
                // Delete the booking from database
                db.bookedTicketDao().delete(currentBooking);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Booking cancelled successfully", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(this, ActivityProfile.class);
                    startActivity(intent);
                    setResult(RESULT_OK);
                    finish();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Error cancelling booking: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void loadBookingDetails() {
        executorService.execute(() -> {
            try {
                // Get the event
                DBEvent event = null;
                List<DBEvent> events = db.eventsDao().getAllBlocking();
                for (DBEvent e : events) {
                    if (e.id == eventId) {
                        event = e;
                        break;
                    }
                }

                if (event == null) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                    return;
                }

                // Get the organizer
                DBUser organizer = db.userDao().getUserById(event.userId);

                // Get user's booking for this event
                List<DBBookedTicket> allBookings = db.bookedTicketDao().getAllBlocking();
                DBBookedTicket myBooking = null;

                for (DBBookedTicket booking : allBookings) {
                    if (booking.userId == userId) {
                        // Check if this booking is for the current event
                        List<DBEventTickets> tickets = db.eventTicketDao().getAllBlocking();
                        for (DBEventTickets ticket : tickets) {
                            if (ticket.id == booking.ticketsId && ticket.eventsID == eventId) {
                                myBooking = booking;
                                break;
                            }
                        }
                        if (myBooking != null) break;
                    }
                }

                if (myBooking == null) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Booking not found", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                    return;
                }

                currentBooking = myBooking;
                DBEvent finalEvent = event;
                DBUser finalOrganizer = organizer;
                DBBookedTicket finalMyBooking = myBooking;

                runOnUiThread(() -> {
                    // Display event details
                    tvEventTitle.setText(finalEvent.title);
                    tvEventDescription.setText(finalEvent.description);
                    tvEventLocation.setText(finalEvent.location);
                    tvEventStartDate.setText(dateFormat.format(new Date(finalEvent.startDate)));
                    tvEventEndDate.setText(dateFormat.format(new Date(finalEvent.endDate)));

                    if (finalEvent.image != null) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(
                                finalEvent.image, 0, finalEvent.image.length);
                        ivEventImage.setImageBitmap(bitmap);
                    }

                    // Display organizer details
                    if (finalOrganizer != null) {
                        tvOrganizerName.setText(finalOrganizer.name);
                        tvOrganizerPhone.setText(finalOrganizer.phoneNumber != null &&
                                !finalOrganizer.phoneNumber.isEmpty()
                                ? finalOrganizer.phoneNumber : "Not provided");

                        String finalOrganizerPhone = finalOrganizer.phoneNumber;

                        // WhatsApp button
                        btnWhatsApp.setOnClickListener(v -> {
                            if (finalOrganizerPhone != null && !finalOrganizerPhone.isEmpty()) {
                                String phoneNumber = finalOrganizerPhone.replaceAll("[^0-9]", "");
                                if (!phoneNumber.startsWith("60")) {
                                    phoneNumber = "60" + phoneNumber;
                                }

                                try {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    String message = "Hi, I have a booking for " + finalEvent.title;
                                    intent.setData(Uri.parse(
                                            "https://api.whatsapp.com/send?phone=" + phoneNumber +
                                                    "&text=" + Uri.encode(message)
                                    ));
                                    startActivity(intent);
                                } catch (Exception e) {
                                    Toast.makeText(this, "WhatsApp not installed",
                                            Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(this, "Phone number not available",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

                        // Call button
                        btnCall.setOnClickListener(v -> {
                            if (finalOrganizerPhone != null && !finalOrganizerPhone.isEmpty()) {
                                try {
                                    Intent intent = new Intent(Intent.ACTION_DIAL);
                                    intent.setData(Uri.parse("tel:" + finalOrganizerPhone));
                                    startActivity(intent);
                                } catch (Exception e) {
                                    Toast.makeText(this, "Error opening dialer",
                                            Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(this, "Phone number not available",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        tvOrganizerName.setText("Unknown");
                        tvOrganizerPhone.setText("Not available");
                        btnWhatsApp.setEnabled(false);
                        btnCall.setEnabled(false);
                    }

                    // Display my ticket details
                    tvMyTicketDate.setText(dateFormat.format(new Date(finalMyBooking.ticketDateTime)));
                    tvMyTicketTime.setText(timeFormat.format(new Date(finalMyBooking.ticketDateTime)));
                    tvMyTicketCount.setText(String.valueOf(finalMyBooking.availableTickets));
                    tvBookingDate.setText(dateFormat.format(new Date(finalMyBooking.dateCreated)));
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Error loading booking: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}