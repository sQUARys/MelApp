package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


public class MainFragment extends Fragment {

    public Button btn;
    public TextView tv;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_window, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btn = view.findViewById(R.id.btn);
        tv = view.findViewById(R.id.tv);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                secondTry();
            }
        });
    }

    public void secondTry(){
        String user = "user";
        String pass = "12345";
//
//        filename = "AUG_0_307.jpeg";
//        path_to_file = "/home/roma/Desktop/CNN/AUG_0_307.jpeg";

        FTPClient ftpClient = new FTPClient();
        try {

            ftpClient.connect("127.0.0.1", 21);
            ftpClient.login(user, pass);
            ftpClient.enterLocalPassiveMode();

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

//            File firstLocalFile = new File(path_to_file);

//            InputStream inputStream = new FileInputStream(firstLocalFile);

            tv.setText("GGGLGLGL");
//            boolean done = ftpClient.storeFile(filename, inputStream);
//            inputStream.close();
//            if (done) {
//                System.out.println("The first file is uploaded successfully.");
//            }
//            inputStream.close();

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
//    public void goforIt(){
//
//        FTPClient con;
//        filename = "AUG_0_307.jpeg";
//        path_to_file = "/home/roma/Desktop/CNN/AUG_0_307.jpeg";
//
//        try
//        {
//            con = new FTPClient();
//            con.connect("127.0.0.1");
//
//            if (con.login("user", "12345"))
//            {
//                con.enterLocalPassiveMode(); // important!
//                con.setFileType(FTP.BINARY_FILE_TYPE);
//
//                FileInputStream in = new FileInputStream(new File("/home/roma/Desktop"));
//                boolean result = con.storeFile(filename, in);
//                in.close();
//                if (result) tv.setText("DOne");
//                con.logout();
//                con.disconnect();
//            }
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//    }
}
