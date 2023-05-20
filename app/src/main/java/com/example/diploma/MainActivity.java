package com.example.diploma;

import static com.kizitonwose.calendar.core.ExtensionsKt.daysOfWeek;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.CalendarMonth;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.view.CalendarView;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder;
import com.kizitonwose.calendar.view.ViewContainer;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private LocalDate selectedDate = null;
    CalendarView calendarView = null;
    TextView date = null;
    FrameLayout flBottomSheet = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        calendarView = findViewById(R.id.calendarView);
        date = findViewById(R.id.main_text);
        flBottomSheet = findViewById(R.id.standard_bottom_sheet);
        List<DayOfWeek> dayOfWeeks = daysOfWeek(DayOfWeek.MONDAY);

        /* Day number binder */
        calendarView.setDayBinder(new MonthDayBinder<DayViewContainer>() {
            @NonNull
            @Override
            public DayViewContainer create(@NonNull View view) {
                return new DayViewContainer(view);
            }

            @Override
            public void bind(@NonNull DayViewContainer container, CalendarDay calendarDay) {
                container.day = calendarDay;
                container.setOnClickListener(calendarView);
                container.textView.setText(String.valueOf(calendarDay.getDate().getDayOfMonth()));
                if (calendarDay.getPosition() == DayPosition.MonthDate) {
                    if (calendarDay.getDate().equals(selectedDate)) {
                        container.textView.setTextColor(Color.WHITE);
                        container.textView.setBackgroundResource(R.drawable.ic_launcher_background);
                    } else {
                        container.textView.setTextColor(Color.BLACK);
                        container.textView.setBackgroundResource(0);
                    }
                }
            }
        });

        /* Weekdays binder */
        calendarView.setMonthHeaderBinder(new MonthHeaderFooterBinder<MonthViewContainer>() {
            @NonNull
            @Override
            public MonthViewContainer create(@NonNull View view) {
                return new MonthViewContainer(view);
            }

            @Override
            public void bind(@NonNull MonthViewContainer container, CalendarMonth calendarMonth) {
                if (container.titlesContainer.getTag() == null) {
                    container.titlesContainer.setTag(calendarMonth.getYearMonth());
                    int count = container.titlesContainer.getChildCount();
                    for (int i = 0; i < count; i++) {
                        TextView it = (TextView) container.titlesContainer.getChildAt(i);
                        it.setText(calendarMonth.getWeekDays().get(0).get(i).getDate().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()));
                    }
                }
            }
        });


        /* Calendar final setup */
        calendarView.setup(YearMonth.now().minusYears(50), YearMonth.now().plusYears(50), dayOfWeeks.get(0));
        calendarView.scrollToMonth(YearMonth.now());


        /* Bottom Sheet Behavior */
        BottomSheetBehavior<FrameLayout> bsBehavior = BottomSheetBehavior.from(flBottomSheet);
        bsBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback(){
          @Override
          public void onStateChanged(@NonNull View bottomSheet, int newState) {
              if (newState == BottomSheetBehavior.STATE_EXPANDED) {

              } else {

              }
          }

          @Override
          public void onSlide(@NonNull View bottomSheet, float slideOffset) {

          }
      });

        selectedDate = LocalDate.now();
        calendarView.notifyDateChanged(selectedDate);
        date.setText(prettyDate(selectedDate));
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    static class MonthViewContainer extends ViewContainer {
        ViewGroup titlesContainer;

        public MonthViewContainer(View view) {
            super(view);
            this.titlesContainer = (ViewGroup) view;
        }
    }

    class DayViewContainer extends ViewContainer {
        View view;
        TextView textView;
        CalendarDay day;

        public DayViewContainer(@NonNull View view) {
            super(view);
            this.view = view;
            textView = view.findViewById(R.id.calendarDayText);
        }

        public void setOnClickListener(CalendarView calendarView) {
            view.setOnClickListener(v -> {
                if (day.getPosition() == DayPosition.MonthDate) {
                    LocalDate currentSelection = selectedDate;
                    if (currentSelection == day.getDate()) {
                        calendarView.notifyDateChanged(currentSelection);
                        selectedDate = LocalDate.now();
                        currentSelection = selectedDate;
                        date.setText(prettyDate(currentSelection));
                        calendarView.notifyDateChanged(currentSelection);
                    } else {
                        selectedDate = day.getDate();
                        date.setText(prettyDate(selectedDate));
                        calendarView.notifyDateChanged(selectedDate);
                        if (currentSelection != null) {
                            calendarView.notifyDateChanged(currentSelection);
                        }
                    }
                }
            });
        }
    }

    public String prettyDate(LocalDate date){
        return date.getDayOfMonth() + " " + date.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
    }
}