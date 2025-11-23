package com.example.showdown;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.showdown.DBUser;
import com.example.showdown.DBEvent;
import com.example.showdown.DBEventTickets;
import com.example.showdown.DBBookedTicket;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import CalendarView.CalendarView;
import CalendarView.EventRecyclerView;

public class ActivityCalendar extends AppCompatActivity {
    private CalendarView calendarView;
    private TextView tvSelectedDate, tvEventsTitleDay, tvEventsTitleMonth;
    private LocalDate today;
    private TextView tvNoEvents;
    private RecyclerView rvEvents;
    private EventRecyclerView eventAdapter;
    private BottomSheetBehavior<View> bottomSheetBehavior;

    private AppDatabase db;
    private ExecutorService executorService;
    private SimpleDateFormat displayDateFormat;
    private DateTimeFormatter calendarDateFormat;
    private ActivityNavigation navHelper;
    private EventRecyclerView mEventRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        db = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();
        displayDateFormat = new SimpleDateFormat("MMMM dd yyyy", Locale.getDefault());
        calendarDateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        navHelper = new ActivityNavigation(this);
        findViewById(R.id.toggle_nav_btn).setOnClickListener(v -> navHelper.toggle());

        initializeViews();
        setupBottomSheet();
        setupRecyclerView();
        setupCalendar();

        findViewById(R.id.profile_bttn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("PROFILE", "Profile button clicked");
                Intent intent = new Intent(ActivityCalendar.this, ActivityProfile.class);
                startActivity(intent);
            }
        });
    }

    private void initializeViews() {
        calendarView = findViewById(R.id.calendar_view);
        tvSelectedDate = findViewById(R.id.tv_selected_date);
        tvEventsTitleDay = findViewById(R.id.tv_events_title_day);
        tvEventsTitleMonth = findViewById(R.id.tv_events_title_month);
        tvNoEvents = findViewById(R.id.tv_no_events);
        rvEvents = findViewById(R.id.rv_events);
    }

    private void setupBottomSheet() {
        View bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        // Configure bottom sheet
        bottomSheetBehavior.setPeekHeight(400);
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // Handle state changes if needed
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // Handle sliding animation if needed
            }
        });
        today = LocalDate.now();
        Date todayDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        String displayDay = displayDateFormat.format(todayDate);
        long timestamp = todayDate.getTime();
        loadEventsFromDatabase(timestamp,displayDay);
    }

    private void setupRecyclerView() {
        eventAdapter = new EventRecyclerView(event -> {
            // Handle book ticket click
            Intent intent = new Intent(ActivityCalendar.this, ActivityMain.class);
            intent.putExtra("Event_ID", event.event.id);
        });

        rvEvents.setLayoutManager(new LinearLayoutManager(this));
        rvEvents.setAdapter(eventAdapter);
    }

    private void setupCalendar() {
        calendarView.setDateSelectedListener(selectedDates -> {
            if (selectedDates != null && !selectedDates.isEmpty()) {
                String selectedDate = selectedDates.get(0);
                loadEventsForDate(selectedDate);
            }
        });
    }

    private void loadEventsForDate(String dateStr) {
        try {
            // Parse the date string (format: dd-MM-yyyy)
            LocalDate localDate = LocalDate.parse(dateStr, calendarDateFormat);

            // Update selected date display
            Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            String displayDate = displayDateFormat.format(date);
            tvSelectedDate.setText(displayDate);

            long timestamp = date.getTime();

            loadEventsFromDatabase(timestamp, displayDate);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading events", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadEventsFromDatabase(long selectedDate, String displayDate) {
        executorService.execute(() -> {
            try {
                // filter date
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(selectedDate);
                calendar.set(Calendar.HOUR_OF_DAY, 12);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                long dayEnd = calendar.getTimeInMillis();

                // Get event ticket for that day
                List<DBEvent> events = db.eventsDao().getEventsByDate(dayEnd);
                List<EventWithDetails> eventDetailsList = new ArrayList<>();
                Log.d("Calendar", "loadEventsFromDatabase: "+events);

                for (DBEvent event : events) {
                    EventWithDetails details = new EventWithDetails();
                    details.event = event;
                    Log.d("Calendar", "loadEventsFromDatabase: "+displayDateFormat.format(details.datetime));

                    // Get user information
                    List<DBUser> users = db.userDao().getAllBlocking();
                    for (DBUser user : users) {
                        if (user.id == event.userId) {
                            details.user = user;
                            break;
                        }
                    }

                    // Calculate available tickets
                    Integer totalTickets = db.eventTicketDao().getTotalAvailableTickets(event.id);
                    int bookedTickets = db.bookedTicketDao().getBookedCountByEvent(event.id);

                    if (totalTickets != null) {
                        details.availableTickets = totalTickets - bookedTickets;
                    } else {
                        details.availableTickets = 0;
                    }

                    eventDetailsList.add(details);
                    Log.d("Event Cards", "booking added");
                }

                // Update UI on main thread
                runOnUiThread(() -> {
                    if (eventDetailsList.isEmpty()) {
                        tvNoEvents.setVisibility(View.VISIBLE);
                        rvEvents.setVisibility(View.GONE);
                        tvEventsTitleDay.setText(displayDate.split(" ")[1]);
                        tvEventsTitleMonth.setText(displayDate.split(" ")[0] + displayDate.split(" ")[2]);
                    } else {
                        tvNoEvents.setVisibility(View.GONE);
                        rvEvents.setVisibility(View.VISIBLE);
                        tvEventsTitleDay.setText(displayDate.split(" ")[1]);
                        tvEventsTitleMonth.setText(displayDate.split(" ")[0] + ", " + displayDate.split(" ")[2]);
                        eventAdapter.setEvents(eventDetailsList);

                        // Expand bottom sheet to show events
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Error loading events: " + e.getMessage(),
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