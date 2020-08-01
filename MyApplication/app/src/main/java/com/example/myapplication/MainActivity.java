package com.example.myapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public String filename;
    public ImageView camera;
    public Button btn;
    public TextView tv;
    public Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);


//        Fragment selected = new MainFragment();
//        getSupportFragmentManager().beginTransaction().replace(R.id.container,
//                selected).commit();

        camera = findViewById(R.id.imageview);
        btn = findViewById(R.id.btn);
        tv =  findViewById(R.id.tv);
    }

    @Override
    protected void onResume() {
        super.onResume();

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        send_data_to_server();
                    }
                }).start();

            }
        });
    }

    public void takePicture(View view){
        Intent imageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(imageIntent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(imageIntent,0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bundle extras = data.getExtras();
        imageBitmap = (Bitmap) extras.get("data");
        imageBitmap = Bitmap.createScaledBitmap(imageBitmap, 128, 128, false);
    }

    public static void verifyStoragePermissions(Activity activity) {
        //Permission for getting a file
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    public void send_data_to_server(){
        String user = "user";
        String pass = "12345";

        filename = "AUG_0_11.jpeg";

        FTPClient ftpClient = new FTPClient();
        try {

            ftpClient.connect("192.168.1.104");
            ftpClient.login(user, pass);
            ftpClient.enterLocalPassiveMode();

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
            byte[] bitmapdata = bos.toByteArray();
            ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);

            boolean done = ftpClient.storeFile(filename, bs);
            if (done) {
                System.out.println("The first file is uploaded successfully.");
            }

        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();

        } finally {

            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

}