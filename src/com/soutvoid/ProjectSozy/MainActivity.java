package com.soutvoid.ProjectSozy;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class MainActivity extends Activity {

    static TextView TestAsync;

    public static void UploadToServer(final String address, final String user, final String passwd, final String dironserver, final String nameonserver, final String localpathtofile) throws IOException {
        class Task extends AsyncTask<Void, Void, Void> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                TestAsync.setText("Begin");
            }

            @Override
            protected Void doInBackground(Void... params) {
                FTPClient ftpClient = new FTPClient();
                try {
                    ftpClient.connect(address);
                    ftpClient.login(user, passwd);
                    ftpClient.changeWorkingDirectory(dironserver);
                    BufferedInputStream buffout = null;                                 //Создаем буфер ввода для файла
                    buffout = new BufferedInputStream(new FileInputStream(localpathtofile));
                    ftpClient.storeFile(nameonserver, buffout);
                    buffout.close();
                    ftpClient.logout();
                    ftpClient.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPreExecute();
                TestAsync.setText("End");
            }
        }
        Task MyTask = new Task();
        MyTask.execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {                                   //Вызывается при создании активности
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        final Button UploadButton = (Button)findViewById(R.id.uploadbutton);                //кнопка выгрузки
        TestAsync = (TextView)findViewById(R.id.test_async);
        final Editable FileInput = ((EditText)findViewById(R.id.file_input)).getText();

        UploadButton.setOnClickListener(new View.OnClickListener() {                    //Создаем обработчик нажатия для кнопки выгрузки
            @Override
            public void onClick(View v) {
                try {
                    UploadToServer(Settings.FTP.getAddress(), Settings.FTP.getUser(), Settings.FTP.getPassword(), "/public", FileInput.toString(), R.string.pathtohome + FileInput.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void settingsOnClick(View view) {
        Intent Settings = new Intent(MainActivity.this, Settings.class);
        startActivity(Settings);
    }

}
