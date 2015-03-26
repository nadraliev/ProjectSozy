package com.soutvoid.ProjectSozy;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by andrew on 15.03.15.
 */
public class Profiles extends Activity {

    final String ATTRIBUTE_NAME = "name";
    final String ATTRIBUTE_LOCALPATH = "localpath";
    final String ATTRIBUTE_SERVER = "server";
    final String ATTRIBUTE_TYPE = "type";

    ListView profileslist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profiles);

        UpdateList();
    }

    @Override
    public void onResume() {
        super.onResume();
        UpdateList();
    }

    public void UpdateList() {
        SQLiteOpen dbOpen = new SQLiteOpen(this);
        SQLiteDatabase db;
        try {
            db = dbOpen.getWritableDatabase();
        } catch (SQLiteException e) {
            e.printStackTrace();
            db = dbOpen.getReadableDatabase();
        }

        Cursor profiles = db.query("profiles", new String[] {"name", "localpath", "address", "type"}, null, null, null, null, null);
        profiles.moveToFirst();

        ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>(profiles.getCount());
        Map<String, Object> map;

        for (int i = 0; i < profiles.getCount(); i++) {
            map = new HashMap<String, Object>();
            map.put(ATTRIBUTE_NAME, profiles.getString(0));
            map.put(ATTRIBUTE_LOCALPATH, profiles.getString(1).substring(profiles.getString(1).lastIndexOf("/") + 1));   //отрезаем все кроме имени папки
            map.put(ATTRIBUTE_SERVER, profiles.getString(2));
            if (profiles.getString(3).equals("upload"))
                map.put(ATTRIBUTE_TYPE, R.drawable.ic_rightarrow);
            else map.put(ATTRIBUTE_TYPE, R.drawable.ic_lefttarrow);
            data.add(map);
            profiles.moveToNext();
        }
        profiles.close();


        String[] from = {ATTRIBUTE_NAME, ATTRIBUTE_LOCALPATH, ATTRIBUTE_SERVER, ATTRIBUTE_TYPE};
        int[] to = {R.id.profilenameitem, R.id.localpathitem, R.id.serveraddressitem, R.id.typeitem};

        SimpleAdapter ProfilesAdapter = new SimpleAdapter(this, data, R.layout.listitemprofiles, from, to);

        profileslist = (ListView)findViewById(R.id.profileslist);
        profileslist.setAdapter(ProfilesAdapter);
    }

    public void profileactionbarback(View view) {
        onBackPressed();
    }

    public void addprofilebutton(View view) {
        Intent newprofile = new Intent(this, AddProfile.class);
        startActivity(newprofile);
    }

    public void popupmenu(View view) {
        showPopupMenu(view);
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.inflate(R.menu.popupmenufile);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.deletemenu :
                        Toast chk = Toast.makeText(getApplicationContext(), "CHECK", Toast.LENGTH_SHORT);
                        chk.show();
                }
                return true;
            }
        });
        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {

            @Override
            public void onDismiss(PopupMenu menu) {

            }
        });

        popupMenu.show();
    }

}
