package com.soutvoid.ProjectSozy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by andrew on 15.03.15.
 */
public class AddProfile extends Activity {

    String LocalPath = "";
    String RemotePath = "";
    TextView localpathtext;
    TextView remotepathtext;
    String address;
    String user;
    String password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addprofile);
        getActionBar().hide();

        localpathtext = (TextView)findViewById(R.id.localpath);
        remotepathtext = (TextView)findViewById(R.id.remotepath);
        Editable addressedit = ((EditText)findViewById(R.id.addredit)).getText();
        Editable useredit = ((EditText)findViewById(R.id.useredit)).getText();
        Editable passwordedit = ((EditText)findViewById(R.id.passwdedit)).getText();

        //инициализируем базу данных
        String driverName = "org.sqlite.JDBC";
        try {
            Class.forName(driverName).newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:res/profiles.db");
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void actionbarback(View view) {
        onBackPressed();
    }

    public void chooselocalpath(View view) {
        Intent i = new Intent(this, PathsSelection.class);
        startActivityForResult(i, 1);
    }

    public void chooseremotepath(View view) {
        Intent i = new Intent(this, FTPPathsSelection.class);
        startActivityForResult(i, 2);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (data == null) {
                return;
            }
            LocalPath = data.getStringExtra("path");
            localpathtext.setText((CharSequence) new File(LocalPath).getName());
        }
        if (requestCode == 2) {
            if (data == null) {
                return;
            }
            RemotePath = data.getStringExtra("remotepathtext");
            remotepathtext.setText((CharSequence) new File(RemotePath).getName());
        }
        }


}
