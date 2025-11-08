package com.example.showdown;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActivityAddEvent extends AppCompatActivity {
    private EditText etTitle, etDescription, etLocation;
    private Button btnSelectStartDate, btnSelectEndDate, btnPublish;
    private ImageButton ivEventImage, btnCancel,cvAddTicket;
    private ArrayList<AddTicket.TicketInfo> ticketSlots;
    private RecyclerView rvTicketList;
    private TextView tvTicketCount;

    private AddTicketAdapter ticketAdapter;

    private AppDatabase db;
    private ExecutorService executorService;
    private SimpleDateFormat dateFormat;

    private long startDateTimestamp = 0;
    private long endDateTimestamp = 0;
    private int currentUserId = 1;
    private byte[] selectedImageBytes = null;

    // Image picker launcher
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        loadImageFromUri(imageUri);
                    }
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_event);

        db = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();
        dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        ticketSlots = new ArrayList<>();

        initializeViews();
        setupTicketRecyclerView();
        setupClickListeners();

    }

    private void initializeViews() {
        etTitle = findViewById(R.id.et_event_title);
        etDescription = findViewById(R.id.et_event_description);
        etLocation  = findViewById(R.id.et_event_location);
        btnSelectStartDate = findViewById(R.id.btn_select_start_date);
        btnSelectEndDate = findViewById(R.id.btn_select_end_date);
        btnPublish = findViewById(R.id.btn_publish_event);
        btnCancel = findViewById(R.id.btn_cancel);
        ivEventImage = findViewById(R.id.cardImage2);
        cvAddTicket = findViewById(R.id.cv_add_ticket);
        rvTicketList = findViewById(R.id.rv_ticket_list);
        tvTicketCount = findViewById(R.id.tv_ticket_count);
    }

    private void setupTicketRecyclerView() {
        ticketAdapter = new AddTicketAdapter(position -> {
            ticketSlots.remove(position);
            ticketAdapter.removeTicket(position);
            updateTicketCount();
            Toast.makeText(this,"Ticket Slot Removed",
                    Toast.LENGTH_SHORT).show();
        });

        rvTicketList.setLayoutManager(new LinearLayoutManager(this));
        rvTicketList.setAdapter(ticketAdapter);
        updateTicketCount();
    }

    private void showAddTicketDialog() {
        new AddTicket(this, ticketInfo -> {
            ticketSlots.add(ticketInfo);
            ticketAdapter.addTicket(ticketInfo);
            updateTicketCount();
        }).show();
    }

    private void updateTicketCount() {
        int totalTickets = 0;
        for (AddTicket.TicketInfo ticket: ticketSlots) {
            totalTickets += ticket.availableTickets;
        }

        if (ticketSlots.isEmpty()) {
            tvTicketCount.setText("No tickets added yet");
            tvTicketCount.setVisibility(View.VISIBLE);
            rvTicketList.setVisibility(View.GONE);
        } else {
            tvTicketCount.setText(ticketSlots.size() + " slot(s), " + totalTickets + " total tickets");
            tvTicketCount.setVisibility(View.VISIBLE);
            rvTicketList.setVisibility(View.VISIBLE);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float ratioBitmap = (float) width / (float) height;
        float ratioMax = (float) maxWidth / (float) maxHeight;

        int finalWidth = maxWidth;
        int finalHeight = maxHeight;

        if (ratioMax > ratioBitmap) {
            finalWidth = (int) ((float) maxHeight * ratioBitmap);
        } else {
            finalHeight = (int) ((float) maxWidth / ratioBitmap);
        }

        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true);
    }

    private void loadImageFromUri(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            Bitmap resizedBitmap = resizeBitmap(bitmap, 800, 800);
            ivEventImage.setImageBitmap(resizedBitmap);

            // Byte Converter
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
            selectedImageBytes = stream.toByteArray();

            Toast.makeText(this, "Image selected!", Toast.LENGTH_SHORT).show();

            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading image: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void showDatePicker (boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);

                    long timestamp = selectedDate.getTimeInMillis();
                    String displayDate = dateFormat.format(selectedDate.getTime());

                    if (isStartDate) {
                        startDateTimestamp = timestamp;
                        btnSelectStartDate.setText("Start: " + displayDate);
                    } else {
                        endDateTimestamp = timestamp;
                        btnSelectEndDate.setText("End: " + displayDate);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void publishEvent() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        // Validation
        if (title.isEmpty()) {
            etTitle.setError("Title is required");
            Toast.makeText(this, "Please enter event title", Toast.LENGTH_SHORT).show();
            return;
        }
        if (description.isEmpty()) {
            etDescription.setError("Description is required");
            Toast.makeText(this, "Please enter event description", Toast.LENGTH_SHORT).show();
            return;
        }
        if (location.isEmpty()) {
            etLocation.setError("Location is required");
            Toast.makeText(this, "Please enter event location", Toast.LENGTH_SHORT).show();
            return;
        }
        if (startDateTimestamp == 0) {
            Toast.makeText(this, "Please select start date", Toast.LENGTH_SHORT).show();
            return;
        }
        if (endDateTimestamp == 0) {
            Toast.makeText(this, "Please select end date", Toast.LENGTH_SHORT).show();
            return;
        }
        if (endDateTimestamp < startDateTimestamp) {
            Toast.makeText(this, "End date must be after start date", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedImageBytes == null) {
            Toast.makeText(this, "Please select an event image", Toast.LENGTH_SHORT).show();
            return;
        }
        if (ticketSlots.isEmpty()) {
            Toast.makeText(this, "Please add at least one ticket slot", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            try {
                DBEvent event = new DBEvent();
                event.title = title;
                event.description = description;
                event.location = location;
                event.startDate = startDateTimestamp;
                event.endDate = endDateTimestamp;
                event.publishedDate = System.currentTimeMillis();
                event.userId = currentUserId;
                event.image = selectedImageBytes;
                event.active = true;

                long eventId = db.eventsDao().insert(event);

                // Create All Ticket Slots
                for (AddTicket.TicketInfo ticketInfo: ticketSlots) {
                    DBEventTickets ticket = new DBEventTickets();
                    ticket.name = event.title;
                    ticket.availableTickets = ticketInfo.availableTickets;
                    ticket.ticketDateTime = ticketInfo.dateTime;
                    ticket.dateCreated = System.currentTimeMillis();
                    ticket.eventsID = (int) eventId;

                    db.eventTicketDao().insert(ticket);
                }

                runOnUiThread(() -> {
                    Toast.makeText(this, "Event Published Successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ActivityAddEvent.this, ActivityProfile.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    setResult(RESULT_OK);
                    finish();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Error publishing event: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void setupClickListeners() {
        btnSelectStartDate.setOnClickListener(v -> showDatePicker(true));
        btnSelectEndDate.setOnClickListener(v -> showDatePicker(false));

        btnPublish.setOnClickListener(v -> publishEvent());
        btnCancel.setOnClickListener(v -> finish());

        ivEventImage.setOnClickListener(v -> openImagePicker());

        cvAddTicket.setOnClickListener(v -> showAddTicketDialog());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
//        if (searchEngine != null) {
//            searchEngine.cancel();
//        }
//        MapboxSearchSdk.terminate();
    }
}