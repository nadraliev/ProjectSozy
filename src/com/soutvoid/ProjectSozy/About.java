package com.soutvoid.ProjectSozy;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by andrew on 22.03.15.
 */
public class About extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        getActionBar().hide();
    }

}
