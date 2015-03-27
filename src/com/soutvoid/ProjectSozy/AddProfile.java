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
    public static Editable addressedit;
    public static Editable useredit;
    public static Editable passwordedit;
    public static Editable nameedit;
    boolean isUploading = true;
    boolean isFTPFile;
    String syncType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addprofile);
        getActionBar().setIcon(R.drawable.ic_action_back);
        getActionBar().setHomeButtonEnabled(true);

        isFTPFile = false;
        localpathtext = (TextView)findViewById(R.id.localpath);
        remotepathtext = (TextView)findViewById(R.id.remotepath);
        addressedit = ((EditText)findViewById(R.id.addredit)).getText();
        useredit = ((EditText)findViewById(R.id.useredit)).getText();
        passwordedit = ((EditText)findViewById(R.id.passwdedit)).getText();
        nameedit = ((EditText)findViewById(R.id.nameedit)).getText();

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
                    SQLiteOpen dbOpen = new SQLiteOpen(this);
                    SQLiteDatabase db;
                    try {
                        db = dbOpen.getWritableDatabase();
                    } catch (SQLiteException e) {
                        e.printStackTrace();
                        db = dbOpen.getReadableDatabase();
                    }

                    if (nameedit.toString().trim().equals("") || addressedit.toString().trim().equals("") || useredit.toString().trim().equals("") || passwordedit.toString().trim().equals("") ||
                            LocalPath.equals("") || RemotePath.equals("")) {
                        Toast emptyField = Toast.makeText(getApplicationContext(), getString(R.string.emptyField), Toast.LENGTH_LONG);
                        emptyField.show();
                    } else {

                        if (db.query("profiles", new String[]{"name"}, "name = '" + nameedit.toString() + "'", null, null, null, null).getCount() == 0) {

                            LocalPath = LocalPath.substring(1);
                            LocalPath = LocalPath.substring(LocalPath.indexOf("/"));

                            ContentValues newValues = new ContentValues();
                            newValues.put("name", nameedit.toString());
                            newValues.put("address", addressedit.toString());
                            newValues.put("user", useredit.toString());
                            newValues.put("password", passwordedit.toString());
                            newValues.put("localpath", "/storage/emulated/0" + LocalPath);
                            newValues.put("remotepath", RemotePath);
                            if (isUploading)
                                syncType = "upload";
                            else syncType = "download";
                            newValues.put("type", syncType);
                            db.insert("profiles", null, newValues);
                            finish();
                        } else {
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
