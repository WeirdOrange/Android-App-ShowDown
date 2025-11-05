package com.example.showdown;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
    private Button btnHome;
    private CardView cvAddEvent;
    private RecyclerView rvUserPosts;
    private ProfilePostsAdapter postsAdapter;
    private AppDatabase db;
    private ExecutorService executorService;
    private int currentUserId = 1; // TODO: Replace with actual logged-in user ID
    private List<DBEvent> userEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        initializeViews();
        setupAddEventCard();
        loadUserPosts();

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityProfile.this, ActivityMain.class);
            startActivity(intent);
        });
    }

    private void initializeViews() {
        btnHome = findViewById(R.id.profile_main);
        cvAddEvent = findViewById(R.id.cv_add_event);
        rvUserPosts = findViewById(R.id.rv_user_posts);

//        postsAdapter = new ProfilePostsAdapter(event -> {
//            Intent intent = new Intent(ActivityProfile.this, ActivityMain.class);
//            intent.putExtra("Event_ID", event.eventId);
//            startActivity(intent);
//        });

        rvUserPosts.setLayoutManager(new GridLayoutManager(this, 2));
        rvUserPosts.setAdapter(postsAdapter);
    }

    private final ActivityResultLauncher<Intent> activityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadUserPosts(); //Refresh Posts when AddEvent Returns True
                }
            });


    private void setupAddEventCard() {
        cvAddEvent.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityProfile.this, ActivityAddEvent.class);
            activityResultLauncher.launch(intent);
        });
    }

    private void loadUserPosts() {
        executorService.execute(() -> {
            try {
                List<DBEvent> userEvents = db.eventsDao().getAllBlocking();
                List<DBEvent> filteredEvents = new ArrayList<>();

                for (DBEvent event : userEvents) {
                    if (event.userId == currentUserId) {
                        filteredEvents.add(event);
                    }
                }

                runOnUiThread(() -> {
                    displayUserPosts();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(
                                this,
                                "Error loading posts: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void displayUserPosts() {
        CardView[] cards = {};
        ImageView[] images = {};

        for (int i = 0; i < cards.length; i++) {
            final int index = i;
            if (i < userEvents.size()) {
                DBEvent event = userEvents.get(i);
                cards[i].setVisibility(View.VISIBLE);

                // Load eventImage
                if (event.image != null && event.image.length > 0) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(event.image, 0, event.image.length);
                    images[i].setImageBitmap(bitmap);
                }

                cards[i].setOnClickListener(v -> {
                    Intent intent = new Intent(ActivityProfile.this, ActivityMain.class);
                    intent.putExtra("Event_ID", userEvents.get(index).id);
                    startActivity(intent);
                });
            } else {
                cards[i].setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
