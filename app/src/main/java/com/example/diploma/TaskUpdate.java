package com.example.diploma;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;

import com.example.diploma.local_database.DatabaseAdapter;
import com.example.diploma.model.User;
import com.github.florent37.singledateandtimepicker.dialog.DoubleDateAndTimePickerDialog;
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskUpdate extends AppCompatActivity {
    DatabaseAdapter dataAdapter;
    private long userId=0;
    private TextView name;
    private Button selectDeadlineTime;
    private Button selectPlannedTime;
    private TextView priority;
    private TextView desc;
    private TextView deadline;
    private TextView planned;
    private TextView status;
    private TextView category;
    private Button delButton;
    private Button saveButton;
    private Spinner prioritySpinner;
    private LocalDate selectedDeadlineDate = LocalDate.now();
    private LocalDate selectedPlannedDate = LocalDate.now();
    String selectedPriority = "high";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_update);

        selectDeadlineTime = findViewById(R.id.deadline_time_button);
        selectPlannedTime = findViewById(R.id.planned_time_button);
        prioritySpinner = findViewById(R.id.priority_spinner);
        delButton = findViewById(R.id.delete_button);
        saveButton = findViewById(R.id.save_button);

        selectDeadlineTime.setOnClickListener(view -> new SingleDateAndTimePickerDialog.Builder(TaskUpdate.this)
                .bottomSheet()
                .minutesStep(15)
                .curved()
                .title("Select deadline date")
                .listener(date -> selectedDeadlineDate = Utils.convertToLocalDateViaInstant(date))
                .display()
        );

        selectPlannedTime.setOnClickListener(view -> new SingleDateAndTimePickerDialog.Builder(TaskUpdate.this)
                .bottomSheet()
                .minutesStep(15)
                .curved()
                .title("Select planned date")
                .listener(date -> selectedPlannedDate = Utils.convertToLocalDateViaInstant(date))
                .display()
        );

        // Создаем адаптер для списка вариантов приоритета
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.priority_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Применяем адаптер к Spinner
        prioritySpinner.setAdapter(adapter);

        // Обработка выбранного значения из Spinner
        prioritySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPriority = parent.getItemAtPosition(position).toString().toLowerCase(Locale.ROOT);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Ничего не делаем
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userId = extras.getLong("id");
        }
        // если 0, то добавление
        if (userId > 0) {
            // получаем элемент по id из бд
            dataAdapter.open();
            User user = dataAdapter.getUserById(userId);
            name.setText(extras.getString("name"));
            desc.setText(extras.getString("desc"));
            selectedDeadlineDate = Utils.convertToLocalDateViaInstant(Utils.parseStringToDate(extras.getString("deadline_time")));
            selectedPlannedDate = Utils.convertToLocalDateViaInstant(Utils.parseStringToDate(extras.getString("planned_time")));
            dataAdapter.close();
        } else {
            // скрываем кнопку удаления
            delButton.setVisibility(View.GONE);
        }

        saveButton.setOnClickListener(view -> {
//            Intent intent = new Intent(TaskUpdate.this, MainActivity.class);
//            startActivity(intent);
        });

    }
}
