package com.soutvoid.ProjectSozy;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.IBinder;

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

    Profile profile;

    int currentDay;
    int currentTime;

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

        executorService = Executors.newFixedThreadPool(2);

        checkProfiles();

        stopSelf();

        return START_NOT_STICKY;
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public void checkProfiles() {

        final Cursor data = db.query("profiles", new String[]{"_id", "daynumber", "time"}, null, null, null, null, null, null);
        data.moveToFirst();

        final Calendar calendar = Calendar.getInstance();   //номер понедельника - 2

        currentDay = calendar.get(Calendar.DAY_OF_WEEK) == 1?6:calendar.get(Calendar.DAY_OF_WEEK) - 2;
        currentTime = calendar.get(Calendar.HOUR_OF_DAY)*60 + calendar.get(Calendar.MINUTE) - calendar.get(Calendar.MINUTE)%10;
        sendTextOnNotif(currentDay + " + " + currentTime, 1);

        for (int i = 0; i < data.getCount(); i++) {
            sendTextOnNotif( data.getInt(1) + " + " + data.getInt(2), 2);
            if (data.getInt(1) == currentDay && data.getInt(2) == currentTime) {
                profile = new Profile(data.getInt(0));
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        profile.startProfile();
                    }
                });
            }
            data.moveToNext();
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
