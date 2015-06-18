package com.soutvoid.ProjectSozy;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;

import java.io.File;

/**
 * Created by andrew on 15.03.15.
 */
public class AddProfile extends Activity {

    String LocalPath = "";
    String RemotePath = "";
    TextView localpathtext;
    TextView remotepathtext;
    public static EditText addressedit;
    public static EditText useredit;
    public static EditText passwordedit;
    public static EditText nameedit;
    boolean isUploading = true;
    boolean isFTPFile;
    public static boolean isChanging = false;    //если true, то активность вызвана, чтобы изменить профиль
    String syncType;

    Integer currentId;
    Integer day;
    Integer time;

    SQLiteOpenProfiles dbOpen;
    SQLiteDatabase db;

    Spinner daysSpinner;
    Spinner hoursSpinner;
    Spinner minutesSpinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addprofile);
        getActionBar().setIcon(R.drawable.ic_action_back);
        getActionBar().setHomeButtonEnabled(true);

        currentId = 0;

        if(isChanging) getActionBar().setTitle(R.string.edit);

        dbOpen = new SQLiteOpenProfiles(this);
        try {
            db = dbOpen.getWritableDatabase();
        } catch (SQLiteException e) {
            e.printStackTrace();
            db = dbOpen.getReadableDatabase();
        }

        isFTPFile = false;
        localpathtext = (TextView)findViewById(R.id.localpath);
        remotepathtext = (TextView)findViewById(R.id.remotepath);
        addressedit = ((EditText)findViewById(R.id.addredit));
        useredit = ((EditText)findViewById(R.id.useredit));
        passwordedit = ((EditText)findViewById(R.id.passwdedit));
        nameedit = ((EditText)findViewById(R.id.nameedit));
        daysSpinner = (Spinner)findViewById(R.id.days);
        hoursSpinner = (Spinner)findViewById(R.id.hours);
        minutesSpinner = (Spinner)findViewById(R.id.minutes);

        //Инициализация спиннера дней недели
        ArrayAdapter<CharSequence> days = ArrayAdapter.createFromResource(this, R.array.daysofweek, R.layout.spinner_row);
        days.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daysSpinner.setAdapter(days);
        day = 0;
        daysSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition, long selectedId) {
                day = selectedItemPosition;
            }

            public void onNothingSelected(AdapterView<?> parent) {
                //ничего не делать
            }
        });

        //инициализация спиннеров времени
        ArrayAdapter<CharSequence> hours = ArrayAdapter.createFromResource(this, R.array.hours, R.layout.spinner_row);
        ArrayAdapter<CharSequence> minutes = ArrayAdapter.createFromResource(this, R.array.minutes, R.layout.spinner_row);
        hours.setDropDownViewResource(R.layout.dropdown_spinner_item);
        minutes.setDropDownViewResource(R.layout.dropdown_spinner_item);
        hoursSpinner.setAdapter(hours);
        minutesSpinner.setAdapter(minutes);
        hoursSpinner.setSelection(22);
        minutesSpinner.setSelection(0);
        time = timeToMinutes(22, 0);
        hoursSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition, long selectedId) {
                hoursSpinner.setSelection(selectedItemPosition);
            }

            public void onNothingSelected(AdapterView<?> parent) {
                //ничего не делать
            }
        });
        minutesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition, long selectedId) {
                minutesSpinner.setSelection(selectedItemPosition);
            }

            public void onNothingSelected(AdapterView<?> parent) {
                //ничего не делать
            }
        });


        if(isChanging) {     //заполнить поля текущими данными, если профиль изменяется
            Cursor data = db.query("profiles", new String[] {"_id", "name", "address", "user", "password", "localpath", "remotepath", "type", "daynumber", "time"}, "name = '" + MainActivity.currentName + "'", null, null, null, null);
            data.moveToFirst();
            currentId = data.getInt(0);
            localpathtext.setText(data.getString(5).substring(data.getString(5).lastIndexOf("/") + 1));
            remotepathtext.setText(data.getString(6).substring(data.getString(6).lastIndexOf("/") + 1));
            addressedit.setText(data.getString(2));
            useredit.setText(data.getString(3));
            passwordedit.setText(data.getString(4));
            nameedit.setText(data.getString(1));
            daysSpinner.setSelection(data.getInt(8));
            hoursSpinner.setSelection(data.getInt(9)/60);
            minutesSpinner.setSelection(data.getInt(9)%60/10);

            LocalPath = data.getString(5);
            RemotePath = data.getString(6);

            if (data.getString(7).equals("upload")) isUploading = true;
            else {
                isUploading = false;
                ImageView arrow = (ImageView)findViewById(R.id.arrow);
                arrow.setImageResource(R.drawable.ic_lefttarrow);
            }
            data.close();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_profile_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home :
                isChanging = false;
                onBackPressed();
                break;
            case R.id.ok_action :
                //проверка, что путь назначения - не файл
                if (isUploading && isFTPFile) {
                    Toast chooseAnother = Toast.makeText(getApplicationContext(), getString(R.string.wrongdestination), Toast.LENGTH_LONG);
                    chooseAnother.show();
                } else if (!isUploading && (!new File(LocalPath).isDirectory())) {
                    Toast chooseAnother = Toast.makeText(getApplicationContext(), getString(R.string.wrongdestination), Toast.LENGTH_LONG);
                    chooseAnother.show();
                } else {

                    if (nameedit.getText().toString().trim().equals("") || addressedit.getText().toString().trim().equals("") || useredit.getText().toString().trim().equals("") || passwordedit.getText().toString().trim().equals("") ||
                            LocalPath.equals("") || RemotePath.equals("")) {
                        Toast emptyField = Toast.makeText(getApplicationContext(), getString(R.string.emptyField), Toast.LENGTH_LONG);
                        emptyField.show();
                    } else {

                        if (db.query("profiles", new String[]{"name"}, "name = '" + nameedit.getText().toString() + "'", null, null, null, null).getCount() == 0 || (isChanging && MainActivity.currentName.equals(nameedit.getText().toString()))) {

                            time = hoursSpinner.getSelectedItemPosition()*60 + minutesSpinner.getSelectedItemPosition()*10;
                            if(!isChanging) {

                                LocalPath = LocalPath.substring(1);
                                LocalPath = LocalPath.substring(LocalPath.indexOf("/"));

                                ContentValues newValues = new ContentValues();
                                newValues.put("name", nameedit.getText().toString());
                                newValues.put("address", addressedit.getText().toString());
                                newValues.put("user", useredit.getText().toString());
                                newValues.put("password", passwordedit.getText().toString());
                                newValues.put("localpath", "/storage/emulated/0" + LocalPath);
                                newValues.put("remotepath", RemotePath);
                                newValues.put("daynumber", day);
                                newValues.put("time", time);
                                if (isUploading)
                                    syncType = "upload";
                                else syncType = "download";
                                newValues.put("type", syncType);
                                db.insert("profiles", null, newValues);
                                isChanging = false;
                                finish();
                            } else {  //при изменении времени и дня могут возникнуть проблемы с запланированной задачей
                                ContentValues newValues = new ContentValues();
                                newValues.put("name", nameedit.getText().toString());
                                newValues.put("address", addressedit.getText().toString());
                                newValues.put("user", useredit.getText().toString());
                                newValues.put("password", passwordedit.getText().toString());
                                newValues.put("localpath", LocalPath);
                                newValues.put("remotepath", RemotePath);
                                newValues.put("daynumber", day);
                                newValues.put("time", time);
                                if (isUploading)
                                    syncType = "upload";
                                else syncType = "download";
                                newValues.put("type", syncType);
                                db.update("profiles", newValues, "_id = " + currentId, null);
                                MainActivity.currentName = nameedit.getText().toString();

                                isChanging = false;
                                finish();
                            }
                        } else if (!isChanging) {
                            Toast nameExists = Toast.makeText(getApplicationContext(), getString(R.string.nameExists), Toast.LENGTH_LONG);
                            nameExists.show();
                        } else if (!MainActivity.currentName.equals(nameedit.getText().toString())) {
                            Toast nameExists = Toast.makeText(getApplicationContext(), getString(R.string.nameExists), Toast.LENGTH_LONG);
                            nameExists.show();
                        }
                    }
                }
                break;
        }

        return true;
    }

    public void chooselocalpath(View view) {
        Intent i = new Intent(this, PathsSelection.class);
        startActivityForResult(i, 1);
    }

    public void chooseremotepath(View view) {
        Intent i = new Intent(this, FTPPathsSelection.class);
        startActivityForResult(i, 2);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (data == null) {
                return;
            }
            LocalPath = data.getStringExtra("path");
            localpathtext.setText(new File(LocalPath).getName());
        }
        if (requestCode == 2) {
            if (data == null) {
                return;
            }
            RemotePath = data.getStringExtra("remotepathtext");
            isFTPFile = data.getBooleanExtra("isFile", isFTPFile);
            remotepathtext.setText((CharSequence) new File(RemotePath).getName());
        }
    }

    public void changeDirection(View view) {
        final ImageView arrow = (ImageView)findViewById(R.id.arrow);
        if (isUploading) {
            Animation rotateToRight = AnimationUtils.loadAnimation(this, R.anim.rotatetoright);
            arrow.startAnimation(rotateToRight);
            Animation.AnimationListener rotateLsiten = new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    arrow.setImageResource(R.drawable.ic_lefttarrow);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            };
            rotateLsiten.onAnimationEnd(rotateToRight);
            isUploading = false;
        } else {
            Animation rotateToLeft = AnimationUtils.loadAnimation(this, R.anim.rotatetoleft);
            arrow.startAnimation(rotateToLeft);
            Animation.AnimationListener rotateListen = new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    arrow.setImageResource(R.drawable.ic_rightarrow);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            };
            rotateListen.onAnimationEnd(rotateToLeft);
            isUploading = true;
        }
    }

    public int timeToMinutes(int hours, int minutes) {
        int result = hours*60 + minutes;
        return result;
    }


}
