package com.example.showdown;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActivityLogin extends AppCompatActivity {
    private EditText emailInput, passwordInput;
    private Button login_bttn, visit_signup_bttn;
    private AppDatabase db;
    private ExecutorService executorService;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        db = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();
        sharedPreferences = getSharedPreferences("ShowdownPrefs", MODE_PRIVATE);

        int userId = sharedPreferences.getInt("userId", -1);
        if (userId != -1) {
            navigateToMain();
            return;
        }
        createDummyUser();

        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        login_bttn = findViewById(R.id.login_bttn);
        visit_signup_bttn = findViewById(R.id.visit_signup_bttn);

//        login_bttn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(ActivityLogin.this, ActivityMain.class);
//                startActivity(intent);
//            }
//        });

        login_bttn.setOnClickListener(v -> loginUser());
        visit_signup_bttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityLogin.this, ActivitySignup.class);
                startActivity(intent);
            }
        });
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            try {
                DBUser user = db.userDao().login(email, password);

                if (user != null) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("userId", user.id);
                    editor.putString("userName", user.name);
                    editor.putString("userEmail", user.email);
                    editor.apply();

                    runOnUiThread(() -> {
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    });
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                    );
                }
            }catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "An error occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void navigateToMain() {
        Intent intent = new Intent(ActivityLogin.this, ActivityMain.class);
        startActivity(intent);
        finish();
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