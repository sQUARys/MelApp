package com.example.myapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
    public String accuracy_server_file = "accuracy.txt";
    public boolean status;
    ByteArrayOutputStream array_for_accuracy;
    ByteArrayInputStream array_for_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);
        camera = findViewById(R.id.imageview);
        btn = findViewById(R.id.btn);
        tv =  findViewById(R.id.tv);
        array_for_accuracy = new ByteArrayOutputStream();
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
        camera.setImageBitmap(imageBitmap);
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

        filename = "picture.jpeg";

        FTPClient ftpClient = new FTPClient();
        try {

            ftpClient.connect("192.168.1.104");
            ftpClient.login(user, pass);
            ftpClient.enterLocalPassiveMode();

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            if(imageBitmap == null){
                System.out.println("Сначала сфотографируйте потом отправляйте");
                return;
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);

            byte[] bytes_of_image = bos.toByteArray();
            array_for_image = new ByteArrayInputStream(bytes_of_image);

            boolean done = ftpClient.storeFile(filename, array_for_image);

            if (done) {

                System.out.println("The first file is uploaded successfully.");;

                status = ftpClient.retrieveFile("/" + accuracy_server_file, array_for_accuracy);

                if(status){

                    System.out.println("The first file is downloaded successfully.");

                    System.out.println(array_for_accuracy.toString());
                    tv.setText("HERE WE WILL CHECK THE ACCURACY: " + array_for_accuracy.toString());
                    array_for_accuracy.reset();
                }
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