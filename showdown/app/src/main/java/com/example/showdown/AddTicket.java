package com.example.showdown;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTicket {
    private Context context;
    private OnTicketAddedListener listener;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;
    private long selectedDateTime = 0;

    public static class TicketInfo {
        public long dateTime;
        public int availableTickets;
        public String displayDate;
        public String displayTime;

        public TicketInfo (long dateTime, int availableTickets, String displayDate, String displayTime) {
            this.dateTime = dateTime;
            this.availableTickets = availableTickets;
            this.displayDate = displayDate;
            this.displayTime = displayTime;
        }
    }

    public interface OnTicketAddedListener {
        void onTicketAdded(TicketInfo ticketInfo);
    }

    public AddTicket(Context ctx, OnTicketAddedListener listener) {
        this.context = ctx;
        this.listener = listener;
    }

    private void showDatePicker (Button date, Button time) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                context,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);

                    if (selectedDateTime != 0) {
                        Calendar existingTime = Calendar.getInstance();
                        existingTime.setTimeInMillis(selectedDateTime);
                        selectedDate.set(Calendar.HOUR_OF_DAY, existingTime.get(Calendar.HOUR_OF_DAY));
                        selectedDate.set(Calendar.MINUTE, existingTime.get(Calendar.MINUTE));
                    } else {
                        selectedDate.set(Calendar.HOUR_OF_DAY, 0);
                        selectedDate.set(Calendar.MINUTE, 0);
                    }

                    selectedDateTime = selectedDate.getTimeInMillis();
                    date.setText(dateFormat.format(selectedDate.getTime()));

                    // Update time display if time was already set
                    if (selectedDate.get(Calendar.HOUR_OF_DAY) != 0 || selectedDate.get(Calendar.MINUTE) != 0) {
                        time.setText(timeFormat.format(selectedDate.getTime()));
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void showTimePicker (Button btnTime) {
        if (selectedDateTime == 0) {
            Toast.makeText(context, "Please select date first", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selectedDateTime);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                context,
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);

                    selectedDateTime = calendar.getTimeInMillis();
                    btnTime.setText(timeFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true // 24 hour format instead of 12 hour format
        );
    }

    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.add_ticket_dialog, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        Button btnSelectDate = dialogView.findViewById(R.id.btn_ticket_date);
        Button btnSelectTime = dialogView.findViewById(R.id.btn_ticket_time);
        EditText etTicketAmount = dialogView.findViewById(R.id.et_ticket_amount);
        Button btnAdd = dialogView.findViewById(R.id.btn_add_ticket);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        btnSelectDate.setOnClickListener(v -> showDatePicker(btnSelectDate, btnSelectTime));
        btnSelectTime.setOnClickListener(v -> showTimePicker(btnSelectTime));

        btnAdd.setOnClickListener(v -> {
            String amountStr = etTicketAmount.getText().toString().trim();

            if (selectedDateTime == 0) {
                Toast.makeText(context, "Please Select Date & Time",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (amountStr.isEmpty()) {
                etTicketAmount.setError("Required");
                Toast.makeText(context, "Please enter ticket amount",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int amount = Integer.parseInt(amountStr);
                if (amount < 0) {
                    Toast.makeText(context, "Amount must be 0 or greater",
                            Toast.LENGTH_SHORT).show();
                    return;
                };

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(selectedDateTime);

                String displayDate = dateFormat.format(calendar.getTime());
                String displayTime = timeFormat.format(calendar.getTime());

                TicketInfo ticketInfo = new TicketInfo(selectedDateTime, amount, displayDate, displayTime);

                if (listener != null) {
                    listener.onTicketAdded(ticketInfo);
                }

                dialog.dismiss();
                Toast.makeText(context, "Ticket slot added!",
                        Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                Toast.makeText(context, "Invalid amount", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
