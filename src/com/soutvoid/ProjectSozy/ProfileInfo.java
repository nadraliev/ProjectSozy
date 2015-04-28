package com.soutvoid.ProjectSozy;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    TextView infoDay;
    TextView infoTime;

    SQLiteDatabase db;
    SQLiteOpen dbOpen;

    String name;

    Integer id;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_info);
        getActionBar().setIcon(R.drawable.ic_action_back);
        getActionBar().setHomeButtonEnabled(true);

        Intent intent = getIntent();
        name = intent.getStringExtra("name");

        dbOpen = new SQLiteOpen(this);
        try {
            db = dbOpen.getWritableDatabase();
        } catch (SQLiteException e) {
            e.printStackTrace();
            db = dbOpen.getReadableDatabase();
        }

        Cursor ids = db.query("profiles", new String[] {"_id"}, "name = '" + name + "'", null, null, null, null);
        ids.moveToFirst();

        id = ids.getInt(0);

        UpdateInfo();

    }

    public void onResume() {
        super.onResume();
        UpdateInfo();
    }

    public void UpdateInfo() {
        Cursor data = db.query("profiles", new String[] {"address", "user", "password", "localpath", "remotepath", "type", "name", "daynumber", "time"}, "_id = " + id, null, null, null, null);
        data.moveToFirst();

        //заполняем поля информацией
        infoName = (TextView)findViewById(R.id.info_name);
        infoAddress = (TextView)findViewById(R.id.info_address);
        infoUser = (TextView)findViewById(R.id.info_user);
        infoLocalPath = (TextView)findViewById(R.id.info_localpath);
        infoRemotePath = (TextView)findViewById(R.id.info_remotepath);
        infoType = (TextView)findViewById(R.id.info_type);
        infoDay = (TextView)findViewById(R.id.info_day);
        infoTime = (TextView)findViewById(R.id.info_time);
        infoName.setText(data.getString(6));
        infoAddress.setText(data.getString(0));
        infoUser.setText(data.getString(1));
        infoLocalPath.setText(data.getString(3));
        infoRemotePath.setText(data.getString(4));
        infoType.setText(data.getString(5));
        infoDay.setText(getApplicationContext().getResources().getStringArray(R.array.daysofweek)[data.getInt(7)]);
        infoTime.setText(data.getInt(8)/60 + ":" + data.getInt(8)%60/10 + "0");

        data.close();
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
                MainActivity.currentName = "";
                onBackPressed();
                break;
            case R.id.deletemenu :
                db.delete("profiles", "name = '" + name + "'", null);
                startService(new Intent(ProfileInfo.this, SyncService.class));
                finish();
                break;
            case R.id.editmenu :
                AddProfile.isChanging = true;
                Intent i = new Intent(ProfileInfo.this, AddProfile.class);
                startActivity(i);
                break;
            case R.id.startmenu :
                final Profile profile = new Profile(name);
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        profile.startProfile();
                    }
                });
                thread.start();
                break;
        }
        return true;
    }

}
