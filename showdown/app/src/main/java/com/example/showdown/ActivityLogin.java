package com.example.showdown;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActivityLogin extends AppCompatActivity {
    private Button login_bttn;
    private Button visit_signup_bttn;
    private AppDatabase db;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        db = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        createDummyUser();

        login_bttn = findViewById(R.id.login_bttn);
        login_bttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityLogin.this, ActivityMain.class);
                startActivity(intent);
            }
        });

        visit_signup_bttn = findViewById(R.id.visit_signup_bttn);
        visit_signup_bttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityLogin.this, ActivitySignup.class);
                startActivity(intent);
            }
        });
    }

    private void createDummyUser() {
        executorService.execute(() -> {
            try {
                List<DBUser> users = db.userDao().getAllBlocking();
                if (users.isEmpty()) {
                    // Create dummy user
                    DBUser dummyUser = new DBUser(
                            "Ivan Lai",              // name
                            "p23015609@student.newinti.edu.my",  // email
                            "password123",           // password
                            "+60164075284"           // phone
                    );
                    db.userDao().insert(dummyUser);
                    runOnUiThread(() ->
                            Toast.makeText(this, "Dummy user created", Toast.LENGTH_SHORT).show()
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}