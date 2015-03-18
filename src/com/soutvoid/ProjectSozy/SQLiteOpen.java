package com.soutvoid.ProjectSozy;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by andrew on 18.03.15.
 */
public class SQLiteOpen extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "sozy.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "profiles";

    //названия столбцов
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_ADDRESS = "address";
    private static final String COLUMN_USER = "user";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_LOCAL_PATH = "localpath";
    private static final String COLUMN_REMOTE_PATH = "remotepath";

    //номера столбцов
    private static final int NUM_COLUMN_ID = 1;
    private static final int NUM_COLUMN_ADDRESS = 2;
    private static final int NUM_COLUNM_USER = 3;
    private static final int NUM_COLUMN_PASSWORD = 4;
    private static final int NUM_COLUMN_LOCAL_PATH = 5;
    private static final int NUM_COLUMN_REMOTE_PATH = 6;

    SQLiteOpen(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_ADDRESS + " STRING," + COLUMN_USER + " STRING," + COLUMN_PASSWORD + " STRING,"
                + COLUMN_LOCAL_PATH + " STRING," + COLUMN_REMOTE_PATH + " STRING);";
        db.execSQL(query);
    }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }

}
