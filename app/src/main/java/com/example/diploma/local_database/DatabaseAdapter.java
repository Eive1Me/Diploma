package com.example.diploma.local_database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.example.diploma.Utils;
import com.example.diploma.model.Category;
import com.example.diploma.model.Group;
import com.example.diploma.model.Priority;
import com.example.diploma.model.Status;
import com.example.diploma.model.Task;
import com.example.diploma.model.User;

import java.util.ArrayList;
import java.util.List;

public class DatabaseAdapter {
    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    public DatabaseAdapter(Context context){
        dbHelper = new DatabaseHelper(context.getApplicationContext());
    }

    public DatabaseAdapter open(){
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        dbHelper.close();
    }

    @SuppressLint("Range")
    public List<Task> getAllTasks() {
        List<Task> taskList = new ArrayList<>();
        Cursor cursor = database.query("TASK_TABLE", null, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Task task = new Task();
                task.setId(cursor.getInt(cursor.getColumnIndex("id")));
                task.setUserId(getUserById(cursor.getInt(cursor.getColumnIndex("userId"))));
                task.setName(cursor.getString(cursor.getColumnIndex("name")));
                task.setCategoryId(getCategoryById(cursor.getInt(cursor.getColumnIndex("categoryId"))));
                task.setPriorityId(getPriorityById(cursor.getInt(cursor.getColumnIndex("priorityId"))));
                task.setPlannedTime(Utils.parseStringToDate(cursor.getString(cursor.getColumnIndex("plannedTime"))));
                task.setDeadlineTime(Utils.parseStringToDate(cursor.getString(cursor.getColumnIndex("deadlineTime"))));
                task.setDesc(cursor.getString(cursor.getColumnIndex("descrip")));
                task.setGroupId(getGroupById(cursor.getInt(cursor.getColumnIndex("groupId"))));
                task.setStatusId(getStatusById(cursor.getInt(cursor.getColumnIndex("statusId"))));
                task.setCompleteTime(Utils.parseStringToDate(cursor.getString(cursor.getColumnIndex("completeTime"))));

                taskList.add(task);
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return taskList;
    }

    public void addTask(Task task) {
        ContentValues values = new ContentValues();
        values.put("id", task.getId());
        values.put("userId", task.getUserId().getId());
        values.put("name", task.getName());
        values.put("categoryId", task.getCategoryId().getId());
        values.put("priorityId", task.getPriorityId().getId());
        values.put("plannedTime", Utils.parseDateToString(task.getPlannedTime())); // Предполагается, что методы getPlannedTime() и getTime() возвращают объект типа Date и его значение в миллисекундах
        values.put("deadlineTime", Utils.parseDateToString(task.getDeadlineTime()));
        values.put("descrip", task.getDesc());
        if (task.getGroupId() != null) {
            values.put("groupId", task.getGroupId().getId());
        } else {
            values.put("groupId", (Long) null);
        }
        values.put("statusId", task.getStatusId().getId());
        values.put("completeTime", Utils.parseDateToString(task.getCompleteTime()));

        if (!isUserExists(task.getUserId().getId()))
            addUser(task.getUserId());
        if (task.getGroupId() != null && !isGroupExists(task.getGroupId().getId()))
            addGroup(task.getGroupId());
        if (!isCategoryExists(task.getCategoryId().getId()))
            addCategory(task.getCategoryId());
        if (!isPriorityExists(task.getPriorityId().getId()))
            addPriority(task.getPriorityId());
        if (!isStatusExists(task.getStatusId().getId()))
            addStatus(task.getStatusId());
        if (!isTaskExists(task.getId())) {
            System.out.println(task.getName());
            database.insert("TASK_TABLE", null, values);
        } else {
            deleteTask(task.getId());
            database.insert("TASK_TABLE", null, values);
        }
    }

    private boolean isTaskExists(long taskId) {
        Cursor cursor = database.query("TASK_TABLE", null, "id = ?", new String[]{String.valueOf(taskId)}, null, null, null);
        boolean exists = cursor != null && cursor.moveToFirst();
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }

    @SuppressLint("Range")
    public Task getTaskById(int taskId) {
        Cursor cursor = database.query("TASK_TABLE", null, "id = ?", new String[]{String.valueOf(taskId)}, null, null, null);
        Task task = null;
        if (cursor != null && cursor.moveToFirst()) {
            task = new Task();
            task.setId(cursor.getInt(cursor.getColumnIndex("id")));
            task.setUserId(getUserById(cursor.getInt(cursor.getColumnIndex("userId"))));
            task.setName(cursor.getString(cursor.getColumnIndex("name")));
            task.setCategoryId(getCategoryById(cursor.getInt(cursor.getColumnIndex("categoryId"))));
            task.setPriorityId(getPriorityById(cursor.getInt(cursor.getColumnIndex("priorityId"))));
            task.setPlannedTime(Utils.parseStringToDate(cursor.getString(cursor.getColumnIndex("plannedTime"))));
            task.setDeadlineTime(Utils.parseStringToDate(cursor.getString(cursor.getColumnIndex("deadlineTime"))));
            task.setDesc(cursor.getString(cursor.getColumnIndex("descrip")));
            task.setGroupId(getGroupById(cursor.getInt(cursor.getColumnIndex("groupId"))));
            task.setStatusId(getStatusById(cursor.getInt(cursor.getColumnIndex("statusId"))));
            task.setCompleteTime(Utils.parseStringToDate(cursor.getString(cursor.getColumnIndex("completeTime"))));
        }
        if (cursor != null) {
            cursor.close();
        }
        return task;
    }

    public void deleteTask(long taskId) {
        database.delete("TASK_TABLE", "id = ?", new String[]{String.valueOf(taskId)});
    }

    public void addUser(User user) {
        ContentValues values = new ContentValues();
        values.put("id", user.getId());
        values.put("login", user.getLogin());
        values.put("password", user.getPassword());

        database.insert("USER_TABLE", null, values);
    }

    private boolean isUserExists(long userId) {
        Cursor cursor = database.query("USER_TABLE", null, "id = ?", new String[]{String.valueOf(userId)}, null, null, null);
        boolean exists = cursor != null && cursor.moveToFirst();
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }

    @SuppressLint("Range")
    public User getUserById(long userId) {
        Cursor cursor = database.query("USER_TABLE", null, "id = ?", new String[]{String.valueOf(userId)}, null, null, null);
        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = new User();
            user.setId(cursor.getInt(cursor.getColumnIndex("id")));
            user.setLogin(cursor.getString(cursor.getColumnIndex("login")));
            user.setPassword(cursor.getString(cursor.getColumnIndex("password")));
        }
        if (cursor != null) {
            cursor.close();
        }
        return user;
    }

    public void deleteUser(int userId) {
        database.delete("USER_TABLE", "id = ?", new String[]{String.valueOf(userId)});
    }

    public void addCategory(Category category) {
        ContentValues values = new ContentValues();
        values.put("id", category.getId());
        values.put("userId", category.getUserId().getId());
        values.put("name", category.getName());
        values.put("descrip", category.getDesc());
        values.put("colour", category.getColour());

        database.insert("CATEGORY_TABLE", null, values);
    }

    private boolean isCategoryExists(long categoryId) {
        Cursor cursor = database.query("CATEGORY_TABLE", null, "id = ?", new String[]{String.valueOf(categoryId)}, null, null, null);
        boolean exists = cursor != null && cursor.moveToFirst();
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }

    @SuppressLint("Range")
    public Category getCategoryById(long categoryId) {
        Cursor cursor = database.query("CATEGORY_TABLE", null, "id = ?", new String[]{String.valueOf(categoryId)}, null, null, null);
        Category category = null;
        if (cursor != null && cursor.moveToFirst()) {
            category = new Category();
            category.setId(cursor.getInt(cursor.getColumnIndex("id")));
            category.setUserId(getUserById(cursor.getInt(cursor.getColumnIndex("userId"))));
            category.setName(cursor.getString(cursor.getColumnIndex("name")));
            category.setDesc(cursor.getString(cursor.getColumnIndex("descrip")));
            category.setColour(cursor.getString(cursor.getColumnIndex("colour")));
        }
        if (cursor != null) {
            cursor.close();
        }
        return category;
    }

    public void deleteCategory(int categoryId) {
        database.delete("CATEGORY_TABLE", "id = ?", new String[]{String.valueOf(categoryId)});
    }

    public void addPriority(Priority priority) {
        ContentValues values = new ContentValues();
        values.put("id", priority.getId());
        values.put("value", priority.getValue());

        database.insert("PRIORITY_TABLE", null, values);
    }

    private boolean isPriorityExists(long priorityId) {
        Cursor cursor = database.query("PRIORITY_TABLE", null, "id = ?", new String[]{String.valueOf(priorityId)}, null, null, null);
        boolean exists = cursor != null && cursor.moveToFirst();
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }

    @SuppressLint("Range")
    public Priority getPriorityById(int priorityId) {
        Cursor cursor = database.query("PRIORITY_TABLE", null, "id = ?", new String[]{String.valueOf(priorityId)}, null, null, null);
        Priority priority = null;
        if (cursor != null && cursor.moveToFirst()) {
            priority = new Priority();
            priority.setId(cursor.getInt(cursor.getColumnIndex("id")));
            priority.setValue(cursor.getString(cursor.getColumnIndex("value")));
        }
        if (cursor != null) {
            cursor.close();
        }
        return priority;
    }

    public void deletePriority(int priorityId) {
        database.delete("PRIORITY_TABLE", "id = ?", new String[]{String.valueOf(priorityId)});
    }

    public void addGroup(Group group) {
        ContentValues values = new ContentValues();
        values.put("id", group.getId());
        values.put("name", group.getName());
        values.put("userId", group.getUserId().getId());

        database.insert("GROUP_TABLE", null, values);
    }

    private boolean isGroupExists(long groupId) {
        Cursor cursor = database.query("GROUP_TABLE", null, "id = ?", new String[]{String.valueOf(groupId)}, null, null, null);
        boolean exists = cursor != null && cursor.moveToFirst();
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }

    @SuppressLint("Range")
    public Group getGroupById(int groupId) {
        Cursor cursor = database.query("GROUP_TABLE", null, "id = ?", new String[]{String.valueOf(groupId)}, null, null, null);
        Group group = null;
        if (cursor != null && cursor.moveToFirst()) {
            group = new Group();
            group.setId(cursor.getInt(cursor.getColumnIndex("id")));
            group.setName(cursor.getString(cursor.getColumnIndex("name")));
            group.setUserId(getUserById(cursor.getInt(cursor.getColumnIndex("userId"))));
        }
        if (cursor != null) {
            cursor.close();
        }
        return group;
    }

    public void deleteGroup(int groupId) {
        database.delete("GROUP_TABLE", "id = ?", new String[]{String.valueOf(groupId)});
    }

    public void addStatus(Status status) {
        ContentValues values = new ContentValues();
        values.put("id", status.getId());
        values.put("value", status.getValue());

        database.insert("STATUS_TABLE", null, values);
    }

    private boolean isStatusExists(long statusId) {
        Cursor cursor = database.query("STATUS_TABLE", null, "id = ?", new String[]{String.valueOf(statusId)}, null, null, null);
        boolean exists = cursor != null && cursor.moveToFirst();
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }

    @SuppressLint("Range")
    public Status getStatusById(int statusId) {
        Cursor cursor = database.query("STATUS_TABLE", null, "id = ?", new String[]{String.valueOf(statusId)}, null, null, null);
        Status status = null;
        if (cursor != null && cursor.moveToFirst()) {
            status = new Status();
            status.setId(cursor.getInt(cursor.getColumnIndex("id")));
            status.setValue(cursor.getString(cursor.getColumnIndex("value")));
        }
        if (cursor != null) {
            cursor.close();
        }
        return status;
    }

    public void deleteStatus(int statusId) {
        database.delete("STATUS_TABLE", "id = ?", new String[]{String.valueOf(statusId)});
    }

}
