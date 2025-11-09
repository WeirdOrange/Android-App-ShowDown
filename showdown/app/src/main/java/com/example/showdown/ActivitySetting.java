package com.example.showdown;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActivitySetting extends AppCompatActivity {

    private AppDatabase db;
    private ExecutorService executorService;
    private SharedPreferences sharedPreferences;
    private SharedPreferences themePrefs;
    private ActivityNavigation navHelper;

    private ImageButton btnProfile1, btnProfile2;
    private EditText etUsername, etEmail, etPhoneNumber;
    private RadioGroup rgTheme;
    private RadioButton rbSystem, rbLight, rbDark;
    private Button navigation_bttn, btnLogout;
    private int currentUserId = -1;

    private DBUser currentUser;
    private byte[] selectedProfileImageBytes = null;
    private byte[] selectedBackgroundImageBytes = null;

    private final ActivityResultLauncher<Intent> profileImagePickerLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Uri imageUri = result.getData().getData();
                if (imageUri != null) {
                    loadProfileImage(imageUri);
                }
            }
        });
    private final ActivityResultLauncher<Intent> backgroundImagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        loadProfileBackgroundImage(imageUri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("SETTING", "Setting Page loading");
        setContentView(R.layout.activity_setting);
        Log.d("SETTING", "Setting Page Complete");

        db = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();
        sharedPreferences = getSharedPreferences("ShowdownPrefs", MODE_PRIVATE);
        themePrefs = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        currentUserId = sharedPreferences.getInt("userId", -1);

        navHelper = new ActivityNavigation(this);

        initializeViews();
        setupThemeSelection();
        setupClickListeners();
        loadUserData();
    }

    private void initializeViews() {
        btnProfile1 = findViewById(R.id.profile1);
        btnProfile2 = findViewById(R.id.profile2);
        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        etPhoneNumber = findViewById(R.id.et_phone_number);
        rgTheme = findViewById(R.id.rg_theme);
        rbSystem = findViewById(R.id.rb_system);
        rbLight = findViewById(R.id.rb_light);
        rbDark = findViewById(R.id.rb_dark);
        navigation_bttn = findViewById(R.id.toggle_nav_btn);
        btnLogout = findViewById(R.id.btn_logout);
    }

    private void loadUserData() {
        if (currentUserId == -1) {
            Toast.makeText(this, "User not logged in",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        executorService.execute(() -> {
            try {
                currentUser = db.userDao().getUserById(currentUserId);

                if (currentUser != null) {
                    runOnUiThread(() -> {
                        etUsername.setText(currentUser.name);
                        etEmail.setText(currentUser.email);
                        etPhoneNumber.setText(currentUser.phoneNumber);

                        if (currentUser.profile != null && currentUser.profile.length > 0) {
                            Bitmap profileBitmap = BitmapFactory.decodeByteArray(
                                    currentUser.profile, 0, currentUser.profile.length);
                            btnProfile1.setImageBitmap(profileBitmap);
                        }
                        if (currentUser.backgroundProfile != null && currentUser.backgroundProfile.length > 0) {
                            Bitmap bgBitmap = BitmapFactory.decodeByteArray(
                                    currentUser.backgroundProfile, 0, currentUser.backgroundProfile.length);
                            btnProfile2.setImageBitmap(bgBitmap);
                        }
                        // TODO: MAKE A DEFAULT BACKGROUND PROFILE
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "User not found",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Error loading user data: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void setupThemeSelection() {
        int currentTheme = themePrefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        switch (currentTheme) {
            case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
                rbSystem.setChecked(true);
                break;
            case AppCompatDelegate.MODE_NIGHT_NO:
                rbLight.setChecked(true);
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                rbDark.setChecked(true);
                break;
        }

        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            int themeMode;
            if (checkedId == R.id.rb_system) {
                themeMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            } else if (checkedId == R.id.rb_light) {
                themeMode = AppCompatDelegate.MODE_NIGHT_NO;
            } else {
                themeMode = AppCompatDelegate.MODE_NIGHT_YES;
            }

            themePrefs.edit().putInt("theme_mode", themeMode).apply();
            AppCompatDelegate.setDefaultNightMode(themeMode);
            Toast.makeText(this, "Theme updated",
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void setupClickListeners() {
        navigation_bttn.setOnClickListener(v -> navHelper.toggle());

        btnProfile1.setOnClickListener(v -> openProfileImagePicker());
        btnProfile2.setOnClickListener(v -> openProfileBackgroundImagePicker());

        etUsername.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                saveUsername();
            }
        });

        etEmail.setOnClickListener(v -> showChangeEmailDialog());
        etEmail.setFocusable(false);
        etEmail.setClickable(true);

        // Phone number save
        etPhoneNumber.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                savePhoneNumber();
            }
        });

        btnLogout.setOnClickListener(v -> logout());
    }

    private void openProfileImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        profileImagePickerLauncher.launch(intent);
    }

    private void openProfileBackgroundImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        backgroundImagePickerLauncher.launch(intent);
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

    private void loadProfileImage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            Bitmap resizedBitmap = resizeBitmap(bitmap, 800, 800);
            btnProfile1.setImageBitmap(resizedBitmap);

            // Byte Converter
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
            selectedProfileImageBytes = stream.toByteArray();
            saveProfileImage();

            Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show();

            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading image: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void loadProfileBackgroundImage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            Bitmap resizedBitmap = resizeBitmap(bitmap, 1200, 600);
            btnProfile2.setImageBitmap(resizedBitmap);

            // Byte Converter
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
            selectedBackgroundImageBytes = stream.toByteArray();

            saveBackgroundImage();

            Toast.makeText(this, "Background image updated!", Toast.LENGTH_SHORT).show();

            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading image: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUsername() {
        String newUsername = etUsername.getText().toString().trim();

        if (newUsername.isEmpty()) {
            Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            try {
                currentUser.name = newUsername;
                db.userDao().update(currentUser);

                sharedPreferences.edit().putString("userName", newUsername).apply();

                runOnUiThread(() ->
                        Toast.makeText(this, "Username updated successfully",
                                Toast.LENGTH_SHORT).show()
                );
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Error updating username: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void showChangeEmailDialog() {
        String currentEmail = etEmail.getText().toString().trim();

        AlertDialog.Builder builder = new MaterialAlertDialogBuilder(this);
        final EditText input = new EditText(this);
        input.setHint("Enter new email");
        input.setText(currentEmail);

        builder.setTitle("Change Email")
                .setMessage("You will receive an OTP to verify your new email address")
                .setView(input)
                .setPositiveButton("Continue", (dialog, which) -> {
                    String newEmail = input.getText().toString().trim();
                    if (android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                        requestEmailChange(newEmail);
                    } else {
                        Toast.makeText(this, "Please enter a valid email",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void requestEmailChange(String newEmail) {
        String otp = OTPDialog.generateOTP();
        OTPDialog.saveOTP(this, newEmail, otp);
        OTPDialog.sendOTPEmail(newEmail,otp);

        showOTPDialog(newEmail);
    }

    private void showOTPDialog(String newEmail) {
        Toast.makeText(this, "OTP sent to " + newEmail, Toast.LENGTH_SHORT).show();

        AlertDialog.Builder builder = new MaterialAlertDialogBuilder(this);
        final EditText input = new EditText(this);
        input.setHint("Enter 6-digit OTP");

        builder.setTitle("Verify OTP")
                .setMessage("Enter the OTP sent to " + newEmail)
                .setView(input)
                .setPositiveButton("Verify", (dialog, which) -> {
                    String enteredOTP = input.getText().toString().trim();
                    if (OTPDialog.verifyOTP(this, newEmail, enteredOTP)) {
                        updateEmail(newEmail);
                        OTPDialog.clearOTP(this, newEmail);
                    } else {
                        Toast.makeText(this, "Invalid or expired OTP",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateEmail(String newEmail) {
        executorService.execute(() -> {
            try {
                currentUser.email = newEmail;
                db.userDao().update(currentUser);

                sharedPreferences.edit().putString("userEmail", newEmail).apply();

                runOnUiThread(() -> {
                    etEmail.setText(newEmail);
                    Toast.makeText(this, "Email updated successfully",
                            Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Error updating email: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void savePhoneNumber() {
        String newPhone = etPhoneNumber.getText().toString().trim();

        executorService.execute(() -> {
            try {
                currentUser.phoneNumber = newPhone;
                db.userDao().update(currentUser);

                runOnUiThread(() ->
                        Toast.makeText(this, "Phone number updated successfully",
                                Toast.LENGTH_SHORT).show()
                );
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Error updating phone: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
    private void saveProfileImage() {
        if (selectedProfileImageBytes == null) return;

        executorService.execute(() -> {
            try {
                currentUser.profile = selectedProfileImageBytes;
                db.userDao().update(currentUser);

                runOnUiThread(() ->
                        Toast.makeText(this, "Profile image saved", Toast.LENGTH_SHORT).show()
                );
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Error saving profile image: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void saveBackgroundImage() {
        if (selectedBackgroundImageBytes == null) return;

        executorService.execute(() -> {
            try {
                currentUser.backgroundProfile = selectedBackgroundImageBytes;
                db.userDao().update(currentUser);

                runOnUiThread(() ->
                        Toast.makeText(this, "Background image saved", Toast.LENGTH_SHORT).show()
                );
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Error saving background image: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void logout() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    sharedPreferences.edit().clear().apply();
                    Intent intent = new Intent(ActivitySetting.this, ActivityLogin.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
