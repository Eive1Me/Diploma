package com.example.diploma;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;

import com.example.diploma.local_database.DatabaseAdapter;
import com.example.diploma.model.Task;
import com.example.diploma.model.User;
import com.github.florent37.singledateandtimepicker.dialog.DoubleDateAndTimePickerDialog;
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskUpdate extends AppCompatActivity {
    DatabaseAdapter dataAdapter;
    private long taskId = 0;
    private TextView name;
    private TextView desc;
    private Button delButton;
    private Button saveButton;
    private Spinner prioritySpinner;
    private Spinner categorySpinner;
    private Spinner statusSpinner;
    private TextView deadline;
    private Button selectDeadlineTime;
    private TextView planned;
    private Button selectPlannedTime;
    private Switch completed;
    private String taskName;
    private String taskDesc;
    private Long userId = 0L;
    private Date selectedDeadlineDate = Date.from(Instant.now());
    private Date selectedPlannedDate = Date.from(Instant.now());
    private Long selectedPriority = 2L;
    private Long selectedStatus = 3L;
    private Long selectedCategory = 1L;
    private Date completeTime = null;
    private Task task;
    private final String reqUrl = "http://192.168.3.7:8080/antiprocrastinate-api/v1/tasks";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_update);

        dataAdapter = new DatabaseAdapter(this);

        name = findViewById(R.id.name);
        desc = findViewById(R.id.desc);
        selectDeadlineTime = findViewById(R.id.deadline_time_button);
        selectPlannedTime = findViewById(R.id.planned_time_button);
        prioritySpinner = findViewById(R.id.priority_spinner);
        categorySpinner = findViewById(R.id.category_spinner);
        statusSpinner = findViewById(R.id.status_spinner);
        delButton = findViewById(R.id.delete_button);
        saveButton = findViewById(R.id.save_button);
        completed = findViewById(R.id.switch1);
        deadline = findViewById(R.id.deadline_time);
        planned = findViewById(R.id.planned_time);

        selectDeadlineTime.setOnClickListener(view -> new SingleDateAndTimePickerDialog.Builder(TaskUpdate.this)
                .bottomSheet()
                .minutesStep(15)
                .curved()
                .title("Select deadline date")
                .listener(date -> {
                    selectedDeadlineDate = date;
                    deadline.setText(Utils.getBeautifulDate(date));
                })
                .display()
        );

        selectPlannedTime.setOnClickListener(view -> new SingleDateAndTimePickerDialog.Builder(TaskUpdate.this)
                .bottomSheet()
                .minutesStep(15)
                .curved()
                .title("Select planned date")
                .listener(date -> {
                    selectedPlannedDate = date;
                    planned.setText(Utils.getBeautifulDate(date));
                })
                .display()
        );

        ArrayAdapter<CharSequence> priorityAdapter = ArrayAdapter.createFromResource(this,
                R.array.priority_options, android.R.layout.simple_spinner_item);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        prioritySpinner.setAdapter(priorityAdapter);

        prioritySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPriority = (long) position + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(this,
                R.array.status_options, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        statusSpinner.setAdapter(statusAdapter);

        statusSpinner.post(() -> statusSpinner.setSelection(2));

        statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedStatus = (long) position + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(this,
                R.array.category_options, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        categorySpinner.setAdapter(categoryAdapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = (long) position + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        completed.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                completeTime = Date.from(Instant.now());
            } else {
                completeTime = null;
            }
        });

        String deadlineStr = null, plannedStr = null;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            taskId = extras.getLong("id");
            userId = extras.getLong("UserId");
            deadlineStr = extras.getString("DeadlineDate");
            plannedStr = extras.getString("PlannedDate");
        }
        if (deadline != null) {
            selectedDeadlineDate = Utils.parseStringToDate(deadlineStr);
            selectedPlannedDate = Utils.parseStringToDate(plannedStr);
        }
        if (taskId > 0) {
            dataAdapter.open();
            task = dataAdapter.getTaskById(taskId);
            dataAdapter.close();

            name.setText(task.getName());
            desc.setText(task.getDesc());

            selectedDeadlineDate = task.getDeadlineTime();
            selectedPlannedDate = task.getPlannedTime();

            int statusId = (int) task.getStatusId().getId();
            statusSpinner.post(() -> statusSpinner.setSelection(statusId - 1));
            selectedStatus = task.getStatusId().getId();

            int priorityId = (int) task.getPriorityId().getId();
            prioritySpinner.setSelection(priorityId - 1);
            selectedPriority = task.getPriorityId().getId();

            int categoryId = (int) task.getCategoryId().getId();
            categorySpinner.setSelection(categoryId - 1);
            selectedCategory = task.getCategoryId().getId();

            completed.setChecked(task.getCompleteTime() != null);
        } else {
            delButton.setVisibility(View.GONE);
        }

        deadline.setText(Utils.getBeautifulDate(selectedDeadlineDate));
        planned.setText(Utils.getBeautifulDate(selectedPlannedDate));

        saveButton.setOnClickListener(view -> {
            //TODO check that it has a name
            taskName = name.getText().toString();
            taskDesc = desc.getText().toString();
            String param = "";
            if (taskId > 0) param = "/" + taskId;
            new sendTaskFromURL().execute(reqUrl + param);
        });

        delButton.setOnClickListener(view -> {
            String param = "";
            if (taskId > 0) param = "/" + taskId;
            new sendTaskFromURL().execute(reqUrl + param, "del");
            dataAdapter.open();
            dataAdapter.deleteTask(taskId);
            dataAdapter.close();
        });

    }

    private class sendTaskFromURL extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();

                // Создание тела запроса
                JSONObject requestBodyJson = new JSONObject();

                if (strings.length > 1 && (taskId > 0)) {
                    connection.setRequestMethod("DELETE");
                } else if (taskId > 0) {
                    connection.setRequestMethod("PUT");
                    requestBodyJson.put("id", taskId);
                } else {
                    connection.setRequestMethod("POST");
                }

                connection.setDoOutput(true); // Разрешить отправку данных

                if (strings.length == 1) {

                    requestBodyJson.put("userId", userId);
                    requestBodyJson.put("name", taskName);
                    requestBodyJson.put("categoryId", selectedCategory);
                    requestBodyJson.put("priorityId", selectedPriority);
                    requestBodyJson.put("statusId", selectedStatus);
                    requestBodyJson.put("desc", taskDesc);
                    requestBodyJson.put("plannedTime", Utils.parseDateToString(selectedPlannedDate));
                    requestBodyJson.put("deadlineTime", Utils.parseDateToString(selectedDeadlineDate));
                    requestBodyJson.put("completeTime", Utils.parseDateToString(completeTime));

                    String requestBody = requestBodyJson.toString();

                    // Установка типа контента и длины тела запроса
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Content-Length", String.valueOf(requestBody.length()));

                    connection.connect();

                    // Отправка данных
                    OutputStream outputStream = connection.getOutputStream();
                    outputStream.write(requestBody.getBytes());
                    outputStream.flush();

                } else connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuilder buffer = new StringBuilder();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }
                return buffer.toString();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            } finally {
                if (connection != null)
                    connection.disconnect();
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Intent intent = new Intent(TaskUpdate.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }
    }
}
