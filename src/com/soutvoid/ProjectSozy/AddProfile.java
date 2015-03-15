package com.soutvoid.ProjectSozy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.File;

/**
 * Created by andrew on 15.03.15.
 */
public class AddProfile extends Activity {

    String LocalPath = "";
    String RemotePath = "";
    TextView localpathtext;
    TextView remotepath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addprofile);
        getActionBar().hide();

        localpathtext = (TextView)findViewById(R.id.localpath);
        remotepath = (TextView)findViewById(R.id.remotepath);


    }

    public void actionbarback(View view) {
        onBackPressed();
    }

    public void chooselocalpath(View view) {
        Intent i = new Intent(this, PathsSelection.class);
        startActivityForResult(i, 1);
    }

    public void chooseremotepath(View view) {
        Intent i = new Intent(this, FTPPathsSelection.class);     //TODO сделать активити выбора папки на сервере
        startActivityForResult(i, 2);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (data == null) {
                return;
            }
            LocalPath = data.getStringExtra("path");
            localpathtext.setText((CharSequence) new File(LocalPath).getName());
        } else {
            if (data == null) {
                return;
            }
            RemotePath = data.getStringExtra("path");
            remotepath.setText((CharSequence) new File(RemotePath).getName());
        }
    }

}
