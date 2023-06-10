package com.example.diploma;

import com.example.diploma.model.Category;
import com.example.diploma.model.Group;
import com.example.diploma.model.Priority;
import com.example.diploma.model.Task;
import com.example.diploma.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Date;
import java.util.Locale;

public class Utils {

    public static Task parseTaskJsonObject(JSONObject jsonTask) {
        Task task = new Task();
        try {
            task.setId(jsonTask.getLong("id"));
            JSONObject jsonUser = jsonTask.getJSONObject("userId");
            task.setUserId(parseUserJsonObject(jsonUser));
            task.setName(jsonTask.getString("name"));
            JSONObject jsonCateg = jsonTask.getJSONObject("categoryId");
            JSONObject jsonCatUs = jsonCateg.getJSONObject("userId");
            task.setCategoryId(new Category(
                    jsonCateg.getLong("id"),
                    parseUserJsonObject(jsonCatUs),
                    jsonCateg.getString("name"),
                    jsonCateg.getString("desc"),
                    jsonCateg.getString("colour")
            ));
            JSONObject jsonPriority = jsonTask.getJSONObject("priorityId");
            task.setPriorityId(new Priority(jsonPriority.getLong("id"), jsonPriority.getString("value")));
            task.setPlannedTime(parseStringToDate(jsonTask.getString("plannedTime")));
            task.setDeadlineTime(parseStringToDate(jsonTask.getString("deadlineTime")));
            task.setDesc(jsonTask.getString("desc"));
            JSONObject jsonGroup = jsonTask.getJSONObject("groupId");
            JSONObject jsonGrUs = jsonGroup.getJSONObject("userId");
            task.setGroupId(new Group(
                    jsonGroup.getLong("id"),
                    jsonGroup.getString("name"),
                    parseUserJsonObject(jsonGrUs)
            ));
            JSONObject jsonStatus = jsonTask.getJSONObject("statusId");
            task.setStatusId(new com.example.diploma.model.Status(jsonStatus.getLong("id"), jsonStatus.getString("value")));
            task.setCompleteTime(parseStringToDate(jsonTask.getString("completeTime")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return task;
    }

    public static User parseUserJsonObject(JSONObject jsonUser){
        User user = new User();
        try {
            user = new User(jsonUser.getLong("id"), jsonUser.getString("login"), jsonUser.getString("password"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return user;
    }

    public static Date parseStringToDate(String dateString) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        try {
            return format.parse(dateString);
        } catch (ParseException | NullPointerException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String parseDateToString(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        if (date == null)
            return null;
        return format.format(date);
    }

    public static float calculateAlpha(int viewTop, int maxHeight) {
        // В этом примере, я просто использую линейную интерполяцию, чтобы прозрачность изменялась равномерно в зависимости от положения представления.
        float alphaPercentage = (float) viewTop / maxHeight;
        return 1f - alphaPercentage; // Инвертируем значение, чтобы прозрачность увеличивалась с движением вверх.
    }

    public static String prettyDate(LocalDate date){
        return date.getDayOfMonth() + " " + date.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
    }

    public static LocalDate convertToLocalDateViaInstant(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    public static Date convertToDateViaInstant(LocalDate dateToConvert) {
        return java.util.Date.from(dateToConvert.atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }

}