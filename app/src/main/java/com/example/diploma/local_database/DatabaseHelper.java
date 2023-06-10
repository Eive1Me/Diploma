package com.example.diploma.local_database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "mydatabase.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TASK_TABLE = "TASK_TABLE";
    public static final String USER_TABLE = "USER_TABLE";
    public static final String CATEGORY_TABLE = "CATEGORY_TABLE";
    public static final String PRIORITY_TABLE = "PRIORITY_TABLE";
    public static final String GROUP_TABLE = "GROUP_TABLE";
    public static final String STATUS_TABLE = "STATUS_TABLE";

    // Конструктор
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Создание таблицы "Task"
        String createTaskTable = "CREATE TABLE " + TASK_TABLE + "(" +
                "id INTEGER PRIMARY KEY," +
                "userId INTEGER," +
                "name TEXT," +
                "categoryId INTEGER," +
                "priorityId INTEGER," +
                "plannedTime TEXT," +
                "deadlineTime TEXT," +
                "descrip TEXT," +
                "groupId INTEGER," +
                "statusId INTEGER," +
                "completeTime TEXT" +
                ")";
        db.execSQL(createTaskTable);

        // Создание таблицы "User"
        String createUserTable = "CREATE TABLE " + USER_TABLE + "(" +
                "id INTEGER PRIMARY KEY," +
                "login TEXT," +
                "password TEXT" +
                ")";
        db.execSQL(createUserTable);

        // Создание таблицы "Category"
        String createCategoryTable = "CREATE TABLE " + CATEGORY_TABLE + "(" +
                "id INTEGER PRIMARY KEY," +
                "userId INTEGER," +
                "name TEXT," +
                "descrip TEXT," +
                "colour TEXT" +
                ")";
        db.execSQL(createCategoryTable);

        // Создание таблицы "Priority"
        String createPriorityTable = "CREATE TABLE " + PRIORITY_TABLE + "(" +
                "id INTEGER PRIMARY KEY," +
                "value TEXT" +
                ")";
        db.execSQL(createPriorityTable);

        // Создание таблицы "Group"
        String createGroupTable = "CREATE TABLE " + GROUP_TABLE + "(" +
                "id INTEGER PRIMARY KEY," +
                "name TEXT," +
                "userId INTEGER" +
                ")";
        db.execSQL(createGroupTable);

        // Создание таблицы "Status"
        String createStatusTable = "CREATE TABLE " + STATUS_TABLE + "(" +
                "id INTEGER PRIMARY KEY," +
                "value TEXT" +
                ")";
        db.execSQL(createStatusTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TASK_TABLE);
        db.execSQL("DROP TABLE IF EXISTS "+USER_TABLE);
        db.execSQL("DROP TABLE IF EXISTS "+CATEGORY_TABLE);
        db.execSQL("DROP TABLE IF EXISTS "+STATUS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS "+PRIORITY_TABLE);
        db.execSQL("DROP TABLE IF EXISTS "+GROUP_TABLE);
        onCreate(db);
    }
}
