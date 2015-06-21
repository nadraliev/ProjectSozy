package com.soutvoid.ProjectSozy;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by andrew on 31.03.15.
 */
public class ProfilesFragment extends android.app.Fragment {
    final String ATTRIBUTE_NAME = "name";
    final String ATTRIBUTE_LOCALPATH = "localpath";
    final String ATTRIBUTE_SERVER = "server";
    final String ATTRIBUTE_TYPE = "type";

    SQLiteDatabase db;
    SQLiteOpenProfiles dbOpen;

    ListView profileslist;

    ArrayList<String> names = new ArrayList<String>();

    View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.profiles_fragment, container, false);
        UpdateList();
        profileslist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MainActivity.currentName = names.get(position);
                Intent i = new Intent(MainActivity.context, ProfileInfo.class).putExtra("name", MainActivity.currentName);
                startActivity(i);
            }
        });
        return rootView;
    }

    public void onResume() {
        super.onResume();
        UpdateList();
    }

    public void UpdateList() {

        dbOpen = new SQLiteOpenProfiles(MainActivity.context);
        try {
            db = dbOpen.getWritableDatabase();
        } catch (SQLiteException e) {
            e.printStackTrace();
            db = dbOpen.getReadableDatabase();
        }

        names.clear();

        Cursor profiles = db.query("profiles", new String[] {"name", "path", "address", "type"}, null, null, null, null, null);
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

        SimpleAdapter ProfilesAdapter = new SimpleAdapter(MainActivity.context, data, R.layout.listitemprofiles, from, to);

        profileslist = (ListView)rootView.findViewById(R.id.profileslist);
        profileslist.setAdapter(ProfilesAdapter);
    }
}
