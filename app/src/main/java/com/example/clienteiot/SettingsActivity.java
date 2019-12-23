package com.example.clienteiot;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Pattern;

public class SettingsActivity extends AppCompatActivity {
    public static String CLASS_TAG=SettingsActivity.class.getSimpleName();
    public EditText ipEditText;
    public EditText portEditText;
    public Button savePrefs;
    public String ipAddress;
    double checkPort;
    static int port;
    String previousText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Configuración");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final PreferencesManager sharedPreferences=new PreferencesManager(getApplicationContext());

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homeScreen=new Intent(SettingsActivity.this,HomeScreen.class);
                homeScreen.putExtra("name",sharedPreferences.getUsername());
                startActivity(homeScreen);
                SettingsActivity.this.finish();
                Log.e(CLASS_TAG, "Settings terminada");
            }
        });

        ipEditText=findViewById(R.id.ipEditText);
        portEditText=findViewById(R.id.portEditText);
        savePrefs=findViewById(R.id.savePrefs);

        ipAddress=sharedPreferences.getIpAddress();
        port=sharedPreferences.getPort();

        ipEditText.setText(ipAddress);
        portEditText.setText(String.valueOf(port));

        final Pattern PARTIAL_IP_ADDRESS = Pattern.compile("^((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9])\\.){0,3}"+"((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9])){0,1}$");

        ipEditText.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void beforeTextChanged(CharSequence s,int start,int count,int after) {}
            @Override public void afterTextChanged(final Editable s) {
                if(PARTIAL_IP_ADDRESS.matcher(s).matches()) {
                    previousText = s.toString();
                } else {
                    s.replace(0, s.length(), previousText);
                }
            }
        });
        savePrefs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ipCheck=ipEditText.getText().toString();
                checkPort = Double.parseDouble(portEditText.getText().toString());
                if(ValidateIpAddress(ipCheck)&&ValidatePort(checkPort)) {
                    ipAddress = ipCheck;
                    port= (int) checkPort;
                    sharedPreferences.setIpAddress(ipAddress);
                    sharedPreferences.setPort(port);
                    Log.e(CLASS_TAG, ipAddress);
                    Log.e(CLASS_TAG, String.valueOf(port));
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.preferences_saved), Toast.LENGTH_LONG).show();
                }
                else if(ValidateIpAddress(ipCheck)&&!ValidatePort(checkPort)){
                    ipAddress = ipCheck;
                    sharedPreferences.setIpAddress(ipAddress);
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.bad_port), Toast.LENGTH_LONG).show();
                }
                else if(!ValidateIpAddress(ipCheck)&&ValidatePort(checkPort)){
                    port= (int) checkPort;
                    sharedPreferences.setPort(port);
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.bad_ip), Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.bad_prefs), Toast.LENGTH_LONG).show();
                }
            }
        });



    }

    private boolean ValidatePort(double checkPort){
        boolean validation;
        if (checkPort < 1024 || checkPort > 65535) {
            Log.e(CLASS_TAG, "bad port");
            validation=false;
        } else {
            Log.e(CLASS_TAG, "good port");
            validation=true;
        }
        return validation;
    }

    private boolean ValidateIpAddress(String ipCheck){
        boolean validation;
        final Pattern FINAL_IP_ADDRESS =Pattern.compile("((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"+ "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"+ "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"+ "|[1-9][0-9]|[0-9]))");

        if(FINAL_IP_ADDRESS.matcher(ipCheck).matches()) {
            validation = true;
            Log.e(CLASS_TAG, "good ip");
        }
        else{
            validation=false;
            Log.e(CLASS_TAG, "bad ip");
        }

        return validation;
    }

    @Override
    public void onBackPressed() {
        final PreferencesManager sharedPreferences=new PreferencesManager(getApplicationContext());
        Intent homeScreen=new Intent(SettingsActivity.this,HomeScreen.class);
        homeScreen.putExtra("name",sharedPreferences.getUsername());
        startActivity(homeScreen);
        SettingsActivity.this.finish();
        Log.e(CLASS_TAG, "Settings terminada");
    }

}
