package com.soutvoid.ProjectSozy;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.BitmapFactory;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.Executor;

/**
 * Created by andrew on 27.04.15.
 */
public class Profile {

    public int id;
    public String name;
    public String address;
    public String user;
    private String password;
    public String path;
    public String destination;
    public String type;

    public SQLiteDatabase db;
    public SQLiteOpenProfiles dbOpen;

    public FTPClient ftpClient;

    private Context context;

    private Integer filesCount = 0;
    private  Integer counter = 0;

    public Profile(int id) {
        try {
            context = MainActivity.context;
        } catch (Exception e) {
            context = SyncService.context;
        }
        this.id = id;
        try {
            dbOpen = new SQLiteOpenProfiles(context);
            db = dbOpen.getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
            db = dbOpen.getReadableDatabase();
        }
        Cursor cursor = db.query("profiles", new String[] {"name", "address", "user", "password", "path", "destination", "type"}, "_id = " + id, null, null, null, null);
        cursor.moveToFirst();
        name = cursor.getString(0);
        address = cursor.getString(1);
        user = cursor.getString(2);
        password = cursor.getString(3);
        path = cursor.getString(4);
        destination = cursor.getString(5);
        type = cursor.getString(6);
        cursor.close();
        db.close();
    }

    public Profile(String name) {
        try {
            context = MainActivity.context;
        } catch (Exception e) {
            context = SyncService.context;
        }
        this.name = name;
        try {
            dbOpen = new SQLiteOpenProfiles(context);
            db = dbOpen.getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
            db = dbOpen.getReadableDatabase();
        }
        Cursor cursor = db.query("profiles", new String[] {"_id", "address", "user", "password", "path", "destination", "type"}, "name = '" + name + "'", null, null, null, null);
        cursor.moveToFirst();
        id = cursor.getInt(0);
        address = cursor.getString(1);
        user = cursor.getString(2);
        password = cursor.getString(3);
        path = cursor.getString(4);
        destination = cursor.getString(5);
        type = cursor.getString(6);
        cursor.close();
        db.close();
    }

    //выгрузка и все, что с ней связано
    private void filesCount(File[] files) {
        for (File file : files) {
            if (file.isDirectory())
                filesCount(new File(file.getAbsolutePath()).listFiles());
            else filesCount++;
        }
    }

    private void uploadInnerMethod(File[] files, final String destination) throws IOException {
        for (File file : files) {
            if (file.isDirectory()) {
                ftpClient.makeDirectory(destination + "/" + file.getName());
                uploadInnerMethod(file.listFiles(), destination + "/" + file.getName());
            } else {
                BufferedInputStream buffin = new BufferedInputStream((new FileInputStream(file.getAbsolutePath())));
                ftpClient.changeWorkingDirectory(destination);
                ftpClient.storeFile(file.getName(), buffin);
                buffin.close();
                sendNotifProcessing();
            }
        }
    }

    private void upload(final String profileName) {
        final Cursor data = db.query("profiles", new String[]{"address", "user", "password", "localpath", "remotepath"}, "name = '" + profileName + "'", null, null, null, null);
        data.moveToFirst();

        ftpClient = new FTPClient();
        ftpClient.setAutodetectUTF8(true);

        final String localpath = data.getString(3);
        final String remotepath = data.getString(4);

        sendNotifStart();

        try {
            ftpClient.connect(data.getString(0));
            ftpClient.login(data.getString(1), data.getString(2));
            data.close();
            File localPathFile = new File(localpath);
            filesCount = 0;
            if (localPathFile.isDirectory()) {
                filesCount(localPathFile.listFiles());
                sendNotifProcessing();
                String destination = remotepath + "/" + localPathFile.getName();
                ftpClient.changeWorkingDirectory(remotepath);
                ftpClient.makeDirectory(localPathFile.getName());
                uploadInnerMethod(localPathFile.listFiles(), destination);
            } else {
                filesCount = 1;
                sendNotifProcessing();
                BufferedInputStream buffin = new BufferedInputStream(new FileInputStream(localpath));
                ftpClient.changeWorkingDirectory(remotepath);
                ftpClient.storeFile(localPathFile.getName(), buffin);
                buffin.close();
                sendNotifProcessing();
            }
            ftpClient.logout();
            ftpClient.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            sendNotifDone();
        }
    }

    //загрузка и все, что с ней связано
    private void FTPFilesCount(FTPFile[] files) throws IOException {
        String currentDir = ftpClient.printWorkingDirectory();
        for (FTPFile file : files) {
            if (!file.getName().equals(".") && !file.getName().equals("..")) {
                if (file.isDirectory()) {
                    ftpClient.changeWorkingDirectory(ftpClient.printWorkingDirectory() + "/" + file.getName());
                    FTPFilesCount(ftpClient.listFiles());
                    ftpClient.changeWorkingDirectory(currentDir);
                } else {
                    filesCount++;
                }
            }
        }
    }

    private void downloadInnerMethod(FTPFile[] files, final String destination) throws IOException {
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
                    sendNotifProcessing();
                    //здесь увеличиваем счетчик
                }
            }
        }
    }

    private void download(final String profileName) {
        final Cursor data = db.query("profiles", new String[]{"address", "user", "password", "localpath", "remotepath"}, "name = '" + profileName + "'", null, null, null, null);
        data.moveToFirst();

        ftpClient = new FTPClient();
        ftpClient.setAutodetectUTF8(true);

        final String localpath = data.getString(3);
        final String remotepath = data.getString(4);

        sendNotifStart();

        try {
            ftpClient.connect(data.getString(0));
            ftpClient.login(data.getString(1), data.getString(2));
            data.close();
            filesCount = 0;
            if (ftpClient.changeWorkingDirectory(remotepath)) {
                FTPFilesCount(ftpClient.listFiles());
                sendNotifProcessing();
                //здесь определяется кол-во файлов
                String destination = localpath + remotepath.substring(remotepath.lastIndexOf("/"));
                new File(destination).mkdirs();
                downloadInnerMethod(ftpClient.listFiles(), destination);
            } else {   //если таргет это файл
                filesCount = 1;
                sendNotifProcessing();
                ftpClient.changeWorkingDirectory(remotepath.substring(0, remotepath.lastIndexOf("/")));
                BufferedOutputStream buffout = new BufferedOutputStream(new FileOutputStream(localpath + remotepath.substring(remotepath.lastIndexOf("/"))));
                ftpClient.retrieveFile(remotepath, buffout);
                buffout.close();
                sendNotifProcessing();
            }
            ftpClient.logout();
            ftpClient.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            sendNotifDone();
        }
    }


    public void startProfile() {
        dbOpen = new SQLiteOpenProfiles(context);
        try {
            db = dbOpen.getWritableDatabase();
        } catch (SQLiteException e) {
            e.printStackTrace();
            db = dbOpen.getReadableDatabase();
        }

        counter = 0;


        if (name == null) {
            Cursor cursor = db.query("profiles", new String[] {"name"}, "_id = " + id, null, null, null, null);
            cursor.moveToFirst();
            name = cursor.getString(0);
            cursor.close();
        }

        Cursor cursor = db.query("profiles", new String[]{"type"}, "name = '" + name + "'", null, null, null, null);
        cursor.moveToFirst();
        if (cursor.getString(0).equals("upload"))
            upload(name);
        else download(name);
        cursor.close();
    }

    private void sendTextOnNotif(String input, int id) {

        Notification.Builder builder = new Notification.Builder(context);


        builder
                .setSmallIcon(R.drawable.ic_notif)
                .setTicker(input)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(input)
                .setContentText(input)
                .setAutoCancel(true);


        Notification notification = builder.build();

        NotificationManager notificationManager = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, notification);
    }

    private void sendNotifStart() {

        Resources resources = context.getResources();

        Intent intent = new Intent(context, ProfileInfo.class).putExtra("name", name);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);


        Notification.Builder builder = new Notification.Builder(context);


        builder.setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_notif)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_notif))
                .setTicker(resources.getString(R.string.syncstarted))
                .setWhen(System.currentTimeMillis())
                .setContentTitle('"' + name + '"' + " " + resources.getString(R.string.is) + " " + resources.getString(R.string.syncstarted))
                .setContentText(resources.getString(R.string.showprofile))
                .setProgress(0, 0, true)
                .setAutoCancel(false);


        Notification notification = builder.build();

        notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;

        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);

    }

    private void sendNotifDone() {

        Resources resources = context.getResources();

        Intent intent = new Intent(context, ProfileInfo.class).putExtra("name", name);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);


        Notification.Builder builder = new Notification.Builder(context);


        builder.setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_notif)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_notif))
                .setTicker(resources.getString(R.string.synccomplete))
                .setWhen(System.currentTimeMillis())
                .setContentTitle('"' + name + '"' + " " + resources.getString(R.string.synccomplete))
                .setContentText(resources.getString(R.string.showprofile))
                .setAutoCancel(true);


        Notification notification = builder.build();

        NotificationManager notificationManager = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }

    private void sendNotifProcessing() {

        Resources resources = context.getResources();

        Intent intent = new Intent(context, ProfileInfo.class).putExtra("name", name);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        int maxCount = 0;
        maxCount = filesCount;

        Notification.Builder builder = new Notification.Builder(context);


        builder.setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_notif)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_notif))
                .setTicker(resources.getString(R.string.syncing))
                .setWhen(System.currentTimeMillis())
                .setContentTitle('"' + name + '"' + " " + resources.getString(R.string.is) + " " + resources.getString(R.string.syncing))
                .setContentText(resources.getString(R.string.showprofile))
                .setProgress(maxCount, counter, false)
                .setAutoCancel(false);


        Notification notification = builder.build();

        notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);

        counter++;
    }

    public static String md5(String st) {
        MessageDigest messageDigest = null;
        byte[] digest = new byte[0];

        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(st.getBytes());
            digest = messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        BigInteger bigInt = new BigInteger(1, digest);
        String md5Hex = bigInt.toString(16);

        while( md5Hex.length() < 32 ){
            md5Hex = "0" + md5Hex;
        }

        return md5Hex;
    }


    private ArrayList<String> checkDeletedLocal() {
        ArrayList<String> result = new ArrayList<String>();
        try {
            dbOpen = new SQLiteOpenProfiles(context);
            db = dbOpen.getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
            db = dbOpen.getReadableDatabase();
        }
        Cursor files = db.query("profile" + id, new String[]{"path"}, null, null, null, null, null);
        files.moveToFirst();
        for (int i = 0; i < files.getCount(); i++) {
            if (!(new File(path + files.getString(0)).exists())) {
                result.add(files.getString(0));
            }
            if (!(files.isLast())) files.moveToNext();
        }
        files.close();
        return result;
    }     //проверка на удаленные файлы

    private ArrayList<String> checkDeletedFTP() {
        ArrayList<String> result = new ArrayList<String>();
        try {
            dbOpen = new SQLiteOpenProfiles(context);
            db = dbOpen.getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
            db = dbOpen.getReadableDatabase();
        }
        try {
            ftpClient = new FTPClient();
            ftpClient.setAutodetectUTF8(true);
            ftpClient.connect(address);
            ftpClient.login(user, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Cursor files = db.query("profile" + id, new String[]{"path"}, null, null, null, null, null);
        files.moveToFirst();
        for (int i = 0; i < files.getCount(); i++) {
            try {
                ftpClient.changeWorkingDirectory(path + files.getString(0).substring(0, files.getString(0).lastIndexOf("/")));
                if (ftpClient.mlistFile(path + files.getString(0)) == null) {
                    result.add(files.getString(0));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!(files.isLast()))
                files.moveToNext();
        }
        try {
            ftpClient.logout();
            ftpClient.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        files.close();
        return result;
    }

    public void rescanLocal(String path, String prefix) {
        for (File file : new File(path).listFiles()) {
            if (file.isDirectory()) {
                rescanLocal(path + "/" + file.getName(), prefix + "/" + file.getName());
            } else {
                if (db.query("profile" + id, null, "path = '" + prefix + "/" + file.getName() + "'", null, null, null, null).getCount() == 0) {
                    ContentValues newValues = new ContentValues();
                    newValues.put("path", prefix + "/" + file.getName());
                    db.insert("profile" + id, null, newValues);
                }
            }
        }
    }       //просканировать рабочую папку и добавить новые. необходима заранее открытая база

    public void rescanFTP(String prefix) {
        try {
            for (FTPFile file : ftpClient.listFiles()) {
                if (!(file.getName().equals(".") || file.getName().equals(".."))) {
                    if (file.isDirectory()) {
                        ftpClient.changeWorkingDirectory(file.getName());
                        rescanFTP(prefix + "/" + file.getName());
                        ftpClient.changeToParentDirectory();
                    } else {
                        if (db.query("profile" + id, null, "path = '" + prefix + "/" + file.getName() + "'", null, null, null, null).getCount() == 0) {
                            ContentValues newValues = new ContentValues();
                            newValues.put("path", prefix + "/" + file.getName());
                            db.insert("profile" + id, null, newValues);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }     //необходимы заранее открытая база и FTPClient в директории Path

    private ArrayList<String> checkChangesLocal() {
        ArrayList<String> result = new ArrayList<String>();
        try {
            dbOpen = new SQLiteOpenProfiles(context);
            db = dbOpen.getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
            db = dbOpen.getReadableDatabase();
        }
        Cursor files = db.query("profile" + id, new String[]{"path", "sizedigest"}, null, null, null, null, null);
        files.moveToFirst();
        for (int i = 0; i < files.getCount(); i++) {
            if (!(md5(new File(path + files.getString(0)).length() + "").equals(files.getString(1)))) {
                result.add(files.getString(0));
            }
            if (!files.isLast()) files.moveToNext();
        }
        files.close();
        return result;
    }       //проверка на измененные файлы

    private ArrayList<String> checkChangesFTP() {
        ArrayList<String> result = new ArrayList<String>();
        try {
            dbOpen = new SQLiteOpenProfiles(context);
            db = dbOpen.getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
            db = dbOpen.getReadableDatabase();
        }
        Cursor files = db.query("profile" + id, new String[]{"path", "sizedigest"}, null, null, null, null, null);
        files.moveToFirst();
        try {
            ftpClient = new FTPClient();
            ftpClient.setAutodetectUTF8(true);
            ftpClient.connect(address);
            ftpClient.login(user, password);
            for (int i = 0; i < files.getCount(); i++) {
                if (!(md5(ftpClient.mlistFile(path + files.getString(0)).getSize() + "").equals(files.getString(1)))) {
                    result.add(files.getString(0));
                }
                if (!files.isLast()) files.moveToNext();
            }
            ftpClient.logout();
            ftpClient.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        files.close();
        return result;
    }

    private void deleteLocal(ArrayList<String> files) {
        try {
            dbOpen = new SQLiteOpenProfiles(context);
            db = dbOpen.getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
            db = dbOpen.getReadableDatabase();
        }
        try {
            ftpClient = new FTPClient();
            ftpClient.setAutodetectUTF8(true);
            ftpClient.connect(address);
            ftpClient.login(user, password);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (String file : files) {
            try {
                ftpClient.deleteFile(destination + file);
            } catch (Exception e) {
                e.printStackTrace();
            }
            db.delete("profile" + id, "path = '" + file + "'", null);
        }
        db.close();
        try {
            ftpClient.logout();
            ftpClient.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }       //удаление файлов с сервера и из баз данных

    private void deleteFTP(ArrayList<String> files) {
        try {
            dbOpen = new SQLiteOpenProfiles(context);
            db = dbOpen.getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
            db = dbOpen.getReadableDatabase();
        }
        for (String file : files) {
            try {
                new File(destination + file).delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
            db.delete("profile" + id, "path = '" + file + "'", null);
        }
        db.close();
    }

    private void upload(ArrayList<String> files) {
                try {
                    ftpClient = new FTPClient();
                    ftpClient.setAutodetectUTF8(true);
                    ftpClient.connect(address);
                    ftpClient.login(user, password);
                    ftpClient.changeWorkingDirectory(destination);

                    try {
                        dbOpen = new SQLiteOpenProfiles(context);
                        db = dbOpen.getWritableDatabase();
                    } catch (Exception e) {
                        e.printStackTrace();
                        db = dbOpen.getReadableDatabase();
                    }
                    for (String file : files) {
                        BufferedInputStream buffin = new BufferedInputStream((new FileInputStream(path + file)));
                        ContentValues newValues = new ContentValues();
                        newValues.put("sizedigest", md5(new File(path + file).length() + ""));
                        db.update("profile" + id, newValues, "path = '" + file + "'", null);
                        file = file.substring(1);
                        while (file.contains("/")) {
                            ftpClient.makeDirectory(file.substring(0, file.indexOf("/")));
                            ftpClient.changeWorkingDirectory(file.substring(0, file.indexOf("/")));
                            file = file.substring(file.indexOf("/") + 1);
                        }
                        ftpClient.storeFile(file, buffin);
                        buffin.close();
                    }
                    db.close();
                    ftpClient.logout();
                    ftpClient.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
    }       //выгрузка на сервер

    private void download(ArrayList<String> files) {
        try {
            ftpClient = new FTPClient();
            ftpClient.setAutodetectUTF8(true);
            ftpClient.connect(address);
            ftpClient.login(user, password);

            try {
                dbOpen = new SQLiteOpenProfiles(context);
                db = dbOpen.getWritableDatabase();
            } catch (Exception e) {
                e.printStackTrace();
                db = dbOpen.getReadableDatabase();
            }
            for (String file : files) {
                new File(destination + file.substring(0, file.lastIndexOf("/"))).mkdirs();
                BufferedOutputStream buffout = new BufferedOutputStream(new FileOutputStream(destination + file));
                ftpClient.changeWorkingDirectory(path);
                ftpClient.retrieveFile(ftpClient.printWorkingDirectory() + file, buffout);
                ContentValues newValues = new ContentValues();
                newValues.put("sizedigest", md5(ftpClient.mlistFile(path + file).getSize() + ""));
                db.update("profile" + id, newValues, "path = '" + file + "'", null);
                buffout.close();
            }
            db.close();
            ftpClient.logout();
            ftpClient.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }       //загрузка с сервера

    public void sync() {
        if (type.equals("upload")) {
            deleteLocal(checkDeletedLocal());
            try {
                dbOpen = new SQLiteOpenProfiles(context);
                db = dbOpen.getWritableDatabase();
            } catch (Exception e) {
                e.printStackTrace();
                db = dbOpen.getReadableDatabase();
            }
            if (db.query("profile" + id, null, null, null, null, null, null).getCount() != 0)
            rescanLocal(path, "");
            db.close();
            upload(checkChangesLocal());
        } else {
            deleteFTP(checkDeletedFTP());
            try {
                dbOpen = new SQLiteOpenProfiles(context);
                db = dbOpen.getWritableDatabase();
            } catch (Exception e) {
                e.printStackTrace();
                db = dbOpen.getReadableDatabase();
            }
            if (db.query("profile" + id, null, null, null, null, null, null).getCount() != 0)
            try {
                ftpClient = new FTPClient();
                ftpClient.setAutodetectUTF8(true);
                ftpClient.connect(address);
                ftpClient.login(user, password);
                ftpClient.changeWorkingDirectory(path);
                rescanFTP("");
                ftpClient.logout();
                ftpClient.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            download(checkChangesFTP());
        }
        db.close();
    }

    public void openConnection() {
        try {
            ftpClient = new FTPClient();
            ftpClient.setAutodetectUTF8(true);
            ftpClient.connect(address);
            ftpClient.login(user, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            ftpClient.logout();
            ftpClient.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}