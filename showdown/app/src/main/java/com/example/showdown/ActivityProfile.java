package com.example.showdown;

import android.content.Intent;
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
    private Button navigation_bttn;
    private FrameLayout profile2;
    private TextView userID, username;
    private ImageView profile1;

    private AppDatabase db;
    private ExecutorService executorService;
    private int currentUserId = 1;
    private List<EventWithDetails> userEventDetails = new ArrayList<>();
    private ActivityNavigation navHelper;

    private final ActivityResultLauncher<Intent> activityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadUserEventsWithDetails();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Log.d("PROFILE", "Profile Page loading");

        db = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        navHelper = new ActivityNavigation(this);
        navigation_bttn = findViewById(R.id.toggle_nav_btn);
        navigation_bttn.setOnClickListener(v -> navHelper.toggle());

        initializeViews();
        setupAddEventCard();
        loadUserEventsWithDetails();
        Log.d("PROFILE", "Profile Page finished loading");
    }

    private void initializeViews() {
        profile2 = findViewById(R.id.profile_pic2);
        profile1 = findViewById(R.id.profile_pic);
        username = findViewById(R.id.username);
        userID = findViewById(R.id.user_id);

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

//    private void setupUserDetails(int userId) {
//        List<DBUser> user = new db.userDao().getUserById()
//    }

    private void setupAddEventCard() {
        cvAddEvent.setOnClickListener(v -> {
            Intent intent = new Intent(this, ActivityAddEvent.class);
            activityResultLauncher.launch(intent);
        });
    }

    private void loadUserEventsWithDetails() {
        executorService.execute(() -> {
            try {
                List<DBEvent> allEvents = db.eventsDao().getAllBlocking();
                List<EventWithDetails> filtered = new ArrayList<>();
                for (DBEvent e : allEvents) {
                    if (e.userId == currentUserId) {
                        EventWithDetails details = new EventWithDetails();
                        details.event = e;

                        // Organizer
                        DBUser organizer = db.userDao().getUserById(currentUserId);
                        details.user = organizer;

                        // Tickets
                        Integer total = db.eventTicketDao().getTotalAvailableTickets(e.id);
                        int booked = db.bookedTicketDao().getBookedCountByEvent(e.id);
                        details.availableTickets = (total != null ? total : 0) - booked;

                        filtered.add(details);
                    }
                }
                userEventDetails = filtered;

                runOnUiThread(this::updateRecyclerView);
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this,
                        "Error loading posts: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void updateRecyclerView() {
        postsAdapter.updateEvents(userEventDetails);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}