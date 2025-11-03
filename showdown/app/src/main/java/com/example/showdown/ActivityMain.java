package com.example.showdown;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ActivityMain extends AppCompatActivity {
    private Button calendar_bttn;
    private Button profile_bttn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calendar_bttn = findViewById(R.id.calendar_bttn);
        calendar_bttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityMain.this, ActivityCalendar.class);
                startActivity(intent);
            }
        });

        profile_bttn = findViewById(R.id.profile_bttn);
        profile_bttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityMain.this, ActivityProfile.class);
                startActivity(intent);
            }
        });
    }
}