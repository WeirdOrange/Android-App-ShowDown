package CalendarView;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.showdown.R;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CalendarView extends LinearLayout {

    private TextView tvCurrentMonth;
    private ImageView ivPreviousMonth, ivNextMonth;
    private GridLayout gridCalendarDays;
    private LocalDate currentMonth;
    private Set<LocalDate> selectedDate;
    private OnDateSelectedListener dateSelectedListener;

    private DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(("dd-MM-yyyy"));
    private DateTimeFormatter monthFormat = DateTimeFormatter.ofPattern(("MMMM yyyy"));

    private void init(Context context){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.custom_calendar, this, true);

        tvCurrentMonth = findViewById(R.id.tv_current_month);
        ivPreviousMonth = findViewById(R.id.iv_previous_month);
        ivNextMonth = findViewById(R.id.iv_next_month);
        gridCalendarDays = findViewById(R.id.grid_calendar_days);

        selectedDate = new HashSet<>();
        currentMonth = LocalDate.now().withDayOfMonth(1);

        ivPreviousMonth.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = currentMonth.minusMonths(1);
                setupCalendar();
            }
        });
        ivNextMonth.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = currentMonth.plusMonths(1);
                setupCalendar();
            }
        });
        setupCalendar();
    }

    private void setupCalendar(){
        gridCalendarDays.removeAllViews();
        tvCurrentMonth.setText(currentMonth.format(monthFormat));

        int firstDayOfWeek = currentMonth.getDayOfWeek().getValue() % 7;

        // ADD First Empty Days
        for (int i = 0; i < firstDayOfWeek; i++) {
            TextView emptyDay = new TextView(getContext(), null, 0, R.style.CalendarDate);
            emptyDay.setLayoutParams(new GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED, 1f),GridLayout.spec(GridLayout.UNDEFINED, 1f)
            ));
            emptyDay.setText("");
            gridCalendarDays.addView(emptyDay);
        }

        // Actual Days of the current month
        for (int day = 1; day <= currentMonth.lengthOfMonth(); day++) {
            // Get the full date for this day
            final LocalDate date = currentMonth.withDayOfMonth(day);
            TextView dayView = new TextView(getContext(), null, 0, R.style.CalendarDate);
            dayView.setLayoutParams(new GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED, 1f), GridLayout.spec(GridLayout.UNDEFINED, 1f)
            ));
            dayView.setText("");

            dayView.setText(String.valueOf(day));
            dayView.setTag(date);

            // Highlight date when selected
            dayView.setSelected(selectedDate.contains(date));

            dayView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    LocalDate clickedDate = (LocalDate) v.getTag();
                    if (selectedDate.contains(clickedDate)) {
                        selectedDate.remove(clickedDate);
                        v.setSelected(false);
                    } else {
                        selectedDate.add(clickedDate);
                        v.setSelected(true);
                    }

                    if (dateSelectedListener != null) {
                        dateSelectedListener.onDateSelected(getSelectedDates());
                    }
                    Log.d("CALENDAR", "Clicked: " + v.getTag());
                    setupCalendar();
                }
            });

            gridCalendarDays.addView(dayView);
        }

        // ADD end empty days
        int totalCells = firstDayOfWeek + currentMonth.lengthOfMonth();

        for (int i = totalCells; i < 42; i++) {
            TextView emptyDay = new TextView(getContext(), null, 0, R.style.CalendarDate);
            emptyDay.setLayoutParams(new GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED, 1f), GridLayout.spec(GridLayout.UNDEFINED, 1f)
            ));
            emptyDay.setText("");
            gridCalendarDays.addView(emptyDay);
        }
    }

    // Accept external String dates, parses them and update selection
    public void setSelectedDate(ArrayList<String> dates) {
        selectedDate.clear();
        for (String dateStr : dates) {
            try {
                selectedDate.add(LocalDate.parse(dateStr,dateFormat));
            } catch (Exception ex) {
                Log.e(TAG, "setSelectedDates");
            }
        }
        setupCalendar();
    }

    public ArrayList<String> getSelectedDates() {
        // Return Selected Dates as string
        ArrayList<String> dates = new ArrayList<>();
        for (LocalDate date:selectedDate) {
            dates.add(date.format(dateFormat));
        }
        return dates;
    }

    public void setDateSelectedListener (OnDateSelectedListener dateSelectedListener) {
        this.dateSelectedListener = dateSelectedListener;
    }

    public CalendarView(Context context) {
        super(context);
        init(context);
    }

    public CalendarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CalendarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public CalendarView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }
}
