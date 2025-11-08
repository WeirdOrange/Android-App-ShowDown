package com.example.showdown;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActivitySetting extends AppCompatActivity {
    private Button navigation_bttn;

    private AppDatabase db;
    private ExecutorService executorService;
    private int currentUserId = 1;
    private List<EventWithDetails> userEventDetails = new ArrayList<>();
    private ActivityNavigation navHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("SETTING", "Setting Page loading");
        setContentView(R.layout.activity_setting);
        Log.d("SETTING", "Setting Page Complete");

        db = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        navHelper = new ActivityNavigation(this);
        navigation_bttn = findViewById(R.id.toggle_nav_btn);
        navigation_bttn.setOnClickListener(v -> navHelper.toggle());
    }


}
