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
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPFile;

import java.io.*;
import java.net.ConnectException;
import java.net.UnknownHostException;

public class MainActivity extends Activity {

    ProgressDialog progressBar;
    ProgressDialog FTPProgressBar;
    Integer filesCount;
    Integer FTPFilesCount;
    Handler increase;
    Handler close;
    TextView test;
    Handler download;

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
    public boolean FTPHasDirectory(FTPClient ftpClient) {
        boolean flag = false;
        try {
            FTPFile[] files = ftpClient.listFiles();
            for (FTPFile file : files) {
                if (file.isDirectory()) flag = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return flag;
    }

    public void mkDir(final String dirName, final String destination) {
        boolean isContains = false;
        File[] files = (new File(destination)).listFiles();
        if (files.length != 0) {
            for (File file : files) {
                if (dirName.equals(file.getName())) isContains = true;
            }
        }
        if (!isContains) {
            new File(destination).mkdirs();
        }
    }

    public void FTPFilesCount(FTPClient ftpClient) {  //после подсчета меняет рабочую директорию!!
        try {
            for (int counter = 0; counter < ftpClient.listFiles().length; counter++) {
                if (ftpClient.listFiles()[counter].isDirectory()) {
                    ftpClient.changeWorkingDirectory(ftpClient.listFiles()[counter].toString());
                    FTPFilesCount(ftpClient);
                } else FTPFilesCount++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean FTPIsDirectory(final String target, FTPClient ftpClient) {
        boolean flag = false;
        try {
            FTPFile[] files = ftpClient.listFiles();
            for (FTPFile file : files) {
                if (file.toString().equals(target)) {
                    if (file.isDirectory()) flag = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return flag;
    }

    public void downloadFromFTPServer(final String address, final String user, final String password, final String target, final String destination, final FTPClient ftpClient) throws IOException {
        if (ftpClient.changeWorkingDirectory(target))
        mkDir(new File(target).getName(), destination);
        else
        ftpClient.changeWorkingDirectory(target.substring(0, target.length() - 1 - new File(target).getName().length()));

        if (FTPHasDirectory(ftpClient)) {
            FTPFile[] files = ftpClient.listFiles();
            for (FTPFile file : files) {
                if (FTPIsDirectory(file.toString(), ftpClient)) {
                    downloadFromFTPServer(address, user, password, file.toString(), destination + "/" + new File(target).getName().toString(), ftpClient);
                }
            }
        }

        FTPProgressBar.setIndeterminate(false);
        try {
            BufferedOutputStream buffout = null;
            if (FTPIsDirectory(target, ftpClient)) {
                FTPFile[] files = ftpClient.listFiles();
                for (FTPFile file : files) {
                    if (!file.isDirectory()) {
                        buffout = new BufferedOutputStream(new FileOutputStream(destination + "/" + file.getName()));
                        ftpClient.retrieveFile(file.toString(), buffout);
                        download.sendEmptyMessage(0);
                    }
                }
            } else {
                buffout = new BufferedOutputStream(new FileOutputStream(destination + "/" + new File(target).getName().toString()));
                ftpClient.retrieveFile(target, buffout);
                download.sendEmptyMessage(0);
            }
            buffout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void downloading(final String address, final String user, final String password, final String target, final String destination) {
        FTPFilesCount = 0;
        final FTPClient ftpClient = new FTPClient();

        FTPProgressBar = new ProgressDialog(this);
        FTPProgressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        Thread download = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ftpClient.connect(address);
                    ftpClient.login(user, password);
                    if (ftpClient.changeWorkingDirectory(target))
                        ftpClient.changeWorkingDirectory(target);
                    else
                        ftpClient.changeWorkingDirectory(target.substring(0, target.length() - 1 - new File(target).getName().length()));
                    if (!new File(target).isDirectory())
                        FTPFilesCount = 1;
                    else {
                        FTPFilesCount(ftpClient);
                    }

                    downloadFromFTPServer(address, user, password, target, destination, ftpClient);

                    ftpClient.logout();
                    ftpClient.disconnect();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        download.start();
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

        //increase для выгрузки файлов
        increase = new Handler() {
            public void handleMessage(android.os.Message msg) {
                progressBar.incrementProgressBy(msg.what);
                if (progressBar.getProgress() == progressBar.getMax()) progressBar.dismiss();
            }
        };

        download = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == 0) FTPProgressBar.incrementProgressBy(1);
                else {
                    FTPProgressBar.setMax(msg.what);
                    FTPProgressBar.setIndeterminate(true);
                    FTPProgressBar.show();
                }
                if (FTPProgressBar.getProgress() == FTPProgressBar.getMax()) FTPProgressBar.dismiss();
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
        String localpath = ((EditText)findViewById(R.id.localpathold)).getText().toString();
        String remotepath = ((EditText)findViewById(R.id.remotepathold)).getText().toString();
        downloading(Settings.FTP.getAddress(), Settings.FTP.getUser(), Settings.FTP.getPassword(), remotepath, "/storage/emulated/0/" + localpath);
    }

}
