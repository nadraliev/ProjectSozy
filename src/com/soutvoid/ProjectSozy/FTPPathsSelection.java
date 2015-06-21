package com.soutvoid.ProjectSozy;

import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by andrew on 15.03.15.
 */
public class FTPPathsSelection extends ListActivity {

    private FTPClient ftpClient = new FTPClient();
    private ArrayList<String> directoryEntries = new ArrayList<String>();
    private String currentDirectory = "/";
    private ArrayAdapter<String> directoryList;
    private Handler printList;
    private TextView abspath;
    boolean isFile = false;
    private ArrayList<String> allFiles = new ArrayList<String>();
    private ArrayList<String> sizes = new ArrayList<String>();

    ProgressBar spinner;
    Handler progress;

    private Toast ConnectExceptionToast;

    private void fill() throws IOException {
        directoryEntries.clear();
        FTPFile[] files = ftpClient.listFiles();

        for (FTPFile file : files) {
            directoryEntries.add(file.getName());
        }

        directoryList = new ArrayAdapter<String>(this, R.layout.row, this.directoryEntries);

        printList.sendEmptyMessage(1);
        progress.sendEmptyMessage(0);
    }

    private void findAllFiles(String prefix) {
        try {
            for (FTPFile file : ftpClient.listFiles()) {
                if (!(file.getName().equals(".") || file.getName().equals(".."))) {
                    if (file.isDirectory()) {
                        ftpClient.changeWorkingDirectory(file.getName());
                        findAllFiles(prefix + "/" + file.getName());
                        ftpClient.changeToParentDirectory();
                    } else {
                        sendTextOnNotif(prefix + "/" + file.getName(),3);
                        allFiles.add(prefix + "/" + file.getName());
                        sizes.add(Profile.md5(file.getSize() + ""));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendTextOnNotif(String input, int id) {

        Context context = MainActivity.context;

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

    private void sendResult() {
        final Intent i = new Intent();
        allFiles.clear();
        sizes.clear();
        if (isFile) {
            allFiles.add(currentDirectory.substring(currentDirectory.lastIndexOf("/") + 1));
            try {
                sizes.add(Profile.md5(ftpClient.mlistFile(currentDirectory).getSize() + ""));
            } catch (Exception e) {
                e.printStackTrace();
            }
            currentDirectory = currentDirectory.substring(0, currentDirectory.lastIndexOf("/"));
            i.putExtra("files", allFiles);
            i.putExtra("sizes", sizes);
            i.putExtra("path", currentDirectory);
            setResult(RESULT_OK, i);
            finish();
        } else {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ftpClient = new FTPClient();
                        ftpClient.setAutodetectUTF8(true);
                        ftpClient.connect(AddProfile.addressedit.getText().toString().trim());
                        ftpClient.login(AddProfile.useredit.getText().toString().trim(), AddProfile.passwordedit.getText().toString().trim());
                        ftpClient.changeWorkingDirectory(currentDirectory);
                        findAllFiles("");
                        ftpClient.logout();
                        ftpClient.disconnect();
                        i.putExtra("files", allFiles);
                        i.putExtra("sizes", sizes);
                        i.putExtra("path", currentDirectory);
                        setResult(RESULT_OK, i);
                        finish();
                    } catch (ConnectException e) {
                        e.printStackTrace();
                        ConnectExceptionToast.show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        ConnectExceptionToast.show();
                    }
                }
            });
            thread.start();

        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final int selectedRowID = position;
        final String selectedItem = directoryEntries.get(selectedRowID);
        progress.sendEmptyMessage(1);


        Thread network = new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    ftpClient.connect(AddProfile.addressedit.getText().toString().trim());
                    ftpClient.login(AddProfile.useredit.getText().toString().trim(), AddProfile.passwordedit.getText().toString().trim());
                    ftpClient.changeWorkingDirectory(currentDirectory);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (selectedItem.equals("..")) {
                    try {
                        ftpClient.changeToParentDirectory();
                        currentDirectory = ftpClient.printWorkingDirectory();
                        fill();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (selectedItem.equals(".")) {
                    currentDirectory = "/";
                    try {
                        ftpClient.changeWorkingDirectory("/");
                        fill();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if (ftpClient.listFiles()[selectedRowID].isDirectory()) {
                        if (!selectedItem.equals(".") && !selectedItem.equals("..")) {
                            if (currentDirectory.equals("/"))
                                currentDirectory = currentDirectory.concat(selectedItem);
                            else currentDirectory = currentDirectory.concat("/" + selectedItem);
                            ftpClient.changeWorkingDirectory(currentDirectory);
                            fill();
                        }
                    } else {
                        currentDirectory = currentDirectory.concat("/" + selectedItem);
                        isFile = true;
                        sendResult();
                        finish();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    ftpClient.logout();
                    ftpClient.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });
        network.start();

    }


    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.ftppathsselection);
        getActionBar().setIcon(R.drawable.ic_action_back);
        getActionBar().setHomeButtonEnabled(true);

        spinner = (ProgressBar)findViewById(R.id.spinner);
        spinner.setVisibility(View.INVISIBLE);
        spinner.setIndeterminate(true);

        progress = new Handler() {
            public void handleMessage(android.os.Message message) {
                if (message.what == 1) spinner.setVisibility(View.VISIBLE);
                else spinner.setVisibility(View.INVISIBLE);
            }
        };

        isFile = false;
        ConnectExceptionToast = Toast.makeText(getApplicationContext(), getString(R.string.connectexception), Toast.LENGTH_SHORT);
        abspath = (TextView)findViewById(R.id.ftpabsolutepathtitle);

        ftpClient.setAutodetectUTF8(true);

        printList = new Handler() {
            public void handleMessage(android.os.Message msg) {
                setListAdapter(directoryList);
                abspath.setText(currentDirectory);
            }
        };

        progress.sendEmptyMessage(1);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ftpClient.connect(AddProfile.addressedit.getText().toString().trim());
                    ftpClient.login(AddProfile.useredit.getText().toString().trim(), AddProfile.passwordedit.getText().toString().trim());
                    ftpClient.changeWorkingDirectory(currentDirectory);
                    fill();
                    ftpClient.logout();
                    ftpClient.disconnect();
                } catch (ConnectException e) {
                    e.printStackTrace();
                    ConnectExceptionToast.show();
                    progress.sendEmptyMessage(0);
                } catch (IOException e) {
                    e.printStackTrace();
                    ConnectExceptionToast.show();
                    progress.sendEmptyMessage(0);
                }
            }
        });
        thread.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_profile_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.ok_action:
                sendResult();
                break;
        }
        return true;
    }


}
