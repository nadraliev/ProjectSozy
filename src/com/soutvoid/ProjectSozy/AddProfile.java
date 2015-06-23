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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    boolean isFile = true;
    boolean isUploading = true;
    public static boolean isChanging = false;    //если true, то активность вызвана, чтобы изменить профиль
    String syncType;

    Integer currentId;

    SQLiteOpenProfiles dbOpen;
    SQLiteDatabase db;

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

        localpathtext = (TextView)findViewById(R.id.localpath);
        remotepathtext = (TextView)findViewById(R.id.remotepath);
        addressedit = ((EditText)findViewById(R.id.addredit));
        useredit = ((EditText)findViewById(R.id.useredit));
        passwordedit = ((EditText)findViewById(R.id.passwdedit));
        nameedit = ((EditText)findViewById(R.id.nameedit));



        if(isChanging) {     //заполнить поля текущими данными, если профиль изменяется
            Cursor data = db.query("profiles", new String[] {"_id", "name", "address", "user", "password", "path", "destination", "type", "daynumber", "time"}, "name = '" + MainActivity.currentName + "'", null, null, null, null);
            data.moveToFirst();
            currentId = data.getInt(0);
            localpathtext.setText(data.getString(5).substring(data.getString(5).lastIndexOf("/") + 1));    //TODO здесь надо исправить
            remotepathtext.setText(data.getString(6).substring(data.getString(6).lastIndexOf("/") + 1));
            addressedit.setText(data.getString(2));
            useredit.setText(data.getString(3));
            passwordedit.setText(data.getString(4));
            nameedit.setText(data.getString(1));

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
                    if (nameedit.getText().toString().trim().equals("") || addressedit.getText().toString().trim().equals("") || useredit.getText().toString().trim().equals("") || passwordedit.getText().toString().trim().equals("") ||
                            LocalPath.equals("") || RemotePath.equals("")) {
                        Toast emptyField = Toast.makeText(getApplicationContext(), getString(R.string.emptyField), Toast.LENGTH_LONG);
                        emptyField.show();
                    } else {
                        if (db.query("profiles", new String[]{"name"}, "name = '" + nameedit.getText().toString() + "'", null, null, null, null).getCount() == 0 || (isChanging && MainActivity.currentName.equals(nameedit.getText().toString()))) {
                            if(!isChanging) {

                                ContentValues newValues = new ContentValues();
                                newValues.put("name", nameedit.getText().toString());
                                newValues.put("address", addressedit.getText().toString());
                                newValues.put("user", useredit.getText().toString());
                                newValues.put("password", passwordedit.getText().toString());
                                if (isUploading) {
                                    syncType = "upload";
                                    if (new File(LocalPath).isDirectory())
                                    newValues.put("path", LocalPath);
                                    else newValues.put("path", LocalPath.substring(0, LocalPath.lastIndexOf("/")));
                                    if (!isFile)
                                    newValues.put("destination", RemotePath);
                                    else newValues.put("destination", RemotePath.substring(0, RemotePath.lastIndexOf("/")));
                                } else {
                                    syncType = "download";
                                    if (!isFile)
                                    newValues.put("path", RemotePath);
                                    else newValues.put("path", RemotePath.substring(0, RemotePath.lastIndexOf("/")));
                                    if (new File(LocalPath).isDirectory())
                                    newValues.put("destination", LocalPath);
                                    else newValues.put("destination", LocalPath.substring(0, LocalPath.lastIndexOf("/")));
                                }
                                newValues.put("type", syncType);
                                db.insert("profiles", null, newValues);
                                Cursor cursor = db.query("profiles", new String[]{"_id"}, "name = '" + nameedit.getText().toString() + "'", null, null, null, null);
                                cursor.moveToFirst();
                                int id = cursor.getInt(0);
                                cursor.close();
                                Intent i = new Intent(AddProfile.this, SyncService.class);
                                i.putExtra("id", id).putExtra("isUpload", isUploading).putExtra("isFile", isFile).putExtra("reason", "createProfile").putExtra("local", LocalPath).putExtra("remote", RemotePath);
                                startService(i);
                                isChanging = false;
                                finish();
                            } else {
                                ContentValues newValues = new ContentValues();
                                newValues.put("name", nameedit.getText().toString());
                                newValues.put("address", addressedit.getText().toString());
                                newValues.put("user", useredit.getText().toString());
                                newValues.put("password", passwordedit.getText().toString());
                                if (isUploading) {
                                    syncType = "upload";
                                    if (new File(LocalPath).isDirectory())
                                        newValues.put("path", LocalPath);
                                    else newValues.put("path", LocalPath.substring(0, LocalPath.lastIndexOf("/")));
                                    if (!isFile)
                                        newValues.put("destination", RemotePath);
                                    else newValues.put("destination", RemotePath.substring(0, RemotePath.lastIndexOf("/")));
                                } else {
                                    syncType = "download";
                                    if (!isFile)
                                        newValues.put("path", RemotePath);
                                    else newValues.put("path", RemotePath.substring(0, RemotePath.lastIndexOf("/")));
                                    if (new File(LocalPath).isDirectory())
                                        newValues.put("destination", LocalPath);
                                    else newValues.put("destination", LocalPath.substring(0, LocalPath.lastIndexOf("/")));
                                }
                                newValues.put("type", syncType);
                                db.update("profiles", newValues, "_id = " + currentId, null);
                                Cursor cursor = db.query("profiles", new String[]{"_id"}, "name = '" + nameedit.getText().toString() + "'", null, null, null, null);
                                cursor.moveToFirst();
                                int id = cursor.getInt(0);
                                cursor.close();
                                dbOpen.dropTable(db, "profile" + currentId);
                                Intent i = new Intent(AddProfile.this, SyncService.class);
                                i.putExtra("id", currentId).putExtra("isUpload", isUploading).putExtra("isFile", isFile).putExtra("reason", "createProfile").putExtra("local", LocalPath).putExtra("remote", RemotePath);
                                startService(i);
                                cursor.close();
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
            localpathtext.setText(LocalPath.substring(LocalPath.lastIndexOf("/") + 1));    //на этом этапе у нас есть путь к главной папке и список всех файлов

        }
        if (requestCode == 2) {
            if (data == null) {
                return;
            }
            RemotePath = data.getStringExtra("path");
            isFile = data.getBooleanExtra("isFile", isFile);
            if (RemotePath.equals("/"))
                remotepathtext.setText("/");
            else
                remotepathtext.setText( new File(RemotePath).getName());
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
