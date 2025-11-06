package com.example.showdown;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActivityMain extends AppCompatActivity {
    private Button btnGetTicket, btnWhatsapp, btnInfo, navigation_bttn;
    private ImageButton profile_bttn;
    private TextView tvEventTitle, tvEventDescription, tvEventLocation, tvEventStartDate, tvEventEndDate, tvEventOrganizer, tvEventContact;
    private RecyclerView rvEventCarousel;
    private EventCarouselAdapter carouselAdapter;

    private AppDatabase db;
    private ExecutorService executorService;
    private SimpleDateFormat dateFormat;
    private int currentEventId = -1;
    private DBEvent currentEvent;
    private DBUser currentOrganizer;
    private List<EventWithDetails> allUpcomingEvents;
    private ActivityNavigation navHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();
        dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

        navHelper = new ActivityNavigation(this);
        navigation_bttn = findViewById(R.id.toggle_nav_btn);
        navigation_bttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navHelper.toggle();
            }
        });

        initializeViews();
        setupCarousel();

        // eventID checker if an ID was passed from CalendarView or ProfileView
        Intent intent = getIntent();
        if (intent.hasExtra("Event_ID")) {
            currentEventId = intent.getIntExtra("Event_ID", -1);
            loadSpecifiedEvent(currentEventId);
        } else {
            loadAllUpcomingEvents();
        }

        btnGetTicket.setOnClickListener(v -> {
            if (currentEvent != null) {
                Toast.makeText(this, "Booking tickets for: " + currentEvent.title,
                        Toast.LENGTH_SHORT).show();
                // TODO: Navigate to ticket booking activity (show mapbox)
            } else {
                Toast.makeText(this, "No event selected",
                        Toast.LENGTH_SHORT).show();
            }
        });

        profile_bttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityMain.this, ActivityProfile.class);
                startActivity(intent);
            }
        });
    }

    private void initializeViews() {
        profile_bttn = findViewById(R.id.profile_bttn);

        tvEventTitle = findViewById(R.id.event_title);
        tvEventDescription = findViewById(R.id.event_description);
        tvEventLocation = findViewById(R.id.event_location);
        tvEventStartDate = findViewById(R.id.event_start_date);;
        tvEventEndDate = findViewById(R.id.event_end_date);
        tvEventOrganizer = findViewById(R.id.event_organizer);
        tvEventContact = findViewById(R.id.contact_phone);

        rvEventCarousel = findViewById(R.id.rv_event_carousel);
        btnGetTicket = findViewById(R.id.btn_get_ticket);
        btnWhatsapp = findViewById(R.id.event_contact);
        btnInfo = findViewById(R.id.event_info);
    }

    private int findCenterItemPosition(LinearLayoutManager layoutManager) {
        int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
        int lastVisiblePosition = layoutManager.findLastVisibleItemPosition();

        if (firstVisiblePosition == RecyclerView.NO_POSITION) {
            return 0;
        }

        return (firstVisiblePosition + lastVisiblePosition) / 2;
    }

    private void displayEventDetails(EventWithDetails eventWithDetails) {
        currentEvent = eventWithDetails.event;
        currentOrganizer = eventWithDetails.user;

        String startDate = dateFormat.format(new Date(currentEvent.startDate));
        String endDate = dateFormat.format(new Date(currentEvent.endDate));

        tvEventTitle.setText(currentEvent.title);
        tvEventDescription.setText(currentEvent.description);
        tvEventLocation.setText(currentEvent.location);
        tvEventStartDate.setText(startDate);
        tvEventEndDate.setText(endDate);

        if (currentOrganizer != null) {
            tvEventOrganizer.setText("By " + currentOrganizer.name);
            tvEventContact.setText(currentOrganizer.phoneNumber);

            btnWhatsapp.setOnClickListener(V -> {
                String message = "Hi, I'm interested in the event: " + currentEvent.title;
                String phoneNumber = currentOrganizer.phoneNumber.replaceAll("[^0-9]", "");

                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(android.net.Uri.parse(
                            "https://api.whatsapp.com/send?phone=" + phoneNumber + "&text=" + message
                    ));
                } catch (Exception e) {
                    Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            tvEventOrganizer.setText("By Unknown");
            tvEventContact.setText("N/A");
        }
        // Display ticket availability
        String ticketInfo = eventWithDetails.availableTickets + " tickets available";
        btnGetTicket.setText(ticketInfo);
    }

    public void  loadAllUpcomingEvents() {
        executorService.execute(() -> {
            try {
                long todayTime = System.currentTimeMillis();
                List<DBEvent> upcomingEvents = db.eventsDao().getUpcomingEvents(todayTime);

                if (!upcomingEvents.isEmpty()) {
                    java.util.List<EventWithDetails> eventDetailsList = new java.util.ArrayList<>();

                    for (DBEvent event: upcomingEvents) {
                        EventWithDetails details = new EventWithDetails();
                        details.event = event;

                        List<DBUser> users = db.userDao().getAllBlocking();
                        for (DBUser user: users) {
                            if (user.id == event.userId) {
                                details.user = user;
                                break;
                            }
                        }

                        Integer totalTickets = db.eventTicketDao().getTotalAvailableTickets(event.id);
                        int bookedTickets = db.bookedTicketDao().getBookedCountByEvent(event.id);

                        if (totalTickets != null) {
                            details.availableTickets = totalTickets - bookedTickets;
                        } else {
                            details.availableTickets = 0;
                        }

                        eventDetailsList.add(details);
                    }

                    allUpcomingEvents = eventDetailsList;
                    runOnUiThread(() ->{
                        carouselAdapter.setEvents(eventDetailsList);
                        carouselAdapter.setActivePosition(0);

                        if (!eventDetailsList.isEmpty()) {
                            displayEventDetails(eventDetailsList.get(0));
                        }
                    });
                } else {
                    runOnUiThread(() -> {
                        tvEventTitle.setText("No Events Available");
                        tvEventDescription.setText("Check back later for upcoming events!");
                        tvEventLocation.setText("N/A");
                        tvEventStartDate.setText("N/A");
                        tvEventEndDate.setText("N/A");
                        tvEventOrganizer.setText("N/A");
                        tvEventContact.setText("N/A");
                        Toast.makeText(this, "No events found. Create one in your profile!",
                                Toast.LENGTH_LONG).show();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Error loading events: " + e.getMessage()
                                ,Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void loadSpecifiedEvent(int eventId) {
        executorService.execute(() -> {
            try {
                List<DBEvent> events = db.eventsDao().getAllBlocking();
                DBEvent event = null;

                for (DBEvent e: events) {
                    if (e.id == eventId) {
                        event = e;
                        break;
                    }
                }

                if (event != null) {
                    loadAllUpcomingEvents();
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Event not found",
                                Toast.LENGTH_SHORT).show();
                        loadAllUpcomingEvents();
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

    private void setupCarousel() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false);
        rvEventCarousel.setLayoutManager(layoutManager);

        carouselAdapter = new EventCarouselAdapter(eventWithDetails -> {
            displayEventDetails(eventWithDetails);
        });

        rvEventCarousel.setAdapter(carouselAdapter);

        // Snap to center
        rvEventCarousel.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView,newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int centerPosition = findCenterItemPosition(layoutManager);
                    if (centerPosition >= 0 && centerPosition < allUpcomingEvents.size()) {
                        carouselAdapter.setActivePosition(centerPosition);
                        displayEventDetails(allUpcomingEvents.get(centerPosition));
                    }
                }
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