package com.soutvoid.ProjectSozy;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

    SQLiteOpen dbOpen;
    SQLiteDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addprofile);
        getActionBar().setIcon(R.drawable.ic_action_back);
        getActionBar().setHomeButtonEnabled(true);

        currentId = 0;

        if(isChanging) getActionBar().setTitle(R.string.edit);

        dbOpen = new SQLiteOpen(this);
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

        if(isChanging) {
            Cursor data = db.query("profiles", new String[] {"_id", "name", "address", "user", "password", "localpath", "remotepath", "type"}, "name = '" + MainActivity.currentName + "'", null, null, null, null);
            data.moveToFirst();
            currentId = data.getInt(0);
            localpathtext.setText(data.getString(5).substring(data.getString(5).lastIndexOf("/") + 1));
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
                                if (isUploading)
                                    syncType = "upload";
                                else syncType = "download";
                                newValues.put("type", syncType);
                                db.insert("profiles", null, newValues);
                                isChanging = false;
                                finish();
                            } else {
                                ContentValues newValues = new ContentValues();
                                newValues.put("name", nameedit.getText().toString());
                                newValues.put("address", addressedit.getText().toString());
                                newValues.put("user", useredit.getText().toString());
                                newValues.put("password", passwordedit.getText().toString());
                                newValues.put("localpath", LocalPath);
                                newValues.put("remotepath", RemotePath);
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


}
