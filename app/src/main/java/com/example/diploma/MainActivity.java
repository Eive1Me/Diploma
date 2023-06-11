package com.example.diploma;

import static com.kizitonwose.calendar.core.ExtensionsKt.daysOfWeek;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.drawerlayout.widget.DrawerLayout;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.ArcShape;
import android.graphics.drawable.shapes.OvalShape;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.diploma.local_database.DatabaseAdapter;
import com.example.diploma.model.Task;
import com.example.diploma.model.User;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private LocalDate selectedDate = null;
    CalendarView monthCalendarView = null;
    WeekCalendarView weekCalendarView = null;
    TextView month_day = null;
    TextView week_day = null;
    ListView taskViewList = null;
    TaskAdapter taskAdapter = null;
    FrameLayout flBottomSheet = null;
    List<Task> taskList = new ArrayList<>();
    DatabaseAdapter adapter = null;
    Button addTask = null;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    User currentUser = new User();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        weekCalendarView = findViewById(R.id.weekCalendarView);
        monthCalendarView = findViewById(R.id.calendarView);
        month_day = findViewById(R.id.month_day);
        week_day = findViewById(R.id.week_day);
        flBottomSheet = findViewById(R.id.standard_bottom_sheet);
        taskViewList = findViewById(R.id.taskList);
        addTask = findViewById(R.id.add_task);
        List<DayOfWeek> dayOfWeeks = daysOfWeek(DayOfWeek.MONDAY);
        int selectionDrawable = R.drawable.baseline_brightness_1_24;

        FileInputStream fin = null;
        try {
            fin = openFileInput("profile.txt");
            byte[] bytes = new byte[fin.available()];
            fin.read(bytes);
            String[] text = (new String (bytes)).split(":");
            if (text.length == 3)
                currentUser = new User(Long.parseLong(text[0]), text[1], text[2]);
        }
        catch(IOException | NullPointerException ex) {
            Toast.makeText(this, "Пожалуйста, войдите в систему.", Toast.LENGTH_SHORT).show();
        }
        finally{
            if(fin!=null) {
                try {
                    fin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        adapter = new DatabaseAdapter(this);
        adapter.open();
        try {
            taskList = adapter.getAllTasks();
            List<Task> newList = new ArrayList<>();
            for (Task task : taskList) {
                if (task.getUserId().getId() != currentUser.getId()) {
                    adapter.deleteTask(task.getId());
                } else {
                    newList.add(task);
                }
            }
            taskList = newList;
        } catch (SQLiteException ignored) {}
        adapter.close();

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

                // Проверяем, есть ли задачи на выбранную дату
                LocalDate date = calendarDay.getDate();
                List<Task> tasks = getTasksForDate(date);

                if (calendarDay.getPosition() == DayPosition.MonthDate) {
                    if (calendarDay.getDate().equals(selectedDate)) {
                        taskAdapter = new TaskAdapter(MainActivity.this, R.layout.list_layout, tasks);
                        taskViewList.setAdapter(taskAdapter);
                        container.textView.setTextColor(Color.WHITE);
                        container.textView.setBackgroundResource(selectionDrawable);
                    } else {
                        container.textView.setTextColor(Color.BLACK);
                        container.textView.setBackgroundResource(0);
                    }
                }
                if (!tasks.isEmpty()) {
                    Map<Color, Integer> colormap = new HashMap<>();
                    for (Task task : tasks) {
                        Color taskColor = Color.valueOf(Color.parseColor(task.getCategoryId().getColour()));
                        if (!colormap.containsKey(taskColor))
                            colormap.put(taskColor, 1);
                        else
                            colormap.put(taskColor, colormap.get(taskColor) + 1);
                    }

                    int totalPercentage = 0; // Общий процент, чтобы вычислить оставшийся цвет
                    for (int percentage : colormap.values()) {
                        totalPercentage += percentage;
                    }

                    float startAngle = 0;
                    int width = 50; // Ширина окружности
                    int height = 50; // Высота окружности

                    Bitmap bitmap = Bitmap.createBitmap(width + 8, height + 8, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);

                    RectF rectF = new RectF(8, 8, width, height);
                    for (int i = 0; i < colormap.values().size(); i++) {
                        int percentage = colormap.values().toArray(new Integer[]{})[i];
                        int color = colormap.keySet().toArray(new Color[]{})[i].toArgb();
                        float sweepAngle = (float) percentage / totalPercentage * 360;

                        Paint paint = new Paint();
                        paint.setAntiAlias(true);
                        paint.setStyle(Paint.Style.STROKE);
                        paint.setStrokeWidth(6);
                        paint.setColor(color);

                        canvas.drawArc(rectF, startAngle, sweepAngle, false, paint);

                        startAngle += sweepAngle;
                    }

                    Drawable drawable = new BitmapDrawable(container.progressCircle.getResources(), bitmap);
                    drawable.setAlpha(180);
                    container.progressCircle.setBackground(drawable);
                } else {
                    // Если задач нет, убираем фоновый рисунок
                    container.progressCircle.setBackgroundResource(0);
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
                        container.textView.setBackgroundResource(selectionDrawable);
                    } else {
                        container.textView.setTextColor(Color.BLACK);
                        container.textView.setBackgroundResource(0);
                    }
                }
                // Проверяем, есть ли задачи на выбранную дату
                LocalDate date = weekDay.getDate();
                List<Task> tasks = getTasksForDate(date);
                if (!tasks.isEmpty()) {
                    Map<Color, Integer> colormap = new HashMap<>();
                    for (Task task : tasks) {
                        Color taskColor = Color.valueOf(Color.parseColor(task.getCategoryId().getColour()));
                        if (!colormap.containsKey(taskColor))
                            colormap.put(taskColor, 1);
                        else
                            colormap.put(taskColor, colormap.get(taskColor) + 1);
                    }

                    int totalPercentage = 0; // Общий процент, чтобы вычислить оставшийся цвет
                    for (int percentage : colormap.values()) {
                        totalPercentage += percentage;
                    }

                    float startAngle = 0;
                    int width = 50; // Ширина окружности
                    int height = 50; // Высота окружности

                    Bitmap bitmap = Bitmap.createBitmap(width + 8, height + 8, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);

                    RectF rectF = new RectF(8, 8, width, height);
                    for (int i = 0; i < colormap.values().size(); i++) {
                        int percentage = colormap.values().toArray(new Integer[]{})[i];
                        int color = colormap.keySet().toArray(new Color[]{})[i].toArgb();
                        float sweepAngle = (float) percentage / totalPercentage * 360;

                        Paint paint = new Paint();
                        paint.setAntiAlias(true);
                        paint.setStyle(Paint.Style.STROKE);
                        paint.setStrokeWidth(6);
                        paint.setColor(color);

                        canvas.drawArc(rectF, startAngle, sweepAngle, false, paint);

                        startAngle += sweepAngle;
                    }

                    Drawable drawable = new BitmapDrawable(container.progressCircle.getResources(), bitmap);
                    drawable.setAlpha(180);
                    container.progressCircle.setBackground(drawable);
                } else {
                    // Если задач нет, убираем фоновый рисунок
                    container.progressCircle.setBackgroundResource(0);
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
                calendarViewAlpha *= Utils.calculateAlpha(monthCalendarView.getTop(), maxHeight);
                weekCalendarViewAlpha *= Utils.calculateAlpha(weekCalendarView.getTop(), maxHeight);

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

        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        Point screenSize = new Point();
        display.getSize(screenSize);
        int screenHeight = screenSize.y;
        ViewTreeObserver monthCalendarViewObserver = monthCalendarView.getViewTreeObserver();
        monthCalendarViewObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) flBottomSheet.getLayoutParams();
                int monthCalendarHeight = monthCalendarView.getHeight();
                BottomSheetBehavior<FrameLayout> bottomSheetBehavior = BottomSheetBehavior.from(flBottomSheet);
                bottomSheetBehavior.setPeekHeight(screenHeight - monthCalendarHeight - findViewById(R.id.titlesContainer).getHeight() - 8);
                layoutParams.setBehavior(bottomSheetBehavior);
                flBottomSheet.setLayoutParams(layoutParams);
                monthCalendarView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        ViewTreeObserver weekCalendarViewObserver = weekCalendarView.getViewTreeObserver();
        weekCalendarViewObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) flBottomSheet.getLayoutParams();
                int weekCalendarHeight = weekCalendarView.getHeight();
                layoutParams.height = screenHeight - weekCalendarHeight - findViewById(R.id.titlesContainer).getHeight() - 8;
                flBottomSheet.setLayoutParams(layoutParams);
                weekCalendarView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        selectedDate = LocalDate.now();
        monthCalendarView.notifyDateChanged(selectedDate);
        month_day.setText(Utils.prettyDate(selectedDate));
        week_day.setText(selectedDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()));

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, 0, 0);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Добавьте обработчик кликов по элементам меню
        navigationView.setNavigationItemSelectedListener(item -> {
            // Обработка выбора пунктов меню здесь
            // Можно добавить свою логику для каждого пункта меню
            if (item.getTitle().equals("Log in")){
                Intent intent = new Intent(MainActivity.this, LogIn.class);
                startActivity(intent);
            } else if (item.getTitle().equals("Rearrange tasks")) {
                taskList.forEach(task -> {
                    if (task.getId() == 16) {
                        task.setPlannedTime(Date.from(task.getPlannedTime().toInstant().plus(1, ChronoUnit.DAYS)));
                        monthCalendarView.updateMonthData();
                        weekCalendarView.updateWeekData();
                    }
                } );
            } else if (item.getTitle().equals("Profile")) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                intent.putExtra("UserName", currentUser.getLogin());
                intent.putExtra("UserId", currentUser.getId());
                startActivity(intent);
            }
            drawerLayout.closeDrawers();
            return true;
        });

        Bundle arguments = getIntent().getExtras();
        try {
            Long userId = arguments.getLong("UserId");
            currentUser.setId(userId);
        } catch (NullPointerException ignored){}
    }

    public void addTask(View view) {
        Intent intent = new Intent(MainActivity.this, TaskUpdate.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser.getId() > 0)
            try {
                new GetDataFromURL().execute("http://192.168.3.7:8080/antiprocrastinate-api/v1/tasks/all/" + currentUser.getId());
            } catch (NullPointerException ignored){}
        monthCalendarView.updateMonthData();
        weekCalendarView.updateWeekData();
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
        ImageView progressCircle;
        WeekDay day;

        public WeekDayViewContainer(@NonNull View view) {
            super(view);
            this.view = view;
            textView = view.findViewById(R.id.calendarDayText);
            progressCircle = view.findViewById(R.id.progress_circle);
        }

        public void setOnClickListener(WeekCalendarView weekCalendarView) {
            view.setOnClickListener(v -> {
                if (day.getPosition() == WeekDayPosition.RangeDate) {
                    LocalDate currentSelection = selectedDate;
                    if (currentSelection == day.getDate()) {
                        monthCalendarView.notifyDateChanged(currentSelection);
                        weekCalendarView.notifyDateChanged(currentSelection);
                        selectedDate = LocalDate.now();
                        month_day.setText(Utils.prettyDate(currentSelection));
                        week_day.setText(currentSelection.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()));
                        monthCalendarView.notifyDateChanged(currentSelection);
                        weekCalendarView.notifyDateChanged(currentSelection);
                    } else {
                        selectedDate = day.getDate();
                        month_day.setText(Utils.prettyDate(selectedDate));
                        week_day.setText(selectedDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()));
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
        ImageView progressCircle;
        CalendarDay day;

        public DayViewContainer(@NonNull View view) {
            super(view);
            this.view = view;
            textView = view.findViewById(R.id.calendarDayText);
            progressCircle = view.findViewById(R.id.progress_circle);
        }

        public void setOnClickListener(CalendarView monthCalendarView) {
            view.setOnClickListener(v -> {
                if (day.getPosition() == DayPosition.MonthDate) {
                    LocalDate currentSelection = selectedDate;
                    if (currentSelection == day.getDate()) {
                        monthCalendarView.notifyDateChanged(currentSelection);
                        weekCalendarView.notifyDateChanged(currentSelection);
                        selectedDate = LocalDate.now();
                        month_day.setText(Utils.prettyDate(currentSelection));
                        week_day.setText(currentSelection.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()));
                        monthCalendarView.notifyDateChanged(currentSelection);
                        weekCalendarView.notifyDateChanged(currentSelection);
                    } else {
                        selectedDate = day.getDate();
                        month_day.setText(Utils.prettyDate(selectedDate));
                        week_day.setText(selectedDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()));
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

    private List<Task> getTasksForDate(LocalDate date) {
        List<Task> tasksForDate = new ArrayList<>();
        for (Task task : taskList) {
            Date date1 = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
            if (task.getPlannedTime().getYear() == date1.getYear() &&
                task.getPlannedTime().getMonth() == date1.getMonth() &&
                task.getPlannedTime().getDate() == date1.getDate()
            ){
                tasksForDate.add(task);
            }
        }
        return tasksForDate;
    }

    private class GetDataFromURL extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            return Utils.backgroundTask(strings);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                taskList.clear();
                adapter.open();
                JSONArray array = new JSONArray(result);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject jsonTask = array.getJSONObject(i);
                    if (currentUser.getLogin() == null){
                        currentUser = Utils.parseUserJsonObject(jsonTask.getJSONObject("userId"));
                    }
                    Task task = Utils.parseTaskJsonObject(jsonTask);
                    if (!taskList.contains(task)){
                        taskList.add(task);
                        try {
                            adapter.addTask(task);
                        } catch (SQLiteConstraintException ignored) {}
                    }
                }
                weekCalendarView.updateWeekData();
                monthCalendarView.updateMonthData();

                FileOutputStream fos = null;
                try {
                    String text = currentUser.getId() + ":" + currentUser.getLogin() + ":" + currentUser.getPassword();
                    fos = openFileOutput("profile.txt", MODE_PRIVATE);
                    fos.write(text.getBytes());
                }
                catch(IOException ex) {
                    Toast.makeText(MainActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                }
                finally{
                    try{
                        if(fos!=null)
                            fos.close();
                    }
                    catch(IOException ex){
                        Toast.makeText(MainActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                for (Task task :
                        taskList) {
                    if (task.getUserId().getId() != currentUser.getId()) {
                        adapter.deleteTask(task.getId());
                    }
                }
                monthCalendarView.updateMonthData();
                weekCalendarView.updateWeekData();
                adapter.close();
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

}