package com.soutvoid.ProjectSozy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by andrew on 17.03.15.
 */
public class Launch extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {                                   //Вызывается при создании активности
        super.onCreate(savedInstanceState);
        getActionBar().hide();
        setContentView(R.layout.launch);
        //Intent i = new Intent(this, MainActivity.class);
        //startActivity(i);
    }
}
