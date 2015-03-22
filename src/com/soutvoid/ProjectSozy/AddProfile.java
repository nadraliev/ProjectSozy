package com.soutvoid.ProjectSozy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
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
    Editable addressedit;
    Editable useredit;
    Editable passwordedit;
    Editable nameedit;
    String address;
    String user;
    String password;
    boolean isUploading = true;
    boolean isFTPFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addprofile);
        getActionBar().hide();

        isFTPFile = false;
        localpathtext = (TextView)findViewById(R.id.localpath);
        remotepathtext = (TextView)findViewById(R.id.remotepath);
        addressedit = ((EditText)findViewById(R.id.addredit)).getText();
        useredit = ((EditText)findViewById(R.id.useredit)).getText();
        passwordedit = ((EditText)findViewById(R.id.passwdedit)).getText();
        nameedit = ((EditText)findViewById(R.id.nameedit)).getText();

    }

    public void actionbarback(View view) {
        onBackPressed();
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
            localpathtext.setText((CharSequence) new File(LocalPath).getName());
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
                    arrow.setBackgroundResource(R.drawable.ic_lefttarrow);
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
                    arrow.setBackgroundResource(R.drawable.ic_rightarrow);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            };
            rotateListen.onAnimationEnd(rotateToLeft);
            isUploading = true;
        }
    }

    public void ok(View view) {
        if (isUploading && isFTPFile) {
            Toast chooseAnother = Toast.makeText(getApplicationContext(), getString(R.string.wrongdestination), Toast.LENGTH_LONG);
            chooseAnother.show();
        } else if (!isUploading && (!new File(LocalPath).isDirectory())) {
            Toast chooseAnother = Toast.makeText(getApplicationContext(), getString(R.string.wrongdestination), Toast.LENGTH_LONG);
            chooseAnother.show();
        } else {
            //TODO здесь сохранять в базу данных
        }
    }


}
