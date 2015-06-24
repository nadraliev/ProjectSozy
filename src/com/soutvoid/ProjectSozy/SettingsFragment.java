package com.soutvoid.ProjectSozy;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

/**
 * Created by andrew on 24.06.15.
 */
public class SettingsFragment extends Fragment {

    SharedPreferences sharedPreferences;
    View view;
    EditText interval;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.settingsfragment, container, false);
        sharedPreferences = MainActivity.sharedPreferences;
        interval = (EditText)view.findViewById(R.id.interval);
        try {
            interval.setText(sharedPreferences.getString("interval", "5"));
        } catch (Exception e) {
            interval.setText(10 + "");
        }
        return view;
    }

    public void onResume() {
        sharedPreferences = MainActivity.sharedPreferences;
        interval = (EditText)view.findViewById(R.id.interval);
        try {
            interval.setText(sharedPreferences.getString("interval", "5"));
        } catch (Exception e) {
            interval.setText(10 + "");
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("interval", interval.getText().toString());
        editor.apply();
        super.onDestroy();
    }
}
