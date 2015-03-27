package com.soutvoid.ProjectSozy;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPFile;

import java.io.*;
import java.net.ConnectException;

/**
 * Created by andrew on 27.03.15.
 */
public class ProfileInfo extends Activity {

    TextView infoName;
    TextView infoAddress;
    TextView infoUser;
    TextView infoLocalPath;
    TextView infoRemotePath;
    TextView infoType;

    SQLiteDatabase db;
    SQLiteOpen dbOpen;

    String name = MainActivity.currentName;

    FTPClient ftpClient = new FTPClient();

    ProgressDialog progressBar;
    ProgressDialog FTPProgressBar;

    Integer filesCount;
    Integer FTPFilesCount = 0;

    Handler increase;
    Handler close;
    Handler downloadHandler;

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
        filesCount = 0;
        if (new File(target).isDirectory())
            filesCount(new File(target));
        else filesCount = 1;


        //Инициализируем прогрессбар
        progressBar = new ProgressDialog(this);
        progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressBar.setMax(filesCount);
        progressBar.setIndeterminate(true);
        progressBar.show();

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
                    close.sendEmptyMessage(0);   //послать 0 для закрытия прогрессбара
                } catch (ConnectException e) {
                    e.printStackTrace();                                    //TODO улучшиь распознавание исключений
                    ConnectionExceptionToast.show();
                    close.sendEmptyMessage(0);
                } catch (FTPConnectionClosedException e) {
                    e.printStackTrace();
                    ConnectionClosedExceptionToast.show();
                    close.sendEmptyMessage(0);
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

        progressBar.setIndeterminate(false);
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
                    increase.sendEmptyMessage(1);
                }
            } else {
                buffin = new BufferedInputStream(new FileInputStream(targetFile.toString()));
                ftpClient.storeFile(targetFile.getName(), buffin);
                buffin.close();
                increase.sendEmptyMessage(1);
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
                    downloadHandler.sendEmptyMessage(-1);
                    BufferedOutputStream buffout = new BufferedOutputStream(new FileOutputStream(destination + "/" + file.getName()));
                    ftpClient.retrieveFile(ftpClient.printWorkingDirectory() + "/" + file.getName(), buffout);
                    buffout.close();
                    downloadHandler.sendEmptyMessage(-3);
                }
            }
        }
    }

    public void download(final String profileName) {
        final Cursor data = db.query("profiles", new String[] {"address", "user", "password", "localpath", "remotepath"}, "name = '" + profileName + "'", null, null, null, null);
        data.moveToFirst();

        final String localpath = data.getString(3);
        final String remotepath = data.getString(4);
        FTPProgressBar = new ProgressDialog(this);
        FTPProgressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        FTPProgressBar.setMax(0);
        FTPProgressBar.setIndeterminate(true);
        FTPProgressBar.show();



        final Thread download = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ftpClient.connect(data.getString(0));
                    ftpClient.login(data.getString(1), data.getString(2));
                    if (ftpClient.changeWorkingDirectory(remotepath)) {
                        FTPFilesCount(ftpClient.listFiles());
                        downloadHandler.sendEmptyMessage(FTPFilesCount);
                        FTPFilesCount = 0;
                        String destination = localpath + remotepath.substring(remotepath.lastIndexOf("/"));
                        new File(destination).mkdirs();
                        downloadInnerMethod(ftpClient.listFiles(), destination);
                    } else {
                        downloadHandler.sendEmptyMessage(1);
                        downloadHandler.sendEmptyMessage(-1);
                        ftpClient.changeWorkingDirectory(remotepath.substring(0, remotepath.lastIndexOf("/")));
                        BufferedOutputStream buffout = new BufferedOutputStream(new FileOutputStream(localpath + remotepath.substring(remotepath.lastIndexOf("/"))));
                        ftpClient.retrieveFile(remotepath, buffout);
                        buffout.close();
                        downloadHandler.sendEmptyMessage(-3);
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

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_info);
        getActionBar().setIcon(R.drawable.ic_action_back);
        getActionBar().setHomeButtonEnabled(true);

        dbOpen = new SQLiteOpen(this);
        try {
            db = dbOpen.getWritableDatabase();
        } catch (SQLiteException e) {
            e.printStackTrace();
            db = dbOpen.getReadableDatabase();
        }

        Cursor data = db.query("profiles", new String[] {"address", "user", "password", "localpath", "remotepath", "type"}, "name = '" + name + "'", null, null, null, null);
        data.moveToFirst();

        infoName = (TextView)findViewById(R.id.info_name);
        infoAddress = (TextView)findViewById(R.id.info_address);
        infoUser = (TextView)findViewById(R.id.info_user);
        infoLocalPath = (TextView)findViewById(R.id.info_localpath);
        infoRemotePath = (TextView)findViewById(R.id.info_remotepath);
        infoType = (TextView)findViewById(R.id.info_type);
        infoName.setText(name);
        infoAddress.setText(data.getString(0));
        infoUser.setText(data.getString(1));
        infoLocalPath.setText(data.getString(3));
        infoRemotePath.setText(data.getString(4));
        infoType.setText(data.getString(5));


        ftpClient.setAutodetectUTF8(true);

        downloadHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == -3) FTPProgressBar.incrementProgressBy(1);
                else if (msg.what == -1) FTPProgressBar.setIndeterminate(false);
                else if (msg.what == -2) {
                    FTPProgressBar.setIndeterminate(true);
                    FTPProgressBar.show();
                }
                else {
                    FTPProgressBar.setMax(msg.what);

                }
                if (FTPProgressBar.getProgress() == FTPProgressBar.getMax()) {
                    FTPProgressBar.dismiss();
                    Toast doneToast = Toast.makeText(getApplicationContext(), getString(R.string.done), Toast.LENGTH_SHORT);
                    doneToast.show();
                }

            }
        };

        increase = new Handler() {
            public void handleMessage(android.os.Message msg) {
                progressBar.incrementProgressBy(msg.what);
                if (progressBar.getProgress() == progressBar.getMax()) {
                    progressBar.dismiss();
                    Toast doneToast = Toast.makeText(getApplicationContext(), getString(R.string.done), Toast.LENGTH_SHORT);
                    doneToast.show();
                }
            }
        };


        close = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == 0) progressBar.dismiss();
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.info_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home :
                onBackPressed();
                break;
            case R.id.deletemenu :
                db.delete("profiles", "name = '" + name + "'", null);
                finish();
                break;
            case R.id.startmenu :
                Cursor types = db.query("profiles", new String[] {"type"}, "name = '" + name + "'", null, null, null, null);
                types.moveToFirst();
                if (types.getString(0).equals("download")) {
                    download(name);
                } else {
                    Cursor data = db.query("profiles", new String[] {"address", "user", "password", "localpath", "remotepath"}, "name = '" + name + "'", null, null, null, null);
                    data.moveToFirst();
                    Uploading(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4));
                }
                break;
        }
        return true;
    }
}
