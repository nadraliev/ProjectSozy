package com.soutvoid.ProjectSozy;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;

/**
 * Created by andrew on 23.06.15.
 */
public class FilesListAdapter extends BaseAdapter {

    Context context;
    SQLiteDatabase db;
    SQLiteOpenProfiles dbOpen;
    ArrayList<String> files;
    int idProfile;
    LayoutInflater lInflater;

    FilesListAdapter (Context context, int idProfile) {
        this.context = context;
        try {
            dbOpen = new SQLiteOpenProfiles(context);
            db = dbOpen.getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
            db = dbOpen.getReadableDatabase();
        }
        files = new ArrayList<String>();
        this.idProfile = idProfile;
        Cursor cursor = db.query("profile" + idProfile, new String[] {"path"}, null, null, null, null, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            files.add(cursor.getString(0));
            if (!cursor.isLast()) cursor.moveToNext();
        }
        cursor.close();
        db.close();
        lInflater = (LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return files.size();
    }

    @Override
    public Object getItem(int position) {
        return files.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // используем созданные, но не используемые view
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.listfilesitem, parent, false);
        }

        // заполняем View в пункте списка данными из товаров: наименование, цена
        // и картинка
        ((TextView) view.findViewById(R.id.filespath)).setText(files.get(position));

        Button button = (Button) view.findViewById(R.id.filesclear);
        // присваиваем чекбоксу обработчик
        button.setOnClickListener(onClickListener);
        // пишем позицию
        button.setTag(position);
        return view;
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Integer id = (Integer)v.getTag();
            try {
                dbOpen = new SQLiteOpenProfiles(context);
                db = dbOpen.getWritableDatabase();
            } catch (Exception e) {
                e.printStackTrace();
                db = dbOpen.getReadableDatabase();
            }
            String file = files.get((Integer) v.getTag());
            db.delete("profile" + idProfile, "path = '" + file + "'", null);
            files.remove(id.intValue());
            notifyDataSetChanged();
        }
    };
}
