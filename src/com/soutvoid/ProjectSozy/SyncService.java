package com.soutvoid.ProjectSozy;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by andrew on 28.03.15.
 */
public class SyncService extends Service {

    //TODO реализовать отмену загрузки/выгрузки


    SQLiteDatabase db;
    SQLiteOpen dbOpen;

    Integer id;

    ArrayList<Integer[]> delays;
    TimerTask timerTask;

    int currentDay;

    public void onCreate() {
        super.onCreate();

        dbOpen = new SQLiteOpen(this);
        try {
            db = dbOpen.getWritableDatabase();
        } catch (SQLiteException e) {
            e.printStackTrace();
            db = dbOpen.getReadableDatabase();
        }



    }

    public int onStartCommand(Intent intent, int flags, int startId) {

        updateTimers();


        return START_REDELIVER_INTENT;
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public void updateTimers() {

        final Cursor data = db.query("profiles", new String[] {"_id", "daynumber", "time"}, null, null, null, null, null, null);
        data.moveToFirst();
        try {
            id = data.getInt(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        final Calendar calendar = Calendar.getInstance();   //номер понедельника - 2

        Integer delay;
        Timer timer = new Timer();

        delays = new ArrayList<Integer[]>();   //здесь хранятся кол-во минут до начала профилей и id, им соответствующие

        currentDay = calendar.get(Calendar.DAY_OF_WEEK) == 1?6:calendar.get(Calendar.DAY_OF_WEEK) - 2;

        for (int i = 0; i < data.getCount(); i++) {    //ищем задержку для каждого  профиля
            if (currentDay*24*60 + calendar.get(Calendar.HOUR_OF_DAY)*60 + calendar.get(Calendar.MINUTE) < data.getInt(1)*24*60 + data.getInt(2)) {
                if (currentDay == data.getInt(1)) {
                    delay = data.getInt(2) - (calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE));
                } else
                    delay = 24 * 60 - (calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)) + 24 * 60 * (data.getInt(1) - currentDay) + data.getInt(2);
            } else if (currentDay*24*60 + calendar.get(Calendar.HOUR_OF_DAY)*60 + calendar.get(Calendar.MINUTE) > data.getInt(1)*24*60 + data.getInt(2)) {
                delay = 24 * 60 - (calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)) + 24 * 60 * (6 - currentDay + data.getInt(1)) + data.getInt(2);
            } else delay = 0;

            delays.add(new Integer[] {data.getInt(0), delay});
            data.moveToNext();
        }

        data.close();

        Integer[][] delaysArray = delays.toArray(new Integer[delays.size()][2]);   //преобразуем в массив для сортировки

        Arrays.sort(delaysArray, new ComparatorDelays());

        final ArrayDeque<Integer[]> arrayDeque = new ArrayDeque<Integer[]>();

        for (int i = 0; i < delaysArray.length; i ++)
            arrayDeque.add(delaysArray[i]);

        int count = delaysArray.length;


        for (int i = 0; i < count; i++) {
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    Profile profile = new Profile(arrayDeque.pollFirst()[0]);
                    profile.startProfile();
                }
            };
            timer.schedule(timerTask, delaysArray[i][1]*60*1000, 1000*60*60*24*7);
        }

        //TODO сервис не поднимается после падения!
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
