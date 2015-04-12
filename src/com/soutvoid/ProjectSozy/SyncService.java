package com.soutvoid.ProjectSozy;

import android.app.*;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPFile;

import java.io.*;
import java.net.ConnectException;
import java.sql.Array;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by andrew on 28.03.15.
 */
public class SyncService extends Service {

    //TODO реализовать отмену загрузки/выгрузки


    SQLiteDatabase db;
    SQLiteOpen dbOpen;

    FTPClient ftpClient;

    Integer filesCount = 0;
    Integer FTPFilesCount = 0;
    Integer counter;  //TODO это была плохая идея
    Integer id;

    Handler showNotifStarting;
    Handler showNotifProcessing;
    Handler showNotifDone;

    ExecutorService executorService;

    ArrayList<Integer[]> delays;
    TimerTask timerTask;

    int currentDay;

    //выгрузка и все, что с ней связано
    //вторая попытка
    public void filesCount(File[] files) {
        for (File file : files) {
            if (file.isDirectory())
                filesCount(new File(file.getAbsolutePath()).listFiles());
            else filesCount++;
        }
    }

    public void uploadInnerMethod(File[] files, final String destination) throws IOException {
        for (File file : files) {
            if (file.isDirectory()) {
                ftpClient.makeDirectory(destination + "/" + file.getName());
                uploadInnerMethod(file.listFiles(), destination + "/" + file.getName());
            } else {
                BufferedInputStream buffin = new BufferedInputStream((new FileInputStream(file.getAbsolutePath())));
                ftpClient.changeWorkingDirectory(destination);
                ftpClient.storeFile(file.getName(), buffin);
                buffin.close();
                showNotifProcessing.sendEmptyMessage(0);
            }
        }
    }

    public void upload(final String profileName) {
        final Cursor data = db.query("profiles", new String[] {"address", "user", "password", "localpath", "remotepath"}, "name = '" + profileName + "'", null, null, null, null);
        data.moveToFirst();

        final String localpath = data.getString(3);
        final String remotepath = data.getString(4);

        showNotifStarting.sendEmptyMessage(0);

        try {
            ftpClient.connect(data.getString(0));
            ftpClient.login(data.getString(1), data.getString(2));
            data.close();
            File localPathFile = new File(localpath);
            filesCount = 0;
            if (localPathFile.isDirectory()) {
                filesCount(localPathFile.listFiles());
                showNotifProcessing.sendEmptyMessage(0);
                String destination = remotepath + "/" + localPathFile.getName();
                ftpClient.changeWorkingDirectory(remotepath);
                ftpClient.makeDirectory(localPathFile.getName());
                uploadInnerMethod(localPathFile.listFiles(), destination);
            } else {
                filesCount = 1;
                showNotifProcessing.sendEmptyMessage(0);
                BufferedInputStream buffin = new BufferedInputStream(new FileInputStream(localpath));
                ftpClient.changeWorkingDirectory(remotepath);
                ftpClient.storeFile(localPathFile.getName(), buffin);
                buffin.close();
                showNotifProcessing.sendEmptyMessage(0);
            }
            ftpClient.logout();
            ftpClient.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            showNotifDone.sendEmptyMessage(0);
        }
    }

    //загрузка и все, что с ней связано
    //вторая попытка
    public void FTPFilesCount(FTPFile[] files) throws IOException {
        String currentDir = ftpClient.printWorkingDirectory();
        for (FTPFile file : files) {
            if (!file.getName().equals(".") && !file.getName().equals("..")) {
                if (file.isDirectory()) {
                    ftpClient.changeWorkingDirectory(ftpClient.printWorkingDirectory() + "/" + file.getName());
                    FTPFilesCount(ftpClient.listFiles());
                    ftpClient.changeWorkingDirectory(currentDir);
                } else {
                    FTPFilesCount++;
                }
            }
        }
    }

    public void downloadInnerMethod(FTPFile[] files, final String destination) throws IOException {
        String currentDir = ftpClient.printWorkingDirectory();
        for (FTPFile file : files) {
            if (!file.getName().equals(".") && !file.getName().equals("..")) {
                if (file.isDirectory()) {
                    new File(destination + "/" + file.getName()).mkdir();
                    ftpClient.changeWorkingDirectory(ftpClient.printWorkingDirectory() + "/" + file.getName());
                    downloadInnerMethod(ftpClient.listFiles(), destination + "/" + file.getName());
                    ftpClient.changeWorkingDirectory(currentDir);
                } else {
                    BufferedOutputStream buffout = new BufferedOutputStream(new FileOutputStream(destination + "/" + file.getName()));
                    ftpClient.retrieveFile(ftpClient.printWorkingDirectory() + "/" + file.getName(), buffout);
                    buffout.close();
                    showNotifProcessing.sendEmptyMessage(1);
                    //здесь увеличиваем счетчик
                }
            }
        }
    }

    public void download(final String profileName) {
        final Cursor data = db.query("profiles", new String[] {"address", "user", "password", "localpath", "remotepath"}, "name = '" + profileName + "'", null, null, null, null);
        data.moveToFirst();

        final String localpath = data.getString(3);
        final String remotepath = data.getString(4);

        showNotifStarting.sendEmptyMessage(0);

        try {
            ftpClient.connect(data.getString(0));
            ftpClient.login(data.getString(1), data.getString(2));
            data.close();
            FTPFilesCount = 0;
            if (ftpClient.changeWorkingDirectory(remotepath)) {
                FTPFilesCount(ftpClient.listFiles());
                showNotifProcessing.sendEmptyMessage(1);
                 //здесь определяется кол-во файлов
                String destination = localpath + remotepath.substring(remotepath.lastIndexOf("/"));
                new File(destination).mkdirs();
                downloadInnerMethod(ftpClient.listFiles(), destination);
            } else {   //если таргет это файл
                FTPFilesCount = 1;
                showNotifProcessing.sendEmptyMessage(1);
                ftpClient.changeWorkingDirectory(remotepath.substring(0, remotepath.lastIndexOf("/")));
                BufferedOutputStream buffout = new BufferedOutputStream(new FileOutputStream(localpath + remotepath.substring(remotepath.lastIndexOf("/"))));
                ftpClient.retrieveFile(remotepath, buffout);
                buffout.close();
                showNotifProcessing.sendEmptyMessage(1);
            }
            ftpClient.logout();
            ftpClient.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            showNotifDone.sendEmptyMessage(0);
        }
    }


    public void onCreate() {
        super.onCreate();

        dbOpen = new SQLiteOpen(this);
        try {
            db = dbOpen.getWritableDatabase();
        } catch (SQLiteException e) {
            e.printStackTrace();
            db = dbOpen.getReadableDatabase();
        }


        ftpClient = new FTPClient();
        ftpClient.setAutodetectUTF8(true);

        executorService = Executors.newFixedThreadPool(1);

        showNotifStarting = new Handler() {
            public void handleMessage(android.os.Message message) {
                Context context = SyncService.this;

                Resources resources = context.getResources();

                Cursor names = db.query("profiles", new String[] {"name"}, "_id = " + id, null, null, null, null);
                names.moveToFirst();
                Intent intent = new Intent(SyncService.this, ProfileInfo.class).putExtra("name", names.getString(0));
                PendingIntent pendingIntent = PendingIntent.getActivity(SyncService.this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);


                Notification.Builder builder = new Notification.Builder(context);


                builder.setContentIntent(pendingIntent)
                        .setSmallIcon(R.drawable.ic_notif)
                        .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_notif))
                        .setTicker(resources.getString(R.string.syncstarted))
                        .setWhen(System.currentTimeMillis())
                        .setContentTitle('"' + names.getString(0) + '"' + " " + resources.getString(R.string.is) + " " + resources.getString(R.string.syncstarted))
                        .setContentText(resources.getString(R.string.showprofile))
                        .setProgress(0, 0, true)
                        .setAutoCancel(false);

                names.close();

                Notification notification = builder.build();

                notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;

                NotificationManager notificationManager = (NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(1, notification);

            }
        };

        //посылать 0, если идет выгрузка и 1, если загрузка
        showNotifProcessing = new Handler() {
            public void handleMessage(android.os.Message message) {
                Context context = SyncService.this;

                Resources resources = context.getResources();

                Cursor names = db.query("profiles", new String[]{"name"}, "_id = " + id, null, null, null, null);
                names.moveToFirst();
                Intent intent = new Intent(SyncService.this, ProfileInfo.class).putExtra("name", names.getString(0));
                PendingIntent pendingIntent = PendingIntent.getActivity(SyncService.this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

                int maxCount = 0;
                if (message.what == 0) maxCount = filesCount;
                else if (message.what == 1) maxCount = FTPFilesCount;

                Notification.Builder builder = new Notification.Builder(context);


                builder.setContentIntent(pendingIntent)
                        .setSmallIcon(R.drawable.ic_notif)
                        .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_notif))
                        .setTicker(resources.getString(R.string.syncing))
                        .setWhen(System.currentTimeMillis())
                        .setContentTitle('"' + names.getString(0) + '"' + " " + resources.getString(R.string.is) + " " + resources.getString(R.string.syncing))
                        .setContentText(resources.getString(R.string.showprofile))
                        .setProgress(maxCount, counter, false)
                        .setAutoCancel(false);

                names.close();

                Notification notification = builder.build();

                notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;

                NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(1, notification);

                counter++;
            }

        };

        showNotifDone = new Handler() {
            public void handleMessage(android.os.Message message) {
                Context context = SyncService.this;

                Resources resources = context.getResources();

                Cursor names = db.query("profiles", new String[] {"name"}, "_id = " + id, null, null, null, null);
                names.moveToFirst();
                Intent intent = new Intent(SyncService.this, ProfileInfo.class).putExtra("name", names.getString(0));
                PendingIntent pendingIntent = PendingIntent.getActivity(SyncService.this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);


                Notification.Builder builder = new Notification.Builder(context);


                builder.setContentIntent(pendingIntent)
                        .setSmallIcon(R.drawable.ic_notif)
                        .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_notif))
                        .setTicker(resources.getString(R.string.synccomplete))
                        .setWhen(System.currentTimeMillis())
                        .setContentTitle('"' + names.getString(0) + '"' + " " + resources.getString(R.string.synccomplete))
                        .setContentText(resources.getString(R.string.showprofile))
                        .setAutoCancel(true);

                names.close();

                Notification notification = builder.build();

                NotificationManager notificationManager = (NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(1, notification);
            }
        };

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

        delays = new ArrayList<Integer[]>();

        currentDay = calendar.get(Calendar.DAY_OF_WEEK) == 1?6:calendar.get(Calendar.DAY_OF_WEEK) - 2;

        for (int i = 0; i < data.getCount(); i++) {
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

        Integer[][] delaysArray = delays.toArray(new Integer[delays.size()][2]);

        Arrays.sort(delaysArray, new ComparatorDelays());

        final ArrayDeque<Integer[]> arrayDeque = new ArrayDeque<Integer[]>();

        for (int i = 0; i < delaysArray.length; i ++)
            arrayDeque.add(delaysArray[i]);

        int count = delaysArray.length;


        for (int i = 0; i < count; i++) {
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    startProfile(arrayDeque.pollFirst()[0]);
                }
            };
            timer.schedule(timerTask, delaysArray[i][1]*60*1000, 1000*60*60*24*7);
        }

        //TODO сервис не возмобновляет работу после падения!
    }

    public void startProfile(final int id) {
        this.id = id;
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                counter = 0;
                Cursor types = db.query("profiles", new String[]{"type", "name"}, "_id = " + id, null, null, null, null);
                types.moveToFirst();
                if (types.getString(0).equals("download")) {
                    download(types.getString(1));
                } else {
                    upload(types.getString(1));
                }

                types.close();

            }
        });

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
