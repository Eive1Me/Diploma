package com.example.diploma;

import static com.kizitonwose.calendar.core.ExtensionsKt.daysOfWeek;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.CalendarMonth;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.core.Week;
import com.kizitonwose.calendar.core.WeekDay;
import com.kizitonwose.calendar.core.WeekDayPosition;
import com.kizitonwose.calendar.view.CalendarView;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder;
import com.kizitonwose.calendar.view.ViewContainer;
import com.kizitonwose.calendar.view.WeekCalendarView;
import com.kizitonwose.calendar.view.WeekDayBinder;
import com.kizitonwose.calendar.view.WeekHeaderFooterBinder;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private LocalDate selectedDate = null;
    CalendarView monthCalendarView = null;
    WeekCalendarView weekCalendarView = null;
    TextView month_day = null;
    TextView week_day = null;
    FrameLayout flBottomSheet = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        weekCalendarView = findViewById(R.id.weekCalendarView);
        monthCalendarView = findViewById(R.id.calendarView);
        month_day = findViewById(R.id.month_day);
        week_day = findViewById(R.id.week_day);
        flBottomSheet = findViewById(R.id.standard_bottom_sheet);
        List<DayOfWeek> dayOfWeeks = daysOfWeek(DayOfWeek.MONDAY);

        /* Day number binder */
        MonthDayBinder<DayViewContainer> monthDayBinder = new MonthDayBinder<DayViewContainer>() {
            @NonNull
            @Override
            public DayViewContainer create(@NonNull View view) {
                return new DayViewContainer(view);
            }

            @Override
            public void bind(@NonNull DayViewContainer container, CalendarDay calendarDay) {
                container.day = calendarDay;
                container.setOnClickListener(monthCalendarView);
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
        };
        WeekDayBinder<WeekDayViewContainer> dayBinder = new WeekDayBinder<WeekDayViewContainer>() {
            @NonNull
            @Override
            public WeekDayViewContainer create(@NonNull View view) {
                return new WeekDayViewContainer(view);
            }

            @Override
            public void bind(@NonNull WeekDayViewContainer container, WeekDay weekDay) {
                container.day = weekDay;
                container.setOnClickListener(weekCalendarView);
                container.textView.setText(String.valueOf(weekDay.getDate().getDayOfMonth()));
                if (weekDay.getPosition() == WeekDayPosition.RangeDate) {
                    if (weekDay.getDate().equals(selectedDate)) {
                        container.textView.setTextColor(Color.WHITE);
                        container.textView.setBackgroundResource(R.drawable.ic_launcher_background);
                    } else {
                        container.textView.setTextColor(Color.BLACK);
                        container.textView.setBackgroundResource(0);
                    }
                }
            }
        };

        /* Weekdays binder */
        MonthHeaderFooterBinder<MonthViewContainer> monthHeader = new MonthHeaderFooterBinder<MonthViewContainer>() {
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
        };
        WeekHeaderFooterBinder<MonthViewContainer> weekHeader = new WeekHeaderFooterBinder<MonthViewContainer>() {
            @NonNull
            @Override
            public MonthViewContainer create(@NonNull View view) {
                return new MonthViewContainer(view);
            }

            @Override
            public void bind(@NonNull MonthViewContainer container, Week week) {
                if (container.titlesContainer.getTag() == null) {
                    container.titlesContainer.setTag(week.getDays().get(0));
                    int count = container.titlesContainer.getChildCount();
                    for (int i = 0; i < count; i++) {
                        TextView it = (TextView) container.titlesContainer.getChildAt(i);
                        it.setText(week.getDays().get(i).getDate().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()));
                    }
                }
            }
        };

        /* Calendar final setup */
        monthCalendarView.setDayBinder(monthDayBinder);
        monthCalendarView.setMonthHeaderBinder(monthHeader);
        monthCalendarView.setup(YearMonth.now().minusYears(50), YearMonth.now().plusYears(50), dayOfWeeks.get(0));
        monthCalendarView.scrollToMonth(YearMonth.now());

        /* Week calendar final setup */
        weekCalendarView.setDayBinder(dayBinder);
        weekCalendarView.setWeekHeaderBinder(weekHeader);
        weekCalendarView.setup(LocalDate.now().minusYears(50), LocalDate.now().plusYears(50), dayOfWeeks.get(0));
        weekCalendarView.scrollToWeek(LocalDate.now());
        weekCalendarView.setVisibility(View.INVISIBLE);

        /* Bottom Sheet Behavior */
        BottomSheetBehavior<FrameLayout> bsBehavior = BottomSheetBehavior.from(flBottomSheet);
        bsBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback(){
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    // BottomSheet развернут, показываем weekCalendarView и скрываем calendarView
                    weekCalendarView.setVisibility(View.VISIBLE);
                    monthCalendarView.setVisibility(View.INVISIBLE);

                    // Переместиться к выбранной дате в weekCalendarView
                    if (selectedDate != null) {
                        weekCalendarView.scrollToWeek(selectedDate);
                    }
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED || newState == BottomSheetBehavior.STATE_HIDDEN) {
                    // BottomSheet свернут или скрыт, показываем calendarView и скрываем weekCalendarView
                    monthCalendarView.setVisibility(View.VISIBLE);
                    weekCalendarView.setVisibility(View.INVISIBLE);

                    // Переместиться к выбранной дате в calendarView
                    if (selectedDate != null) {
                        monthCalendarView.scrollToMonth(YearMonth.from(selectedDate));
                    }
                } else {
                    weekCalendarView.setVisibility(View.VISIBLE);
                    monthCalendarView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // Определяем максимальную высоту BottomSheet
                int maxHeight = getResources().getDisplayMetrics().heightPixels;

                // Изменяем прозрачность в зависимости от slideOffset
                float calendarViewAlpha = 1f - slideOffset;
                float weekCalendarViewAlpha = slideOffset;

                // Меняем прозрачность пропорционально slideOffset и положению представлений
                calendarViewAlpha *= calculateAlpha(monthCalendarView.getTop(), maxHeight);
                weekCalendarViewAlpha *= calculateAlpha(weekCalendarView.getTop(), maxHeight);

                // Создаем аниматоры для альфа-канала представлений
                ObjectAnimator calendarViewAnimator = ObjectAnimator.ofFloat(monthCalendarView, "alpha", calendarViewAlpha);
                ObjectAnimator weekCalendarViewAnimator = ObjectAnimator.ofFloat(weekCalendarView, "alpha", weekCalendarViewAlpha);

                // Создаем AnimatorSet и добавляем аниматоры
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.play(calendarViewAnimator).with(weekCalendarViewAnimator);
                animatorSet.setDuration(0); // Установите желаемую продолжительность анимации
                animatorSet.start();
            }
        });

        selectedDate = LocalDate.now();
        monthCalendarView.notifyDateChanged(selectedDate);
        month_day.setText(prettyDate(selectedDate));
        week_day.setText(selectedDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()));
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

    class WeekDayViewContainer extends ViewContainer {
        View view;
        TextView textView;
        WeekDay day;

        public WeekDayViewContainer(@NonNull View view) {
            super(view);
            this.view = view;
            textView = view.findViewById(R.id.calendarDayText);
        }

        public void setOnClickListener(WeekCalendarView weekCalendarView) {
            view.setOnClickListener(v -> {
                if (day.getPosition() == WeekDayPosition.RangeDate) {
                    LocalDate currentSelection = selectedDate;
                    if (currentSelection == day.getDate()) {
                        monthCalendarView.notifyDateChanged(currentSelection);
                        weekCalendarView.notifyDateChanged(currentSelection);
                        selectedDate = LocalDate.now();
                        month_day.setText(prettyDate(currentSelection));
                        week_day.setText(currentSelection.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()));
                        monthCalendarView.notifyDateChanged(currentSelection);
                        weekCalendarView.notifyDateChanged(currentSelection);
                    } else {
                        selectedDate = day.getDate();
                        month_day.setText(prettyDate(selectedDate));
                        week_day.setText(currentSelection.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()));
                        monthCalendarView.notifyDateChanged(selectedDate);
                        weekCalendarView.notifyDateChanged(selectedDate);
                        if (currentSelection != null) {
                            monthCalendarView.notifyDateChanged(currentSelection);
                            weekCalendarView.notifyDateChanged(currentSelection);
                        }
                    }
                }
            });
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

        public void setOnClickListener(CalendarView monthCalendarView) {
            view.setOnClickListener(v -> {
                if (day.getPosition() == DayPosition.MonthDate) {
                    LocalDate currentSelection = selectedDate;
                    if (currentSelection == day.getDate()) {
                        monthCalendarView.notifyDateChanged(currentSelection);
                        weekCalendarView.notifyDateChanged(currentSelection);
                        selectedDate = LocalDate.now();
                        month_day.setText(prettyDate(currentSelection));
                        week_day.setText(currentSelection.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()));
                        monthCalendarView.notifyDateChanged(currentSelection);
                        weekCalendarView.notifyDateChanged(currentSelection);
                    } else {
                        selectedDate = day.getDate();
                        month_day.setText(prettyDate(selectedDate));
                        week_day.setText(currentSelection.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()));
                        monthCalendarView.notifyDateChanged(selectedDate);
                        weekCalendarView.notifyDateChanged(selectedDate);
                        if (currentSelection != null) {
                            monthCalendarView.notifyDateChanged(currentSelection);
                            weekCalendarView.notifyDateChanged(currentSelection);
                        }
                    }
                }
            });
        }
    }

    public String prettyDate(LocalDate date){
        return date.getDayOfMonth() + " " + date.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
    }

    private float calculateAlpha(int viewTop, int maxHeight) {
        // В этом примере, я просто использую линейную интерполяцию, чтобы прозрачность изменялась равномерно в зависимости от положения представления.
        float alphaPercentage = (float) viewTop / maxHeight;
        return 1f - alphaPercentage; // Инвертируем значение, чтобы прозрачность увеличивалась с движением вверх.
    }
}