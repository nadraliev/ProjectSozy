package com.soutvoid.ProjectSozy;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPFile;

import java.io.*;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Set;

public class MainActivity extends Activity {

    ProgressDialog progressBar;
    ProgressDialog FTPProgressBar;
    Integer filesCount;
    Integer FTPFilesCount = 0;
    Handler increase;
    Handler close;
    TextView test;
    Handler downloadHandler;
    FTPClient ftpClient = new FTPClient();
    Boolean FTPIsDir;

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
                } catch (UnknownHostException e) {
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

    public void download(FTPFile[] files, final String destination) throws IOException {
        String currentDir = ftpClient.printWorkingDirectory();
        for (FTPFile file : files) {
            if (!file.getName().equals(".") && !file.getName().equals("..")) {
                if (file.isDirectory()) {
                    new File(destination + "/" + file.getName()).mkdir();
                    ftpClient.changeWorkingDirectory(ftpClient.printWorkingDirectory() + "/" + file.getName());
                    download(ftpClient.listFiles(), destination + "/" + file.getName());
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




    @Override
    protected void onCreate(Bundle savedInstanceState) {                                   //Вызывается при создании активности
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        getActionBar().hide();


        final Button UploadButton = (Button)findViewById(R.id.uploadbutton);                //кнопка выгрузки
        final Editable LocalPath = ((EditText)findViewById(R.id.localpathold)).getText();
        final Editable RemotePath = ((EditText)findViewById(R.id.remotepathold)).getText();
        test = (TextView)findViewById(R.id.test);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.launching);
        final ImageView launch = (ImageView)findViewById(R.id.launch);
        launch.startAnimation(anim);
        Animation.AnimationListener alpha = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                launch.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };
        alpha.onAnimationEnd(anim);

        ftpClient.setAutodetectUTF8(true);



        //increase для выгрузки файлов
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

        close = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == 0) progressBar.dismiss();
            }
        };


        UploadButton.setOnClickListener(new View.OnClickListener() {                    //Создаем обработчик нажатия для кнопки выгрузки
            @Override
            public void onClick(View v) {

                //во избежание запуска нескольких выгрузок выключаем кнопку до конца загрузки
                UploadButton.setEnabled(false);

                if (!LocalPath.toString().equals("") && !RemotePath.toString().equals("")) {
                    Uploading(Settings.FTP.getAddress(), Settings.FTP.getUser(), Settings.FTP.getPassword(), "/storage/emulated/0/" + LocalPath.toString(), "/" + RemotePath.toString());
                }
                else {
                    if (LocalPath.toString().equals("")) {
                        String InputLocalPath = getString(R.string.inputlocalpath);
                        Toast InputLocalPathToast = Toast.makeText(getApplicationContext(), InputLocalPath, Toast.LENGTH_SHORT);
                        InputLocalPathToast.show();
                    }
                    if (RemotePath.toString().equals("")) {
                        String InputRemotePath = getString(R.string.inputremotepath);
                        Toast InputRemotePathToast = Toast.makeText(getApplicationContext(), InputRemotePath, Toast.LENGTH_SHORT);
                        InputRemotePathToast.show();
                    }
                }

                UploadButton.setEnabled(true);
            }
        });
    }

    public void settingsOnClick(View view) {
        Intent Settings = new Intent(MainActivity.this, Settings.class);
        startActivity(Settings);
    }

    public void pathsselectionOnCLick(View view) {
        Intent PathSelection = new Intent(MainActivity.this, PathsSelection.class);
        startActivity(PathSelection);
    }

    public void profiles(View view) {
        Intent profies = new Intent(MainActivity.this, Profiles.class);
        startActivity(profies);
    }

    public void download(View view) {
        final String localpath = ((EditText)findViewById(R.id.localpathold)).getText().toString();
        final String remotepath = ((EditText)findViewById(R.id.remotepathold)).getText().toString();
        FTPProgressBar = new ProgressDialog(this);
        FTPProgressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        FTPProgressBar.setMax(0);
        FTPProgressBar.setIndeterminate(true);
        FTPProgressBar.show();
        final Thread download = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ftpClient.connect(Settings.FTP.getAddress());
                    ftpClient.login(Settings.FTP.getUser(), Settings.FTP.getPassword());
                    if (ftpClient.changeWorkingDirectory(remotepath)) {
                        FTPFilesCount(ftpClient.listFiles());
                        downloadHandler.sendEmptyMessage(FTPFilesCount);
                        FTPFilesCount = 0;
                        download(ftpClient.listFiles(), "/storage/emulated/0/" + localpath);
                    } else {
                        downloadHandler.sendEmptyMessage(1);
                        downloadHandler.sendEmptyMessage(-1);
                        ftpClient.changeWorkingDirectory(remotepath.substring(0, remotepath.lastIndexOf("/")));
                        BufferedOutputStream buffout = new BufferedOutputStream(new FileOutputStream("/storage/emulated/0/" + localpath + remotepath.substring(remotepath.lastIndexOf("/"))));
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

}
