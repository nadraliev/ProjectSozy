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
import android.widget.Toast;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPFile;

import java.io.*;
import java.net.ConnectException;

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
    Integer id = 0;
    Integer counter;

    Handler showNotifStarting;
    Handler showNotifProcessing;
    Handler showNotifDone;


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

        final Thread upload = new Thread(new Runnable() {
            @Override
            public void run() {
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
        });
        upload.start();
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

        final Thread download = new Thread(new Runnable() {
            @Override
            public void run() {
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
        });
        download.start();
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
        id = intent.getIntExtra("_id", 1);

        counter = 0;

        Cursor types = db.query("profiles", new String[]{"type", "name"}, "_id = " + id, null, null, null, null);
        types.moveToFirst();
        if (types.getString(0).equals("download")) {
            download(types.getString(1));
        } else {
            Cursor data = db.query("profiles", new String[] {"address", "user", "password", "localpath", "remotepath"}, "_id = " + id, null, null, null, null);
            data.moveToFirst();
            upload(types.getString(1));
        }

        types.close();

        return START_STICKY;
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public IBinder onBind(Intent intent) {
        return null;
    }


}
