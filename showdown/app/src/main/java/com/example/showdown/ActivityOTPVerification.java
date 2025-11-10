package com.example.showdown;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActivityOTPVerification extends AppCompatActivity {
    private EditText otpInput1,otpInput2,otpInput3,otpInput4,otpInput5,otpInput6;
    private Button verifyButton;
    private TextView timerText, emailText, resendButton;
    private String username, email, password, phoneNum;
    private int userId;
    private AppDatabase db;
    private ExecutorService executorService;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = 300000; // 5 minutes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.request_otp_dialog);

        db = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        // Get signup details from intent
        username = getIntent().getStringExtra("username");
        email = getIntent().getStringExtra("email");
        password = getIntent().getStringExtra("password");
        phoneNum = getIntent().getStringExtra("phoneNumber");
        Log.i("OTP verification","IM IN");

        if (email == null) {
            Toast.makeText(this, "Invalid verification request", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        otpInput1 = findViewById(R.id.otp1);
        otpInput2 = findViewById(R.id.otp2);
        otpInput3 = findViewById(R.id.otp3);
        otpInput4 = findViewById(R.id.otp4);
        otpInput5 = findViewById(R.id.otp5);
        otpInput6 = findViewById(R.id.otp6);
        verifyButton = findViewById(R.id.done_button);
        resendButton = findViewById(R.id.tvResend);
        timerText = findViewById(R.id.timer_text);

        verifyButton.setOnClickListener(v -> verifyOTP(username, email, password, phoneNum));
        resendButton.setOnClickListener(v -> resendOTP());

        startTimer();
    }

    private void verifyOTP(String username, String email, String password, String phoneNum) {
        String enteredOTP = 
            otpInput1.getText().toString().trim() + 
            otpInput2.getText().toString().trim() +
            otpInput3.getText().toString().trim() +
            otpInput4.getText().toString().trim() +
            otpInput5.getText().toString().trim() +
            otpInput6.getText().toString().trim();

        if (enteredOTP.isEmpty()) {
            Toast.makeText(this, "Please enter OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        if (enteredOTP.length() != 6) {
            Toast.makeText(this, "OTP must be 6 digits", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verify OTP
        if (OTPDialog.verifyOTP(this, email, enteredOTP)) {
            // Mark user as verified
            executorService.execute(() -> {
                try {
                    // Create new user
                    DBUser newUser = new DBUser(username, email, password,"");
                    long userId = db.userDao().insert(newUser);
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() ->
                            Toast.makeText(this, "Verification failed", Toast.LENGTH_SHORT).show()
                    );
                }
            });

            runOnUiThread(() -> {
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ActivityOTPVerification.this, ActivityLogin.class);
                startActivity(intent);
                finish();
            });
        } else {
            runOnUiThread(() -> {
                Toast.makeText(this, "Invalid or expired OTP", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ActivityOTPVerification.this, ActivitySignup.class);
                startActivity(intent);
                finish();
            });
        }
    }

    private void resendOTP() {
        // Generate new OTP
        String newOTP = OTPDialog.generateOTP();
        OTPDialog.saveOTP(this, email, newOTP);

        // Send OTP via email
        OTPDialog.sendOTPEmail(email, newOTP);

        Toast.makeText(this, "OTP resent successfully!", Toast.LENGTH_SHORT).show();

        // Restart timer
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        timeLeftInMillis = 300000;
        startTimer();

        // Disable resend button temporarily
        resendButton.setEnabled(false);
        new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                resendButton.setText("Resend (" + millisUntilFinished / 1000 + "s)");
            }

            @Override
            public void onFinish() {
                resendButton.setEnabled(true);
                resendButton.setText("Resend OTP");
            }
        }.start();
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimer();
            }

            @Override
            public void onFinish() {
                timerText.setText("OTP Expired");
                verifyButton.setEnabled(false);
                Toast.makeText(ActivityOTPVerification.this, "OTP expired. Please request a new one.", Toast.LENGTH_LONG).show();
            }
        }.start();
    }

    private void updateTimer() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeFormatted = String.format("Time remaining: %02d:%02d", minutes, seconds);
        timerText.setText(timeFormatted);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
