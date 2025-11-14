package com.example.showdown;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActivityMain extends AppCompatActivity {
    private Button btnGetTicket, btnWhatsapp, btnInfo, navigation_bttn, btnCloseSheet;
    private ImageButton profile_bttn;
    private TextView tvEventTitle, tvEventDescription, tvEventLocation, tvEventStartDate, tvEventEndDate, tvEventOrganizer, tvEventContact;
    private RecyclerView rvEventCarousel;
    private EventCarouselAdapter carouselAdapter;
    private BottomSheetBehavior<FrameLayout> bottomSheetBehavior;
    private FrameLayout bottomSheet;

    private AppDatabase db;
    private ExecutorService executorService;
    private SimpleDateFormat dateFormat;
    private SharedPreferences sharedPreferences;
    private int currentUserId = -1;
    private int currentEventId = -1;
    private DBEvent currentEvent;
    private DBUser currentOrganizer;
    private List<EventWithDetails> allUpcomingEvents;
    private ActivityNavigation navHelper;
    private boolean isDataLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();
        dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        sharedPreferences = getSharedPreferences("ShowdownPrefs", MODE_PRIVATE);

        // Get current user ID
        currentUserId = sharedPreferences.getInt("userId", -1);

        navHelper = new ActivityNavigation(this);
        navigation_bttn = findViewById(R.id.toggle_nav_btn);
        navigation_bttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("NAV", "Navigation button clicked");
                navHelper.toggle();
            }
        });

        initializeViews();
        setupBottomSheet();
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
                // Open ticket booking dialog
                BookTicketDialog dialog = new BookTicketDialog(
                        ActivityMain.this,
                        currentEvent,
                        currentUserId,
                        () -> {
                            // Refresh event details after booking
                            if (currentEventId != -1) {
                                loadSpecifiedEvent(currentEventId);
                            } else {
                                loadAllUpcomingEvents();
                            }
                        }
                );
                dialog.show();
            } else {
                Toast.makeText(this, "No event selected",
                        Toast.LENGTH_SHORT).show();
            }
        });

        profile_bttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("PROFILE", "Profile button clicked");
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

        bottomSheet = findViewById(R.id.bottom_sheet);
        btnCloseSheet = findViewById(R.id.btn_close_sheet);
    }

    private void setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setPeekHeight(400);
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        btnCloseSheet.setOnClickListener(v ->
                bottomSheet.setVisibility(View.GONE)
        );

        btnInfo.setOnClickListener(v -> {
            Log.i("Bottomsheet","Btn info clicked");
            if (bottomSheet.getVisibility() == View.GONE) {
                Log.i("Bottomsheet","Btn info opening");
                bottomSheet.setVisibility(View.VISIBLE);
            } else {
                Log.i("Bottomsheet","Btn info closing");
                bottomSheet.setVisibility(View.GONE);
            }
        });
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

            btnWhatsapp.setOnClickListener(v -> {
                String message = "Hi, I'm interested in the event: " + currentEvent.title;
                String phoneNumber = currentOrganizer.phoneNumber.replaceAll("[^0-9]", "");

                try {
                    // Add country code if not present
                    if (!phoneNumber.startsWith("60")) {
                        phoneNumber = "60" + phoneNumber;
                    }

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(
                            "https://api.whatsapp.com/send?phone=" + phoneNumber + "&text=" + Uri.encode(message)
                    ));
                    startActivity(intent); // THIS WAS MISSING!
                } catch (Exception e) {
                    Toast.makeText(this, "WhatsApp not installed or error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
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

    public void loadAllUpcomingEvents() {
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

        carouselAdapter = new EventCarouselAdapter((eventWithDetails) -> {
            displayEventDetails(eventWithDetails);
        });

        rvEventCarousel.setAdapter(carouselAdapter);

        // Snap to center
        rvEventCarousel.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView,newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (allUpcomingEvents == null || allUpcomingEvents.isEmpty()){
                        return;
                    }
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