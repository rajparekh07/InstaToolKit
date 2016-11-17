package com.teama.instatoolkit;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

public class HomeActivity extends AppCompatActivity {
    public static final String APPNAME = "InstaToolKit";
    EditText mUsername;
    String mUsernameText,profileUrl;



    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */

    public static int counter=0;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        Button b = (Button) findViewById(R.id.getProfilePicture);
        b.setOnClickListener(new View.OnClickListener() {
                                 @Override
                                 public void onClick(View v) {
                                     final View view = v;
                                     final ProgressDialog spinner = new ProgressDialog(v.getContext());
                                     spinner.setMessage("Finding the profile");
                                     spinner.setCancelable(false);
                                     spinner.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                     spinner.show();
                                     final Thread t = new Thread(
                                             new Runnable() {
                                                 @Override
                                                 public void run() {
                                                     doSomething(view);
                                                     spinner.dismiss();
                                                 }
                                             }
                                     );
                                     t.start();

                                 }
                             }
        );
    }
    protected void doSomething(View v){
        try {
            mUsername = (EditText) findViewById(R.id.username);
            mUsernameText = mUsername.getText().toString();
            mUsernameText = mUsernameText.trim();
            profileUrl = "https://www.instagram.com/" + mUsernameText;

            Intent i = new Intent(getApplicationContext(), ImageActivity.class);
            String url = null;//
            ProcessHTML processHTML = new ProcessHTML();
            processHTML.execute(profileUrl);

            while(!ProcessHTML.status){}
            url = ProcessHTML.url;
            if(url.length() == 0) {Toast.makeText(v.getContext(),"Account not found",Toast.LENGTH_LONG).show();return;}
            //url = "https://instagram.fbom1-1.fna.fbcdn.net/t51.2885-19/14705125_211752892581694_7601561075573587968_a.jpg";
            i.putExtra("EXTRA_TEXT"+counter,url);
            i.putExtra("EXTRA_PROFILENAME"+counter++,mUsernameText);

            Log.d("first",url);


            startActivity(i);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
class ProcessHTML extends AsyncTask<String, Void, Void>{
    String profileUrlString,picUrl;
    URL profileUrl;
    public static boolean status = false;
    HttpURLConnection profileUrlConnection;
    public static String url;
    ProcessHTML(){
        status = false;
    }
    @Override
    protected Void doInBackground(String... params) {
        profileUrlString = params[0];
        try {
            profileUrl = new URL(profileUrlString);
        }catch (MalformedURLException mue) {
            Log.d(HomeActivity.APPNAME,profileUrlString);
            return null;
        }
        try {
            profileUrlConnection =(HttpURLConnection) profileUrl.openConnection();
            profileUrlConnection.setReadTimeout(10000 /* milliseconds */);
            profileUrlConnection.setConnectTimeout(15000 /* milliseconds */);
            profileUrlConnection.setRequestMethod("GET");
            profileUrlConnection.setDoInput(true);
            // Starts the query
            profileUrlConnection.connect();
            int response = profileUrlConnection.getResponseCode();
            Log.d("test", "The response is: " + response);
            InputStream is = profileUrlConnection.getInputStream();
            InputStreamReader isr= new InputStreamReader(is);
            BufferedReader bfr = new BufferedReader(isr);
            String temp,mainUrlContent="";
            while((temp = bfr.readLine())!= null) {
                mainUrlContent += temp;
            }
            bfr.close();
            try {
                String[] abc = mainUrlContent.split("\"");
                for (int i = 0; i < abc.length; i++) {
                    String imageprop="og:image";
                    if (abc[i].equals(imageprop)) {
                        Log.d("in find", "property");
                        picUrl = (abc[i + 2]);
                        break;
                    }
                }
            }catch(Exception e){}
        }catch (IOException ioe){}
            try{url = picUrl.replace("/s150x150","");}catch (Exception e){url = "";}
            status = true;
        return null;
    }
}
