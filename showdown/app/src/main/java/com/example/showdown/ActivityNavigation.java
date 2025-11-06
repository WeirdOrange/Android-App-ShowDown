// NavigationHelper.java
package com.example.showdown;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class ActivityNavigation {
    private final Activity currentActivity;
    private View navbar, rootView, overlay;
    private Button mainButton, calendarButton, settingButton;

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
    }

    private void setupNavigation() {
        mainButton.setOnClickListener(v -> navigateTo(ActivityMain.class));
        calendarButton.setOnClickListener(v -> navigateTo(ActivityCalendar.class));
        settingButton.setOnClickListener(v -> navigateTo(ActivityProfile.class));

    }

    public void toggle() {
        if (navbar.getVisibility() == View.VISIBLE) {
            navbar.animate().translationY(navbar.getHeight()).alpha(0f).setDuration(300)
                    .withEndAction(() -> navbar.setVisibility(View.GONE));
            overlay.setVisibility(View.VISIBLE);
        } else {
            navbar.setVisibility(View.VISIBLE);
            navbar.setTranslationY(navbar.getHeight());
            navbar.animate().translationY(0).alpha(1f).setDuration(300);

            overlay.setVisibility(View.GONE);
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
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
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