package com.soutvoid.ProjectSozy;

import android.app.*;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.IBinder;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by andrew on 28.03.15.
 */
public class SyncService extends Service {

    //TODO реализовать отмену загрузки/выгрузки

    public static Context context;

    SQLiteDatabase db;
    SQLiteOpenProfiles dbOpen;

    boolean isUpload = true;
    boolean isFile = true;
    String LocalPath;
    String RemotePath;
    int id;

    ExecutorService executorService;

    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();

        dbOpen = new SQLiteOpenProfiles(this);
        try {
            db = dbOpen.getWritableDatabase();
        } catch (SQLiteException e) {
            e.printStackTrace();
            db = dbOpen.getReadableDatabase();
        }


    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (intent.hasExtra("reason"))
        if (intent.getStringExtra("reason").equals("createProfile")) {
            isUpload = intent.getBooleanExtra("isUpload", true);
            isFile = intent.getBooleanExtra("isFile", false);
            LocalPath = intent.getStringExtra("local");
            RemotePath = intent.getStringExtra("remote");
            id = intent.getIntExtra("id", 0);
            createProfile();
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                syncProfiles();
            }
        });
        thread.start();

        return START_NOT_STICKY;
    }

    public void onDestroy() {
        super.onDestroy();
    }

    private void createProfile() {
        dbOpen = new SQLiteOpenProfiles(this);
        try {
            db = dbOpen.getWritableDatabase();
        } catch (SQLiteException e) {
            e.printStackTrace();
            db = dbOpen.getReadableDatabase();
        }
        dbOpen.createTable(db, "profile" + id);
        ContentValues newValues;
        final Profile profile = new Profile(id);
        profile.dbOpen = dbOpen;
        profile.db = db;
        if (isUpload) {
            if (new File(LocalPath).isDirectory()) {
                profile.rescanLocal(LocalPath, "");
            } else {
                newValues = new ContentValues();
                newValues.put("path", LocalPath);
                db.insert("profile" + id, null, newValues);
            }
        } else {
            if (isFile) {
                newValues = new ContentValues();
                newValues.put("path", RemotePath);
                db.insert("profile" + id, null, newValues);
            } else {
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                            profile.openConnection();
                            profile.ftpClient.changeWorkingDirectory(RemotePath);
                            profile.rescanFTP("");
                            profile.closeConnection();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                thread.start();
            }
        }
    }

    private void syncProfiles() {
        dbOpen = new SQLiteOpenProfiles(this);
        try {
            db = dbOpen.getWritableDatabase();
        } catch (SQLiteException e) {
            e.printStackTrace();
            db = dbOpen.getReadableDatabase();
        }
        Cursor cursor = db.query("profiles", new String[] {"_id"}, null, null, null, null, null);
        cursor.moveToFirst();
        Profile profile;
        for (int i = 0; i < cursor.getCount(); i++) {
            profile = new Profile(cursor.getInt(0));
            profile.sync();
            if (!cursor.isLast()) cursor.moveToNext();
        }
    }

    public void sendTextOnNotif(String input, int id) {
        Context context = SyncService.this;

        Notification.Builder builder = new Notification.Builder(context);


        builder
                .setSmallIcon(R.drawable.ic_notif)
                .setTicker(input)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(input)
                .setContentText(input)
                .setAutoCancel(true);


        Notification notification = builder.build();

        NotificationManager notificationManager = (NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(id, notification);
    }

    public IBinder onBind(Intent intent) {
        return null;
    }


}
