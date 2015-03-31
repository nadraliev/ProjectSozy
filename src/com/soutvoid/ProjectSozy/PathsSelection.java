package com.soutvoid.ProjectSozy;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import android.content.DialogInterface;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by andrew on 12.03.15.
 */
public class PathsSelection extends ListActivity {

    private List<String> directoryEntries = new ArrayList<String>();
    private List<String> directoryEntriesShow = new ArrayList<String>();
    private File currentDirectory = new File("/");
    private File currentFile = new File("");


    //заполняем лист содержимым папки
    private void fill(File[] files) {
        directoryEntries.clear();
        directoryEntriesShow.clear();

        //добавляем .. в начало списка, если можно подняться на уровень вверх
        if (currentDirectory.getParent() != null && !currentDirectory.getParent().equals("/")) {
            directoryEntries.add("..");
            directoryEntriesShow.add("..");
        }
        for (File file : files) {
            if (file.isDirectory()) {
                directoryEntriesShow.add("/" + file.getName());
            } else directoryEntriesShow.add(file.getName());
            directoryEntries.add(file.getAbsolutePath());
        }

        ArrayAdapter<String> directoryList = new ArrayAdapter<String>(this, R.layout.row, this.directoryEntriesShow);
        this.setListAdapter(directoryList);
    }

    private void upOneLevel() {
        if (currentDirectory.getParent() != null) {
            browseTo(currentDirectory.getParentFile());
        }
    }

    private void browseTo(final File aDirectory) {
        if (aDirectory.isDirectory()) {
            currentDirectory = aDirectory;
            fill(aDirectory.listFiles());
            TextView AbsolutePathTitle = (TextView)findViewById(R.id.absolutepathtitle);
            AbsolutePathTitle.setText(aDirectory.getAbsolutePath());
        } else {
            DialogInterface.OnClickListener okButtonListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //дествия по нажатию да
                    currentFile = aDirectory;
                    Intent i = new Intent();
                    if (currentFile.getName() == "")
                        i.putExtra("path", currentDirectory.getAbsolutePath());
                    else i.putExtra("path", currentFile.getAbsolutePath());
                    setResult(RESULT_OK, i);
                    finish();

                }
            };

            DialogInterface.OnClickListener noButtonListener = new DialogInterface.OnClickListener() {
                @Override
            public void onClick(DialogInterface dialog, int which) {
                    //действия по нажатию нет
                }
            };

            new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.alertDialogStyle))
                    .setTitle(getString(R.string.confirmation))
                    .setMessage(getString(R.string.wantopen) + " " + aDirectory.getName() + "?")
                    .setPositiveButton(getString(R.string.yes), okButtonListener)
                    .setNegativeButton(getString(R.string.no), noButtonListener)
                    .show();
        }

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        int selectedRowID = position;
        String selectedFileString = directoryEntries.get(selectedRowID);

        if (selectedFileString.equals("..")) {
            upOneLevel();
        } else {
            File clickedFile = null;
            clickedFile = new File(selectedFileString);
            if (clickedFile != null) {
                browseTo(clickedFile);
            }
        }
    }



    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.pathsselection);
        getActionBar().setIcon(R.drawable.ic_action_back);
        getActionBar().setHomeButtonEnabled(true);

        browseTo(new File("/sdcard"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_profile_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.ok_action:
                Intent i = new Intent();
                if (currentFile.getName() == "")
                    i.putExtra("path", currentDirectory.getAbsolutePath());
                else i.putExtra("path", currentFile.getAbsolutePath());
                setResult(RESULT_OK, i);
                finish();
                break;
        }
        return true;
    }
}
