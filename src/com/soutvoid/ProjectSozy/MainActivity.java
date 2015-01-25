package com.soutvoid.ProjectSozy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class MainActivity extends Activity {

    public static void UploadToServer(final String address, final String user, final String passwd, final String dironserver, final String nameonserver, final String localpathtofile) throws IOException {
        Thread Upload = new Thread(new Runnable() {                                     //Создаем новый поток для выгрузки
            @Override
            public void run() {
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
            }
        });
        Upload.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {                                   //Вызывается при создании активности
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        final Button UploadButton = (Button)findViewById(R.id.uploadbutton);            //кнопка выгрузки

        UploadButton.setOnClickListener(new View.OnClickListener() {                    //Создаем обработчик нажатия для кнопки выгрузки
            @Override
            public void onClick(View v) {
                try {
                    UploadToServer(Settings.FTP.getAddress(), Settings.FTP.getUser(), Settings.FTP.getPassword(), "/public", "tryic.txt", "/storage/emulated/0/tryic.txt");
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
