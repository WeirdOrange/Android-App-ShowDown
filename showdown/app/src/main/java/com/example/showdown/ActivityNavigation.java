// NavigationHelper.java
package com.example.showdown;

import static android.widget.Toast.LENGTH_SHORT;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class ActivityNavigation {
    private final Activity currentActivity;
    private View navbar, rootView, overlay;
    private Button mainButton, calendarButton, settingButton, toggle_closenav_btn;

    private static final String TAG = "NavActivity";

    public ActivityNavigation(Activity activity) {
        this.currentActivity = activity;
        this.rootView = ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
        initializeViews();
        setupNavigation();
        setupOutsideClickListener();

    }

    private void initializeViews() {
        navbar = currentActivity.findViewById(R.id.navbar_container);
        mainButton = currentActivity.findViewById(R.id.main_bttn);
        calendarButton = currentActivity.findViewById(R.id.calendar_bttn);
        settingButton = currentActivity.findViewById(R.id.setting_bttn);
        toggle_closenav_btn = currentActivity.findViewById(R.id.toggle_closenav_btn);
    }

    private void setupNavigation() {
        mainButton.setOnClickListener(v -> navigateTo(ActivityMain.class));
        calendarButton.setOnClickListener(v -> navigateTo(ActivityCalendar.class));
        settingButton.setOnClickListener(v -> navigateTo(ActivityProfile.class));
        toggle_closenav_btn.setOnClickListener(v -> toggle());
    }

    public void toggle() {
        if (navbar != null) {
            Log.i(TAG, "navbar is not null");
            if (navbar.getVisibility() == View.VISIBLE) {
                Log.i(TAG, "trying to close navbar");
                navbar.setVisibility(View.GONE);
                overlay.setVisibility(View.VISIBLE);
            } else {
                Log.i(TAG, "opening navbar");
                navbar.setVisibility(View.VISIBLE);
                overlay.setVisibility(View.GONE);
            }
        } else {
            Log.e(TAG, "An error happened: navbar not found");
        }
    }

    private void navigateTo(Class<?> destination) {
        if (!currentActivity.getClass().equals(destination)) {
            Intent intent = new Intent(currentActivity, destination);
            currentActivity.startActivity(intent);
            currentActivity.finish();
        }
    }

    // Close navbar if user clicks outside of navbar
    @SuppressLint("ClickableViewAccessibility")
    private void setupOutsideClickListener() {
        rootView.setOnTouchListener((v,event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                if (navbar.getVisibility() == View.VISIBLE) {
                    float x = event.getX();
                    float y = event.getY();
                    int[] navbarLocation = new int[2];
                    navbar.getLocationOnScreen(navbarLocation);

                    int navbarTop = navbarLocation[1];
                    int navbarBottom = navbarTop + navbar.getHeight();
                    int navbarLeft = navbarLocation[0];
                    int navbarRight = navbarLeft + navbar.getWidth();

                    if (y < navbarTop || y > navbarBottom || x < navbarLeft || x > navbarRight) {
                        toggle();
                        return true;
                    }
                }
            }
            return false; // If user drag or other touch events
        });
    }
}