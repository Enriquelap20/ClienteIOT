package com.example.clienteiot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    public Timer timer;
    public static String CLASS_TAG=MainActivity.class.getSimpleName();
    public Intent login;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            this.getSupportActionBar().hide();
        }catch (NullPointerException e){}

        setContentView(R.layout.activity_main);

        timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.e(CLASS_TAG, "Timer de carga terminado");
                goToLoginScreen();
            }
        },2000);

    }

    private void goToLoginScreen() {
        login=new Intent(MainActivity.this, LoginActivity.class);
        startActivity(login);
        MainActivity.this.finish();
    }
}
