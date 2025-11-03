package com.example.showdown;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ActivitySignup extends AppCompatActivity {
    private Button signup_bttn;
    private Button visit_login_bttn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        signup_bttn = findViewById(R.id.signup_bttn);
        signup_bttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivitySignup.this, ActivityLogin.class);
                startActivity(intent);
            }
        });

        visit_login_bttn = findViewById(R.id.visit_login_bttn);
        visit_login_bttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivitySignup.this, ActivityLogin.class);
                startActivity(intent);
            }
        });
    }
}