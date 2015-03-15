package com.soutvoid.ProjectSozy;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.view.View;
import android.widget.*;
import android.content.DialogInterface;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrew on 15.03.15.
 */
public class FTPPathsSelection extends ListActivity {

    private Button ok;
    private FTPClient ftpClient;
    private ArrayList<String> directoryEntries = new ArrayList<String>();
    private String currentDirectory;
    ArrayAdapter<String> directoryList;

    public void fill() throws IOException {
        directoryEntries.clear();
        if (ftpClient.changeToParentDirectory()) {
            directoryEntries.add("..");
        }
        ftpClient.changeWorkingDirectory(currentDirectory);
        FTPFile[] files = ftpClient.listFiles();

        for (FTPFile file : files) {
            directoryEntries.add(file.getLink());
        }

        directoryList = new ArrayAdapter<String>(this, R.layout.row, this.directoryEntries);

    }



    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.ftppathsselection);
        getActionBar().hide();

        ok = (Button)findViewById(R.id.okbutton);

        ftpClient = new FTPClient();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ftpClient.connect(Settings.FTP.getAddress());
                    ftpClient.login(Settings.FTP.getUser(), Settings.FTP.getPassword());
                    currentDirectory = "/public";
                    ftpClient.changeWorkingDirectory(currentDirectory);
                    fill();
                    ftpClient.logout();
                    ftpClient.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        this.setListAdapter(directoryList);
    }

    public void actionbarback(View view) {
        onBackPressed();
    }



}
