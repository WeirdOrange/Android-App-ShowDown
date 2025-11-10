package com.example.showdown;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BookTicketDialog {
    private Context context;
    private DBEvent event;
    private int userId;
    private AppDatabase db;
    private ExecutorService executorService;
    private OnTicketBookedListener listener;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;

    public interface OnTicketBookedListener {
        void onTicketBooked();
    }

    public BookTicketDialog(Context context, DBEvent event, int userId, OnTicketBookedListener listener) {
        this.context = context;
        this.event = event;
        this.userId = userId;
        this.listener = listener;
        this.db = AppDatabase.getInstance(context);
        this.executorService = Executors.newSingleThreadExecutor();
        this.dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_book_ticket, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        TextView tvEventTitle = dialogView.findViewById(R.id.tv_dialog_event_title);
        RecyclerView rvTickets = dialogView.findViewById(R.id.rv_available_tickets);
        TextView tvNoTickets = dialogView.findViewById(R.id.tv_no_tickets);
        Button btnClose = dialogView.findViewById(R.id.btn_close_dialog);

        tvEventTitle.setText(event.title);

        rvTickets.setLayoutManager(new LinearLayoutManager(context));

        // Load available tickets
        executorService.execute(() -> {
            try {
                List<DBEventTickets> allTickets = db.eventTicketDao().getTicketsByEventId(event.id);
                List<TicketSlotInfo> ticketSlots = new ArrayList<>();

                for (DBEventTickets ticket : allTickets) {
                    // Calculate available tickets for this slot
                    int bookedCount = db.bookedTicketDao().getBookedCount(ticket.id);
                    int available = ticket.availableTickets - bookedCount;

                    if (available > 0) {
                        TicketSlotInfo slotInfo = new TicketSlotInfo();
                        slotInfo.ticket = ticket;
                        slotInfo.availableCount = available;
                        slotInfo.dateDisplay = dateFormat.format(ticket.ticketDateTime);
                        slotInfo.timeDisplay = timeFormat.format(ticket.ticketDateTime);
                        ticketSlots.add(slotInfo);
                    }
                }

                // Update UI on main thread
                ((android.app.Activity) context).runOnUiThread(() -> {
                    if (ticketSlots.isEmpty()) {
                        rvTickets.setVisibility(View.GONE);
                        tvNoTickets.setVisibility(View.VISIBLE);
                    } else {
                        rvTickets.setVisibility(View.VISIBLE);
                        tvNoTickets.setVisibility(View.GONE);

                        TicketSlotAdapter adapter = new TicketSlotAdapter(ticketSlots, slotInfo -> {
                            bookTicket(slotInfo.ticket, dialog);
                        });
                        rvTickets.setAdapter(adapter);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                ((android.app.Activity) context).runOnUiThread(() ->
                        Toast.makeText(context, "Error loading tickets: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
            }
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void bookTicket(DBEventTickets ticket, AlertDialog dialog) {
        executorService.execute(() -> {
            try {
                // Check if ticket is still available
                int bookedCount = db.bookedTicketDao().getBookedCount(ticket.id);
                int available = ticket.availableTickets - bookedCount;

                if (available <= 0) {
                    ((android.app.Activity) context).runOnUiThread(() ->
                            Toast.makeText(context, "Sorry, this ticket is no longer available",
                                    Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                // Create booking
                DBBookedTicket booking = new DBBookedTicket();
                booking.name = ticket.name;
                booking.ticketDateTime = ticket.ticketDateTime;
                booking.availableTickets = 1; // Booking 1 ticket
                booking.dateCreated = System.currentTimeMillis();
                booking.ticketsId = ticket.id;
                booking.userId = userId;

                db.bookedTicketDao().insert(booking);

                ((android.app.Activity) context).runOnUiThread(() -> {
                    Toast.makeText(context, "Ticket booked successfully!",
                            Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    if (listener != null) {
                        listener.onTicketBooked();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                ((android.app.Activity) context).runOnUiThread(() ->
                        Toast.makeText(context, "Error booking ticket: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    public static class TicketSlotInfo {
        public DBEventTickets ticket;
        public int availableCount;
        public String dateDisplay;
        public String timeDisplay;
    }
}