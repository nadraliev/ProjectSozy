package com.soutvoid.ProjectSozy;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
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

    public static Context context;

    public static String currentName;                                       //TODO избавиться от всех переопределений строк!


    ArrayList<String> names = new ArrayList<String>();

    String[] navigationDrawerItems;
    DrawerLayout navigationDrawer;
    ListView navigationDrawerList;
    int[] navigationDrawerIcons;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profiles);
        getActionBar().setIcon(R.drawable.ic_title);
        getActionBar().setTitle("");

        context = getApplicationContext();

        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = new ProfilesFragment();
        fragmentManager.beginTransaction()
                .add(R.id.main_container, fragment)
                .commit();


        navigationDrawerItems = getResources().getStringArray(R.array.navigationdrawer);
        navigationDrawer = (DrawerLayout)findViewById(R.id.drawer_layout);
        navigationDrawerList = (ListView)findViewById(R.id.left_drawer);
        navigationDrawerIcons = new int[] {R.drawable.ic_list, R.drawable.ic_processing};

        navigationDrawer.setDrawerShadow(R.drawable.drawer_shadow, 20);


        ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>(navigationDrawerItems.length);

        Map<String, Object> map;
        for (int i = 0; i < navigationDrawerItems.length; i++) {
            map = new HashMap<String, Object>();
            map.put("background", R.drawable.white_background);
            map.put("icon", navigationDrawerIcons[i]);
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
                Fragment fragment;
                FragmentManager fragmentManager;
                switch (position) {
                    case 0 :
                        fragment = new ProfilesFragment();

                        fragmentManager = getFragmentManager();
                        fragmentManager.beginTransaction()
                                .replace(R.id.main_container, fragment)
                                .commit();

                        navigationDrawerList.setItemChecked(position, true);
                        navigationDrawer.closeDrawer(navigationDrawerList);
                        setTitle(getResources().getStringArray(R.array.navigationdrawer)[0]);
                        break;
                    case 1 :
                        fragment = new ProcessesFragment();

                        fragmentManager = getFragmentManager();
                        fragmentManager.beginTransaction()
                                .replace(R.id.main_container, fragment)
                                .commit();

                        navigationDrawerList.setItemChecked(position, true);
                        navigationDrawer.closeDrawer(navigationDrawerList);
                        setTitle(getResources().getStringArray(R.array.navigationdrawer)[1]);
                        break;
                }
            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();
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



    public void addprofilebutton(View view) {
        Intent newprofile = new Intent(this, AddProfile.class);
        startActivity(newprofile);
    }

}
