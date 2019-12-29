package com.example.clienteiot;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HomeScreen extends AppCompatActivity {
    public static String CLASS_TAG=HomeScreen.class.getSimpleName();
    public TextView title;
    public Bundle extras;
    String username;
    String message;
    public String ipAddress;
    public int port;
    public EditText clientMsg;
    public static TextView serverMsg;
    public Button tcpButton;
    public Button udpButton;
    public Button clearButton;
    public FloatingActionButton fab;
    public FloatingActionButton settingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        //Identificación de Views
        //Botones
        tcpButton = findViewById(R.id.tcpButton);
        udpButton = findViewById(R.id.udpButton);
        clearButton = findViewById(R.id.clearButton);
        fab = findViewById(R.id.fab);
        settingsButton=findViewById(R.id.settingsButton);
        //TextViews y EditText
        title = findViewById(R.id.title);
        serverMsg = findViewById(R.id.serverMsg);
        clientMsg = findViewById(R.id.clientMsg);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Cliente TCP/UDP");

        final PreferencesManager sharedPreferences=new PreferencesManager(getApplicationContext());

        ipAddress=sharedPreferences.getIpAddress();
        port=sharedPreferences.getPort();

        tcpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message = clientMsg.getText().toString();
                new SendMessage(1,ipAddress,port).execute(message);
                Log.e(CLASS_TAG, ipAddress);
                Log.e(CLASS_TAG, String.valueOf(port));
            }
        });

        udpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message = clientMsg.getText().toString();
                new SendMessage(2,ipAddress,port).execute(message);
                Log.e(CLASS_TAG, ipAddress);
                Log.e(CLASS_TAG, String.valueOf(port));
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPreferences.setRememberLogin(false);
                HomeScreen.this.finish();
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clientMsg.getText().clear();
                serverMsg.setText("");
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settings=new Intent(HomeScreen.this, SettingsActivity.class);
                startActivity(settings);
                HomeScreen.this.finish();
                Log.e(CLASS_TAG, "HomeScreen terminada");
            }
        });

        extras = getIntent().getExtras();
        if (extras != null) {
            username = extras.getString("name");
                if (!username.isEmpty()) {
                    title.setText(getResources().getString(R.string.welcome_msg) + username + "!");
                    }
            }

    }

    class SendMessage extends AsyncTask<String, Void, String> {
        private int identifier;
        private String ipAddress;
        private int port;
        public String modifiedSentence="";
        byte[] sendData;
        byte[] receiveData = new byte[1024];
        boolean err_timedOut=false;
        boolean err_unknownHost=false;
        boolean err_unreachableHost=false;

        public SendMessage(int identifier, String ipAddress, int port) {
            this.identifier=identifier;
            this.ipAddress=ipAddress;
            this.port=port;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.e(CLASS_TAG, "Hilo iniciado");
        }

        @Override
        protected String doInBackground(String... strings) {
           if(identifier==1) {
                try {
                   String sentence = strings[0];
                   Log.e(CLASS_TAG, sentence);
                   String sentenceHash = md5(sentence);
                   Socket clientSocket = new Socket(ipAddress, port);
                   Log.e(CLASS_TAG, "socket inicializado");
                   DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                   BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                   outToServer.writeBytes(sentenceHash + "\n");
                   Log.e(CLASS_TAG, "Mensaje Enviado");

                   modifiedSentence = inFromServer.readLine();
                   Log.e(CLASS_TAG, "Mensaje Recibido");
                   Log.e(CLASS_TAG, modifiedSentence);

                   clientSocket.close();

               }catch(NoRouteToHostException e){
                    e.printStackTrace();
                    err_unreachableHost=true;
                }
                catch(UnknownHostException e){
                    e.printStackTrace();
                    err_unknownHost=true;
               }catch (java.net.ConnectException e) {
                   e.printStackTrace();
                   err_timedOut=true;
               }catch (IOException e) {
                   e.printStackTrace();
               }
           }
           else if(identifier==2){
                try {
                   String sentence = strings[0];
                   sendData = sentence.getBytes();
                   Log.e(CLASS_TAG, sentence);

                   DatagramSocket clientSocket = new DatagramSocket();
                   Log.e(CLASS_TAG, "socket inicializado");

                   DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(ipAddress), port);
                   clientSocket.send(sendPacket);
                   Log.e(CLASS_TAG, "Mensaje Enviado");

                   DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                   clientSocket.receive(receivePacket);
                   Log.e(CLASS_TAG, "Mensaje Recibido");

                   modifiedSentence = new String(receivePacket.getData(), 0, receivePacket.getLength());
                   Log.e(CLASS_TAG, modifiedSentence);

                   clientSocket.close();
               }catch(NoRouteToHostException e){
                    e.printStackTrace();
                    err_unreachableHost=true;
               }catch(UnknownHostException e){
                    e.printStackTrace();
                    err_unknownHost=true;
               }catch (java.net.ConnectException e){
                    e.printStackTrace();
                    err_timedOut=true;
               }catch (IOException e) {
                    e.printStackTrace();
                }
           }
           return modifiedSentence;
        }

        @Override
        protected void onPostExecute(String fromServerMsg) {
            super.onPostExecute(fromServerMsg);
            if(err_timedOut){
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.err_connectionTimedOut), Toast.LENGTH_LONG).show();
                HomeScreen.serverMsg.setText("");
            }
            else if(err_unreachableHost){
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.err_unreachableHost), Toast.LENGTH_LONG).show();
                HomeScreen.serverMsg.setText("");
            }
            else if(err_unknownHost){
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.err_unknownHost), Toast.LENGTH_LONG).show();
                HomeScreen.serverMsg.setText("");
            }
            else if (fromServerMsg.equals("") || fromServerMsg.isEmpty()) {
                HomeScreen.serverMsg.setText("");
            }
            else {
                HomeScreen.serverMsg.setText(getResources().getString(R.string.received_msg) + fromServerMsg);
            }
        }
    }

    public String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte[] messageDigest = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i=0; i<messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));

            return hexString.toString();
        }catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

}

