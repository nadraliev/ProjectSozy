package com.soutvoid.ProjectSozy;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by andrew on 18.03.15.
 */
public class SQLiteOpenProfiles extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "sozy.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "profiles";

    //названия столбцов
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_ADDRESS = "address";
    private static final String COLUMN_USER = "user";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_PATH = "path";
    private static final String COLUMN_DESTINATION = "destination";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_SIZE_DIGEST = "sizedigest";

    //номера столбцов
    private static final int NUM_COLUMN_ID = 0;
    private static final int NUM_COLUMN_NAME = 1;
    private static final int NUM_COLUMN_ADDRESS = 2;
    private static final int NUM_COLUNM_USER = 3;
    private static final int NUM_COLUMN_PASSWORD = 4;
    private static final int NUM_COLUMN_LOCAL_PATH = 5;
    private static final int NUM_COLUMN_REMOTE_PATH = 6;
    private static final int NUM_COLUMN_TYPE = 7;
    private static final int NUM_COLUMN_DAYNUMBER = 8;
    private static final int NUM_COLUMN_TIME = 9;
    private static final int NUM_COLUMN_SHEDULED = 10;

    SQLiteOpenProfiles(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_NAME + " STRING," + COLUMN_ADDRESS + " STRING," + COLUMN_USER + " STRING," + COLUMN_PASSWORD + " STRING,"
                + COLUMN_PATH + " STRING," + COLUMN_DESTINATION + " STRING," + COLUMN_TYPE + " String" + ");";
        db.execSQL(query);
    }

    public void createTable(SQLiteDatabase db, final String name) {
        String query = "CREATE TABLE " + name + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_PATH + " STRING," + COLUMN_SIZE_DIGEST + " STRING);";
        db.execSQL(query);
    }

    public void dropTable(SQLiteDatabase db, final String name) {
        db.execSQL("DROP TABLE IF EXISTS " + name);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

}
