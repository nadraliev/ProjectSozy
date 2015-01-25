package com.soutvoid.ProjectSozy;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import org.apache.commons.net.ftp.FTP;

import java.io.*;

/**
 * Created by andrew on 26.12.14.
 */

public class Settings extends Activity {

    final String PathToHome = "/storage/emulated/0/";
    protected static LoginData FTP = new LoginData();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        final Editable AddressEdit = ((EditText)findViewById(R.id.addredit)).getText(); //поле ввода адреса
        final Editable UserEdit = ((EditText)findViewById(R.id.useredit)).getText();    //поле ввода логина
        final Editable PasswdEdit = ((EditText)findViewById(R.id.passwdedit)).getText();//поле ввода пароля
        final Button SaveButton = (Button)findViewById(R.id.savebutton);                //кнопка сохранения в файл

        SaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FTP.setUser(UserEdit.toString());
                FTP.setAddress(AddressEdit.toString());
                FTP.setPassword(PasswdEdit.toString());
            }
        });
    }
}
