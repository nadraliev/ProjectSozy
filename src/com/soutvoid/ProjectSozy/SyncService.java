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

    SQLiteDatabase db;
    SQLiteOpen dbOpen;

    FTPClient ftpClient = new FTPClient();

    Integer filesCount = 0;
    Integer FTPFilesCount = 0;
    Integer id = 0;
    Integer counter;

    Handler showNotif;

    Notification start;

    //выгрузка все, что с ней связано
    public boolean hasDirectory(File target) {
        boolean flag = false;
        if (target.isDirectory()) {
            for (int counter = 0; counter < target.listFiles().length; counter++) {
                if (target.listFiles()[counter].isDirectory()) flag = true;
            }
        }
        return flag;
    }

    public void mkDirFTP(final String address, final String user, final String passwd, final String dirName, final String destination, final FTPClient ftpClient) {
        try {
            ftpClient.changeWorkingDirectory(destination);
            boolean isContains = false;
            if (ftpClient.listFiles().length != 0)
                for (int counter = 0; counter < ftpClient.listFiles().length; counter++) {
                    if (dirName.equals(ftpClient.listFiles()[counter].getName())) isContains = true;
                }
            if (!isContains) {
                ftpClient.makeDirectory(dirName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void filesCount(File dir) {
        for (int counter = 0; counter < dir.listFiles().length; counter++) {
            if (dir.listFiles()[counter].isDirectory()) filesCount(new File(dir.toString() + "/" + dir.listFiles()[counter].getName()));
            else {
                filesCount += 1;
            }
        }
    }

    public void Uploading(final String address, final String user, final String passwd, final String target, final String destination) {
        showNotif.sendEmptyMessage(0);

        filesCount = 0;
        if (new File(target).isDirectory())
            filesCount(new File(target));
        else filesCount = 1;

        showNotif.sendEmptyMessage(0);

        //Инициализируем строки исключений
        final String UnknownHostException = getString(R.string.unknownhostexception);
        final String ConnectionException = getString(R.string.connectexception);
        final String ConnectionClosedException = getString(R.string.connectionclosedexception);

        //Инициализируем тосты для исключений
        final Toast UnknownHostExceptionToast = Toast.makeText(getApplicationContext(), UnknownHostException + address, Toast.LENGTH_LONG);
        final Toast ConnectionExceptionToast = Toast.makeText(getApplicationContext(), ConnectionException, Toast.LENGTH_LONG);
        final Toast ConnectionClosedExceptionToast = Toast.makeText(getApplicationContext(), ConnectionClosedException, Toast.LENGTH_LONG);

        Thread upload = new Thread(new Runnable() {
            @Override
            public void run() {
                final FTPClient ftpClient = new FTPClient();
                try {
                    ftpClient.connect(address);
                    ftpClient.login(user, passwd);
                    UploadToFTPServer(address, user, passwd, target, destination, ftpClient);
                    ftpClient.logout();
                    ftpClient.disconnect();
                } catch (java.net.UnknownHostException e) {
                    e.printStackTrace();
                    UnknownHostExceptionToast.show();
                } catch (ConnectException e) {
                    e.printStackTrace();                                    //TODO улучшиь распознавание исключений
                    ConnectionExceptionToast.show();
                } catch (FTPConnectionClosedException e) {
                    e.printStackTrace();
                    ConnectionClosedExceptionToast.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        upload.start();
    }

    public void UploadToFTPServer(final String address, final String user, final String passwd, final String target, final String destination, final FTPClient ftpClient) {
        final File targetFile = new File(target);

        //Создаем папку
        if (targetFile.isDirectory())
            mkDirFTP(address, user, passwd, targetFile.getName(), destination, ftpClient);


        //Загружаем файлы
        if (hasDirectory(targetFile)) {
            for (int counter = 0; counter < targetFile.listFiles().length; counter++) {
                if (targetFile.listFiles()[counter].isDirectory()) {
                    UploadToFTPServer(address, user, passwd, targetFile.listFiles()[counter].toString(), destination + "/" + targetFile.getName(), ftpClient);
                }
            }
        }

        try {
            if (targetFile.isDirectory())
                ftpClient.changeWorkingDirectory(destination + "/" + targetFile.getName());
            else ftpClient.changeWorkingDirectory(destination);
            BufferedInputStream buffin = null;
            if (targetFile.isDirectory()) {
                for (int counter = 0; counter < targetFile.listFiles().length; counter++) {
                    if (!targetFile.listFiles()[counter].isDirectory())
                        buffin = new BufferedInputStream(new FileInputStream(targetFile.listFiles()[counter].toString()));
                    ftpClient.storeFile(targetFile.listFiles()[counter].getName(), buffin);
                    showNotif.sendEmptyMessage(0);
                    //здесь увеличиваем счетчик
                }
            } else {
                buffin = new BufferedInputStream(new FileInputStream(targetFile.toString()));
                ftpClient.storeFile(targetFile.getName(), buffin);
                buffin.close();
                showNotif.sendEmptyMessage(0);
                //и здесь тоже
            }
        } catch (IOException e) {
            e.printStackTrace();
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
                    showNotif.sendEmptyMessage(1);
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

        showNotif.sendEmptyMessage(1);

        final Thread download = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ftpClient.connect(data.getString(0));
                    ftpClient.login(data.getString(1), data.getString(2));
                    if (ftpClient.changeWorkingDirectory(remotepath)) {
                        FTPFilesCount(ftpClient.listFiles());
                        showNotif.sendEmptyMessage(1);
                        //здесь определяется кол-во файлов
                        String destination = localpath + remotepath.substring(remotepath.lastIndexOf("/"));
                        new File(destination).mkdirs();
                        downloadInnerMethod(ftpClient.listFiles(), destination);
                    } else {   //если таргет это файл
                        FTPFilesCount = 1;
                        showNotif.sendEmptyMessage(1);
                        ftpClient.changeWorkingDirectory(remotepath.substring(0, remotepath.lastIndexOf("/")));
                        BufferedOutputStream buffout = new BufferedOutputStream(new FileOutputStream(localpath + remotepath.substring(remotepath.lastIndexOf("/"))));
                        ftpClient.retrieveFile(remotepath, buffout);
                        buffout.close();
                        showNotif.sendEmptyMessage(1);
                    }
                    ftpClient.logout();
                    ftpClient.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
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

        ftpClient.setAutodetectUTF8(true);

        showNotif = new Handler() {
            public void handleMessage(android.os.Message message) {
                Context context = SyncService.this;

                Resources resources = context.getResources();  //посылать 0, если идет выгрузка и 1, если загрузка

                int maxCount = 0;
                if (message.what == 0) maxCount = filesCount;
                else if (message.what == 1) maxCount = FTPFilesCount;
                int notText;
                if (maxCount == counter)
                    notText = R.string.synccomplete;
                else  notText = R.string.syncing;
                Cursor names = db.query("profiles", new String[] {"name"}, "_id = " + id, null, null, null, null);
                names.moveToFirst();
                Intent intent = new Intent(SyncService.this, ProfileInfo.class).putExtra("name", names.getString(0));
                PendingIntent pendingIntent = PendingIntent.getActivity(SyncService.this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);


                Notification.Builder builder = new Notification.Builder(context);


                builder.setContentIntent(pendingIntent)
                        .setSmallIcon(R.drawable.ic_notif)
                        .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_notif))
                        .setTicker(resources.getString(notText))
                        .setWhen(System.currentTimeMillis())
                        .setAutoCancel(true)
                        .setContentTitle('"' + names.getString(0) + '"' + " " + resources.getString(R.string.is) + " " + resources.getString(notText))
                        .setContentText(resources.getString(R.string.showprofile));

                if (counter != -1) {
                    if (maxCount != counter) {
                        builder.setProgress(maxCount, counter, false);
                        builder.setAutoCancel(false);
                    }
                } else builder.setProgress(0, 0, true);

                start = builder.build();

                if (maxCount != counter) start.flags = start.flags | Notification.FLAG_ONGOING_EVENT;

                NotificationManager notificationManager = (NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);

                notificationManager.notify(1, start);

                counter++;
            }
        };
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        id = intent.getIntExtra("_id", 1);

        counter = -1;

        Cursor types = db.query("profiles", new String[]{"type", "name"}, "_id = " + id, null, null, null, null);
        types.moveToFirst();
        if (types.getString(0).equals("download")) {
            download(types.getString(1));
        } else {
            Cursor data = db.query("profiles", new String[] {"address", "user", "password", "localpath", "remotepath"}, "_id = " + id, null, null, null, null);
            data.moveToFirst();
            Uploading(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4));
        }



        return START_STICKY;
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public IBinder onBind(Intent intent) {
        return null;
    }


}
