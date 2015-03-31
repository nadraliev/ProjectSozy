package com.soutvoid.ProjectSozy;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by andrew on 15.03.15.
 */
public class MainActivity extends Activity {

    final String ATTRIBUTE_NAME = "name";
    final String ATTRIBUTE_LOCALPATH = "localpath";
    final String ATTRIBUTE_SERVER = "server";
    final String ATTRIBUTE_TYPE = "type";
    public static String currentName;                                       //TODO избавиться от всех переопределений строк!

    ListView profileslist;

    SQLiteDatabase db;
    SQLiteOpen dbOpen;

    ArrayList<String> names = new ArrayList<String>();

    String[] navigationDrawerItems;
    DrawerLayout navigationDrawer;
    ListView navigationDrawerList;
    int[] navigationDrawerIcons;
    int[] navigationDrawerIconsPurple;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profiles);
        getActionBar().setIcon(R.drawable.ic_title);
        getActionBar().setTitle("");

        String itemName = getResources().getStringArray(R.array.navigationdrawer)[0];

        navigationDrawerItems = getResources().getStringArray(R.array.navigationdrawer);
        navigationDrawer = (DrawerLayout)findViewById(R.id.drawer_layout);
        navigationDrawerList = (ListView)findViewById(R.id.left_drawer);
        navigationDrawerIcons = new int[] {R.drawable.ic_list};
        navigationDrawerIconsPurple = new int[] {R.drawable.ic_list_purple};

        navigationDrawer.setDrawerShadow(R.drawable.drawer_shadow, 20);


        ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>(navigationDrawerItems.length);

        Map<String, Object> map;
        for (int i = 0; i < navigationDrawerItems.length; i++) {
            map = new HashMap<String, Object>();
            if (navigationDrawerItems[i].equals(itemName)) {
                map.put("icon", navigationDrawerIconsPurple[i]);
                map.put("background", R.drawable.grey_light_background);
            }
            else map.put("icon", navigationDrawerIcons[i]);
            map.put("text", navigationDrawerItems[i]);
            data.add(map);
        }

        String[] from = {"icon", "text", "background"};
        int[] to = {R.id.drawericon, R.id.drawertext, R.id.drawerbackground};

        SimpleAdapter drawerAdapter = new SimpleAdapter(this, data, R.layout.drawerlistitem, from, to);
        navigationDrawerList.setAdapter(drawerAdapter);
        navigationDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0 :
                        navigationDrawerList.setItemChecked(position, true);
                        navigationDrawer.closeDrawer(navigationDrawerList);
                        break;
                }
            }
        });

        dbOpen = new SQLiteOpen(this);
        try {
            db = dbOpen.getWritableDatabase();
        } catch (SQLiteException e) {
            e.printStackTrace();
            db = dbOpen.getReadableDatabase();
        }

        UpdateList();

        profileslist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentName = names.get(position);
                Intent i = new Intent(MainActivity.this, ProfileInfo.class).putExtra("name", currentName);
                startActivity(i);
                UpdateList();
            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();
        UpdateList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about_action :
                Intent i = new Intent(MainActivity.this, About.class);
                startActivity(i);
                break;
        }
        return true;
    }

    public void UpdateList() {

        names.clear();

        Cursor profiles = db.query("profiles", new String[] {"name", "localpath", "address", "type"}, null, null, null, null, null);
        profiles.moveToFirst();

        ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>(profiles.getCount());
        Map<String, Object> map;

        for (int i = 0; i < profiles.getCount(); i++) {
            map = new HashMap<String, Object>();
            names.add(profiles.getString(0));
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

    public void addprofilebutton(View view) {
        Intent newprofile = new Intent(this, AddProfile.class);
        startActivity(newprofile);
    }

}
