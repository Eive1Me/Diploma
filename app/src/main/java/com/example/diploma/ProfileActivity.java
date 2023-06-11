package com.example.diploma;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.diploma.local_database.DatabaseAdapter;
import com.example.diploma.model.Category;
import com.example.diploma.model.Group;
import com.example.diploma.model.Task;
import com.example.diploma.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    TextView name;
    TextView completed;
    TextView inProgress;
    TextView notStarted;
    DatabaseAdapter adapter = null;
    List<Task> taskList = new ArrayList<>();
    String currentUser = "Offline";
    Long userId = 1L;
    ListView categList = null;
    ListView gropList = null;
    List<Categ> categs = new ArrayList<>();
    List<Grop> grops = new ArrayList<>();
    GroupAdapter groupAdapter = null;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        name = findViewById(R.id.name);
        completed = findViewById(R.id.completed_percentage);
        inProgress = findViewById(R.id.in_progress_percentage);
        notStarted = findViewById(R.id.not_started_percentage);
        categList = findViewById(R.id._dynamic);
        gropList = findViewById(R.id._dynamic1);

        Bundle arguments = getIntent().getExtras();
        try {
            currentUser = arguments.getString("UserName");
            userId = arguments.getLong("UserId");
            name.setText(currentUser);
        } catch (NullPointerException ignored){}

        adapter = new DatabaseAdapter(this);
        adapter.open();
        try {
            taskList = adapter.getAllTasks();
        } catch (SQLiteException ignored) {}
        adapter.close();

        int compl = 0, progr = 0, not =0;

        Map<Long,int[]> categories = new HashMap<>();

        Map<Color, Integer> colormap = new HashMap<>();
        for (Task task : taskList) {
            if (!categories.containsKey(task.getCategoryId().getId()))
                if (task.getStatusId().getId() == 1)
                    categories.put(task.getCategoryId().getId(), new int[]{1, 0});
                else
                    categories.put(task.getCategoryId().getId(), new int[]{0, 1});
            else
                if (task.getStatusId().getId() == 1){
                    int[] arr = categories.get(task.getCategoryId().getId());
                    arr[0]++;
                    categories.put(task.getCategoryId().getId(), arr);
                }
                else{
                    int[] arr = categories.get(task.getCategoryId().getId());
                    arr[1]++;
                    categories.put(task.getCategoryId().getId(), arr);
                }

            Color taskColor;
            if (task.getStatusId().getId() == 1) {
                taskColor = Color.valueOf(Color.parseColor("#64f051"));
                compl++;
            }
            else if (task.getStatusId().getId() == 2) {
                taskColor = Color.valueOf(Color.parseColor("#ecfa73"));
                progr++;
            }
            else {
                taskColor = Color.valueOf(Color.parseColor("#d5e3e0"));
                not++;
            }
            if (!colormap.containsKey(taskColor))
                colormap.put(taskColor, 1);
            else
                colormap.put(taskColor, colormap.get(taskColor) + 1);
        }

        int totalPercentage = 0; // Общий процент, чтобы вычислить оставшийся цвет
        for (int percentage : colormap.values()) {
            totalPercentage += percentage;
        }

        float startAngle = -90;
        int width = 256; // Ширина окружности
        int height = 256; // Высота окружности

        Bitmap bitmap = Bitmap.createBitmap(width + 24, height + 24, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        RectF rectF = new RectF(24, 24, width, height);
        for (int i = 0; i < colormap.values().size(); i++) {
            int percentage = colormap.values().toArray(new Integer[]{})[i];
            int color = colormap.keySet().toArray(new Color[]{})[i].toArgb();
            float sweepAngle = (float) percentage / totalPercentage * 360;

            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(24);
            paint.setColor(color);

            canvas.drawArc(rectF, startAngle, sweepAngle, false, paint);

            startAngle += sweepAngle;
        }

        Drawable drawable = new BitmapDrawable(name.getResources(), bitmap);
        drawable.setAlpha(180);
        name.setBackground(drawable);

        completed.setText(String.format(Locale.getDefault(),"%.2f",(double) compl/taskList.size() * 100).concat("%"));
        inProgress.setText(String.format(Locale.getDefault(),"%.2f",(double) progr/taskList.size() * 100).concat("%"));
        notStarted.setText(String.format(Locale.getDefault(),"%.2f", (double) not/taskList.size() * 100).concat("%"));

        adapter.open();

        for (Long cat : categories.keySet()) {
            int[] nums = categories.get(cat);
            Category category = adapter.getCategoryById(cat);
            categs.add(new Categ(category.getName(), (double) nums[1]/(nums[0] + nums[1]) * 100, category.getColour()));
        }

        adapter.close();

        categs.sort(Comparator.comparing(categ -> categ.perc));

        categList.setAdapter(new CategoryAdapter(this, R.layout.categ_layout, categs));
        groupAdapter = new GroupAdapter(this, R.layout.group_layout, grops);
        gropList.setAdapter(groupAdapter);

        new GetGroupsFromURL().execute("http://192.168.3.7:8080/antiprocrastinate-api/v1/groups/all/" + userId);

    }

    private class Categ {
        String name;
        Double perc;
        String color;

        public Categ(String name, Double perc, String color){
            this.name = name;
            this.perc = perc;
            this.color = color;
        }

        @Override
        public String toString(){
            return name + " : " + perc;
        }
    }

    public class CategoryAdapter extends ArrayAdapter<Categ> {
        private LayoutInflater inflater;
        private int layout;
        private List<Categ> categList;


        public CategoryAdapter(Context context, int resource, List<Categ> categList) {
            super(context, resource, categList);
            this.inflater = LayoutInflater.from(context);
            this.layout = resource;
            this.categList = categList;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = inflater.inflate(this.layout, parent, false);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            Categ categ = categList.get(position);

            viewHolder.name.setText(categ.name);
            viewHolder.percentage.setText(String.format(Locale.getDefault(), "%.2f", categ.perc).concat("%"));
            viewHolder.verticalLine.setBackgroundColor(Color.parseColor(categ.color));

            return convertView;
        }

        private class ViewHolder {
            final TextView name;
            final TextView percentage;
            public View verticalLine;

            ViewHolder(View view) {
                name = view.findViewById(R.id.categ_name);
                percentage = view.findViewById(R.id.percentage);
                verticalLine = view.findViewById(R.id.verticalLine);
            }
        }
    }

    public class Grop {
        String name;
        int numPpl;

        public Grop(String name, int numPpl) {
            this.name = name;
            this.numPpl = numPpl;
        }
    }

    public class GroupAdapter extends ArrayAdapter<Grop> {
        private LayoutInflater inflater;
        private int layout;
        private List<Grop> groupList;

        public GroupAdapter(Context context, int resource, List<Grop> objects) {
            super(context, resource, objects);
            this.inflater = LayoutInflater.from(context);
            this.layout = resource;
            this.groupList = objects;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = inflater.inflate(this.layout, parent, false);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            //Полученный объект
            Grop grop = groupList.get(position);

            viewHolder.name.setText(grop.name);
            viewHolder.numPpl.setText(String.valueOf(grop.numPpl).concat(" ppl"));

            return convertView;
        }

        private class ViewHolder {
            final TextView name;
            final TextView numPpl;

            ViewHolder(View view) {
                name = view.findViewById(R.id.group_name);
                numPpl = view.findViewById(R.id.num_ppl);
            }
        }
    }

    private class GetGroupsFromURL extends AsyncTask<String, String, String> {
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
                List<Group> groups = new ArrayList<>();
                JSONArray array = new JSONArray(result);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject jsonGroup = array.getJSONObject(i);
                    Group group = Utils.parseGroupJsonObject(jsonGroup);
                    groups.add(group);
                    new GetUserAmountFromURL().execute("http://192.168.3.7:8080/antiprocrastinate-api/v1/users/all/" + group.getId(), group.getName());
                }
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    private class GetUserAmountFromURL extends AsyncTask<String, String, String> {
        String name;
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            name = strings[1];
            return Utils.backgroundTask(strings);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                List<User> users = new ArrayList<>();
                JSONArray array = new JSONArray(result);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject jsonUser = array.getJSONObject(i);
                    User user = Utils.parseUserJsonObject(jsonUser);
                    users.add(user);
                }
                grops.add(new Grop(name, users.size()));
                groupAdapter.notifyDataSetChanged();
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

}