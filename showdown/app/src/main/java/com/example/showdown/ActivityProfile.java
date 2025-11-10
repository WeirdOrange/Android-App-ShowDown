package com.example.showdown;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActivityProfile extends AppCompatActivity {
    private CardView cvAddEvent;
    private RecyclerView rvUserPosts;
    private ProfilePostsAdapter postsAdapter;
    private Button navigation_bttn, btnPosts, btnHistory, btnBookings;
    private FrameLayout profile2;
    private TextView userID, username;
    private ImageView profile1;

    private AppDatabase db;
    private ExecutorService executorService;
    private SharedPreferences sharedPreferences;
    private int currentUserId = -1;
    private List<EventWithDetails> userEventDetails = new ArrayList<>();
    private ActivityNavigation navHelper;

    private enum TabType { POSTS, HISTORY, BOOKINGS }
    private TabType currentTab = TabType.POSTS;

    private final ActivityResultLauncher<Intent> activityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadUserData();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Log.d("PROFILE", "Profile Page loading");

        db = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();
        sharedPreferences = getSharedPreferences("ShowdownPrefs", MODE_PRIVATE);

        currentUserId = sharedPreferences.getInt("userId", -1);
        if (currentUserId == -1) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, ActivityLogin.class);
            startActivity(intent);
            finish();
            return;
        }

        navHelper = new ActivityNavigation(this);
        navigation_bttn = findViewById(R.id.toggle_nav_btn);
        navigation_bttn.setOnClickListener(v -> navHelper.toggle());

        initializeViews();
        setupAddEventCard();
        setupTabs();
        loadUserData();
        Log.d("PROFILE", "Profile Page finished loading");
    }

    private void initializeViews() {
        profile2 = findViewById(R.id.profile_pic2);
        profile1 = findViewById(R.id.profile_pic);
        username = findViewById(R.id.username);
        userID = findViewById(R.id.user_id);

        btnPosts = findViewById(R.id.bttnPost);
        btnHistory = findViewById(R.id.bttnHistory);
        btnBookings = findViewById(R.id.bttnBookings);

        cvAddEvent = findViewById(R.id.cv_add_event);
        rvUserPosts = findViewById(R.id.rv_user_posts);

        postsAdapter = new ProfilePostsAdapter(event -> {
            Intent intent = new Intent(this, ActivityMain.class);
            intent.putExtra("Event_ID", event.event.id);
            startActivity(intent);
        });

        rvUserPosts.setLayoutManager(new GridLayoutManager(this, 2));
        rvUserPosts.setAdapter(postsAdapter);
    }

    private void updateTabs() {
        // Reset Tabs
        btnPosts.setBackgroundColor(ContextCompat.getColor(this,R.color.secondary_light));
        btnHistory.setBackgroundColor(ContextCompat.getColor(this,R.color.secondary_light));
        btnBookings.setBackgroundColor(ContextCompat.getColor(this,R.color.secondary_light));
        btnPosts.setTextColor(ContextCompat.getColor(this,R.color.white));
        btnHistory.setTextColor(ContextCompat.getColor(this,R.color.white));
        btnBookings.setTextColor(ContextCompat.getColor(this,R.color.white));

        switch (currentTab) {
            case POSTS:
                btnPosts.setBackgroundColor(ContextCompat.getColor(this,R.color.secondary_dark));
                cvAddEvent.setVisibility(View.VISIBLE);
                break;
            case HISTORY:
                btnHistory.setBackgroundColor(ContextCompat.getColor(this,R.color.secondary_dark));
                cvAddEvent.setVisibility(View.GONE);
                break;
            case BOOKINGS:
                btnBookings.setBackgroundColor(ContextCompat.getColor(this,R.color.secondary_dark));
                cvAddEvent.setVisibility(View.GONE);
                break;
        }
    }

    private void switchTab(TabType tab) {
        currentTab = tab;
        updateTabs();
        filterAndDisplayEvents();
    }

    private void setupTabs() {
        btnPosts.setOnClickListener(v -> switchTab(TabType.POSTS));
        btnHistory.setOnClickListener(v -> switchTab(TabType.HISTORY));
        btnBookings.setOnClickListener(v -> switchTab(TabType.BOOKINGS));
    }

    private void setupAddEventCard() {
        cvAddEvent.setOnClickListener(v -> {
            Intent intent = new Intent(this, ActivityAddEvent.class);
            activityResultLauncher.launch(intent);
        });
    }

    private void updateRecyclerView(List<EventWithDetails> events) {
        if (events.isEmpty()) {
            String message = "";
            switch (currentTab) {
                case POSTS:
                    message = "No active events. Create one!";
                    break;
                case HISTORY:
                    message = "No past events.";
                    break;
                case BOOKINGS:
                    message = "No booked tickets";
            }
        }
        postsAdapter.updateEvents(events);
    }

    private void loadUserData() {
        executorService.execute(() -> {
            try {
                DBUser user = db.userDao().getUserById(currentUserId);
                if (user != null) {
                    runOnUiThread(() -> {
                        // Set User Name and id
                        username.setText(user.name);
                        userID.setText("ID: " + user.id);
                    });
                }

                List<DBEvent> allEvents = db.eventsDao().getAllBlocking();
                List<EventWithDetails> filtered = new ArrayList<>();
                for (DBEvent e : allEvents) {
                    if (e.userId == currentUserId) {
                        EventWithDetails details = new EventWithDetails();
                        details.event = e;

                        // Organizer
                        details.user = db.userDao().getUserById(currentUserId);

                        // Tickets
                        Integer total = db.eventTicketDao().getTotalAvailableTickets(e.id);
                        int booked = db.bookedTicketDao().getBookedCountByEvent(e.id);
                        details.availableTickets = (total != null ? total : 0) - booked;

                        filtered.add(details);
                    }
                }
                userEventDetails = filtered;

                runOnUiThread(this::filterAndDisplayEvents);
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this,
                        "Error loading posts: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void filterAndDisplayEvents() {
        List<EventWithDetails> filteredEvents = new ArrayList<>();
        long currentTime = System.currentTimeMillis();

        switch (currentTab) {
            case POSTS:
                for (EventWithDetails event: userEventDetails) {
                    if (event.event.endDate >= currentTime) { // Get Events that are available
                        filteredEvents.add(event);
                    }
                }
                break;
            case HISTORY:

                for (EventWithDetails event: userEventDetails) { // Get Events from the past
                    if (event.event.endDate < currentTime) {
                        filteredEvents.add(event);
                    }
                }
                break;
            case BOOKINGS:
                loadBookedEvents();
                return;
        }
        updateRecyclerView(filteredEvents);
    }

    private void loadBookedEvents() {
        executorService.execute(() -> {
            try {
                List<EventWithDetails> bookedEvents = new ArrayList<>();

                List<DBBookedTicket> userBookings = db.bookedTicketDao().getAllBlocking();
                List<DBBookedTicket> myBookings = new ArrayList<>();

                // Get bookings I made
                for (DBBookedTicket booking: userBookings) {
                    if (booking.userId == currentUserId) {
                        myBookings.add(booking);
                    }
                }

                // Get event details for the booked event
                for (DBBookedTicket booking: myBookings) {
                    List<DBEventTickets> allTickets = db.eventTicketDao().getAllBlocking();
                    DBEventTickets ticket = null;
                    for (DBEventTickets t: allTickets) {
                        if (t.id == booking.ticketsId) {
                            ticket = t; // ticket found
                            break;
                        }
                    }

                    if (ticket != null) {
                        List<DBEvent> allEvents = db.eventsDao().getAllBlocking();
                        DBEvent event = null;
                        for (DBEvent e: allEvents) {
                            if (e.id == ticket.eventsID) {
                                event = e;
                                break;
                            }
                        }
                        if (event != null) {
                            boolean exists = false;
                            for (EventWithDetails existing: bookedEvents) {
                                if (existing.event.id == event.id) {
                                    exists = true;
                                    break;
                                }
                            }
                            if (!exists) {
                                EventWithDetails details = new EventWithDetails();
                                details.event = event;
                                details.user = db.userDao().getUserById(event.id);

                                details.availableTickets = -1;

                                bookedEvents.add(details);
                                // todo: check if it will show if available ticket is -1
                            }
                        }
                    }

                }
                runOnUiThread(() -> updateRecyclerView(bookedEvents));
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this,
                        "Error loading bookings: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
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