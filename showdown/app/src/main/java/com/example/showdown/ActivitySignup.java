package com.example.showdown;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActivitySignup extends AppCompatActivity {
    private EditText usernameInput, emailInput, passwordInput, confirmPasswordInput;
    private Button signup_bttn, visit_login_bttn;
    private AppDatabase db;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        db = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        usernameInput = findViewById(R.id.username_input);
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        confirmPasswordInput = findViewById(R.id.confirm_password_input);
        signup_bttn = findViewById(R.id.signup_bttn);
        visit_login_bttn = findViewById(R.id.visit_login_bttn);

        signup_bttn.setOnClickListener(v -> registerUser());

        visit_login_bttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivitySignup.this, ActivityLogin.class);
                startActivity(intent);
            }
        });
    }

    private void registerUser() {
        String username = usernameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        if (username.isEmpty()) {
            Toast.makeText(this, "Please enter username", Toast.LENGTH_SHORT).show();
            return;
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty() || password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            try {
                int emailExists = db.userDao().checkEmailExists(email);
                if (emailExists > 0) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Email already registered", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                String otp = OTPDialog.generateOTP();
                OTPDialog.saveOTP(this, email, otp);

                // Send OTP via email
                OTPDialog.sendOTPEmail(email, otp);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Redirecting to OTP Verification...", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ActivitySignup.this, ActivityOTPVerification.class);
                    intent.putExtra("username",username);
                    intent.putExtra("email",email);
                    intent.putExtra("password",password);
                    intent.putExtra("phoneNumber","");
                    startActivity(intent);
                    Log.i("Signup verification","HANDING INFO TO OTP");
                    finish();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "An error occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
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