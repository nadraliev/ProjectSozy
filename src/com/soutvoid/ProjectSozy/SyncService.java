package com.soutvoid.ProjectSozy;

import android.app.*;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.IBinder;
import org.apache.commons.net.ftp.FTPClient;

import java.io.*;
import java.util.*;

/**
 * Created by andrew on 28.03.15.
 */
public class SyncService extends Service {

    //TODO реализовать отмену загрузки/выгрузки


    SQLiteDatabase db;
    SQLiteOpenProfiles dbOpen;

    public static Context context;

    FTPClient ftpClient;

    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();

    }

    public int onStartCommand(Intent intent, int flags, int startId) {

        dbOpen = new SQLiteOpenProfiles(context);
        try {
            db = dbOpen.getWritableDatabase();
        } catch (SQLiteException e) {
            e.printStackTrace();
            db = dbOpen.getReadableDatabase();
        }


        Cursor profiles = db.query("profiles", new String[] {"_id"}, "type = 'upload'", null, null, null, null);
        profiles.moveToFirst();
        Cursor profile;
        ArrayList<Profile> forUpload = new ArrayList<Profile>();
        ArrayList<String> paths;
        Profile curProfile;
        for (int i = 0; i < profiles.getCount(); i++) {
            profile = db.query("profile" + profiles.getInt(0), new String[] {"path", "size"}, null, null, null, null, null);
            profile.moveToFirst();
            curProfile = new Profile(profiles.getInt(0));
            paths = new ArrayList<String>();
            if ((profile.getInt(1) != (new File(profile.getString(0))).length()) || !(new File(profile.getString(0))).exists()) {
                if (profile.getCount() == 1)
                    paths.add(profile.getString(0));
                else {
                    profile.moveToNext();
                    for (int k = 1; k < profile.getCount(); k++) {
                        if ((profile.getInt(1) != (new File(profile.getString(0))).length()) || !(new File(profile.getString(0))).exists())
                            paths.add(profile.getString(0));
                        profile.moveToNext();
                    }
                }
            }
            curProfile.uploadListArray = paths.toArray(new String[paths.size()]);
            forUpload.add(curProfile);
            profiles.moveToNext();
        }

        for (int i = 0; i < forUpload.size(); i++) {
            forUpload.get(i).uploadList();
        }


        db.close();
        dbOpen.close();
        stopSelf();

        return START_NOT_STICKY;
    }

    public void onDestroy() {
        super.onDestroy();
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
