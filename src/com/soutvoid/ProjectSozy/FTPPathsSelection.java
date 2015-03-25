package com.soutvoid.ProjectSozy;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.*;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;

/**
 * Created by andrew on 15.03.15.
 */
public class FTPPathsSelection extends ListActivity {

    private Button ok;
    private FTPClient ftpClient = new FTPClient();
    private ArrayList<String> directoryEntries = new ArrayList<String>();
    private String currentDirectory = "/";
    ArrayAdapter<String> directoryList;
    Handler printList;
    TextView abspath;
    boolean isFile = false;

    public void fill() throws IOException {
        directoryEntries.clear();
        FTPFile[] files = ftpClient.listFiles();

        for (FTPFile file : files) {
            directoryEntries.add(file.getName());
        }

        directoryList = new ArrayAdapter<String>(this, R.layout.row, this.directoryEntries);

        printList.sendEmptyMessage(1);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final int selectedRowID = position;
        final String selectedItem = directoryEntries.get(selectedRowID);


        Thread network = new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    ftpClient.connect(AddProfile.addressedit.toString());
                    ftpClient.login(AddProfile.useredit.toString(), AddProfile.passwordedit.toString());
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
                        ok(ok);
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

        isFile = false;
        ok = (Button)findViewById(R.id.okbutton);
        final Toast ConnectExceptionToast = Toast.makeText(getApplicationContext(), getString(R.string.connectexception), Toast.LENGTH_SHORT);
        abspath = (TextView)findViewById(R.id.ftpabsolutepathtitle);

        ftpClient.setAutodetectUTF8(true);

        printList = new Handler() {
            public void handleMessage(android.os.Message msg) {
                setListAdapter(directoryList);
                abspath.setText(currentDirectory);
            }
        };


        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ftpClient.connect(AddProfile.addressedit.toString());
                    ftpClient.login(AddProfile.useredit.toString(), AddProfile.passwordedit.toString());
                    ftpClient.changeWorkingDirectory(currentDirectory);
                    fill();
                    ftpClient.logout();
                    ftpClient.disconnect();
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

    public void actionbarback(View view) {
        onBackPressed();
    }

    public void ok(View view) {
        Intent i = new Intent();
        i.putExtra("isFile", isFile);
        i.putExtra("remotepathtext", currentDirectory);
        setResult(RESULT_OK, i);
        finish();
    }

}
