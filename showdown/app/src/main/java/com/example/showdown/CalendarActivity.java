package com.example.showdown;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class CalendarActivity extends AppCompatActivity {
    private Button main_bttn;
    private Button profile_bttn;
    private ImageButton eventButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        ImageButton eventButton = findViewById(R.id.eventButton);
        eventButton.setClipToOutline(true);

        main_bttn = findViewById(R.id.calendar_main_bttn);
        main_bttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CalendarActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        profile_bttn = findViewById(R.id.calendar_profile_bttn);
        profile_bttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CalendarActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });
    }
}