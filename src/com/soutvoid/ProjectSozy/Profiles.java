package com.soutvoid.ProjectSozy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

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

    ListView profileslist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profiles);

        String[] names = {"First", "Second"};
        String[] localpaths = {"Music", "Ringtones"};
        String[] servers = {"192.168.100.10", "soutvoid.ddns.net"};

        ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>(names.length);
        Map<String, Object> map;

        for (int i = 0; i < names.length; i++) {
            map = new HashMap<String, Object>();
            map.put(ATTRIBUTE_NAME, names[i]);
            map.put(ATTRIBUTE_LOCALPATH, localpaths[i]);
            map.put(ATTRIBUTE_SERVER, servers[i]);
            data.add(map);
        }

        String[] from = {ATTRIBUTE_NAME, ATTRIBUTE_LOCALPATH, ATTRIBUTE_SERVER};
        int[] to = {R.id.profilenameitem, R.id.localpathitem, R.id.serveraddressitem};

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

}
