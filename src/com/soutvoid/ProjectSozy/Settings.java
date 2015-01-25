package com.soutvoid.ProjectSozy;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.*;

/**
 * Created by andrew on 26.12.14.
 */

public class Settings extends Activity {

    final String PathToHome = "/storage/emulated/0/";

    static void writeToFile(String path, String string) {       //перезаписывает файл
        File file = new File(path);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            BufferedWriter input = new BufferedWriter(new FileWriter(file));
            input.append("try" + "\n");
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        final Editable AddressEdit = ((EditText)findViewById(R.id.addredit)).getText(); //поле ввода адреса
        final Editable UserEdit = ((EditText)findViewById(R.id.useredit)).getText();    //поле ввода логина
        final Editable PasswdEdit = ((EditText)findViewById(R.id.passwdedit)).getText();//поле ввода пароля
        final Button UploadButton = (Button)findViewById(R.id.uploadbutton);            //кнопка выгрузки
        final Button SaveButton = (Button)findViewById(R.id.savebutton);                //кнопка сохранения в файл

        SaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeToFile(PathToHome + "settings.txt", AddressEdit.toString());
                writeToFile(PathToHome + "settings.txt", UserEdit.toString());
                writeToFile(PathToHome + "settings.txt", PasswdEdit.toString());
            }
        });

        UploadButton.setOnClickListener(new View.OnClickListener() {                    //Создаем обработчик нажатия для кнопки выгрузки
            @Override
            public void onClick(View v) {
                String addr = AddressEdit.toString();
                String user = UserEdit.toString();
                String passwd = PasswdEdit.toString();
                try {
                    MainActivity.UploadToServer(addr, user, passwd, "/public", "tryic.txt", "/storage/emulated/0/tryic.txt");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


    }
}
