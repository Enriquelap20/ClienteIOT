package com.example.clienteiot;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.text.Normalizer;

public class HomeScreen extends AppCompatActivity implements Orientation.Listener{

    public static String CLASS_TAG=HomeScreen.class.getSimpleName();
    public TextView title;
    public ImageView logo;
    public Bundle extras;
    String username;
    public String ipAddress;
    public int port;
    public EditText clientMsg;
    public static TextView serverMsg;
    public Button tcpButton;
    public Button udpButton;
    public Button clearButton;
    public Button securityCheck;
    public boolean securityEnforced=true;
    public FloatingActionButton fab;
    public FloatingActionButton settingsButton;
    private final int REQUEST_SPEECH_RECOGNIZER = 3000;
    private String command;
    private String question = "¿Qué Desea Hacer?";
    private Orientation mOrientation;
    public boolean driveMode=false;
    public Button driveCheck;
    public String currentState="centro";
    public Button pedalButton;
    public Button reverseButton;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        //Identificación de Views
        //Botones
        securityCheck = findViewById(R.id.securityCheck);
        driveCheck = findViewById(R.id.driveCheck);
        pedalButton = findViewById(R.id.pedalButton);
        reverseButton=findViewById(R.id.reverseButton);

        tcpButton = findViewById(R.id.tcpButton);
        udpButton = findViewById(R.id.udpButton);
        clearButton = findViewById(R.id.clearButton);
        fab = findViewById(R.id.fab);
        settingsButton=findViewById(R.id.settingsButton);
        //TextViews y EditText
        title = findViewById(R.id.title);
        serverMsg = findViewById(R.id.serverMsg);
        clientMsg = findViewById(R.id.clientMsg);
        logo=findViewById(R.id.imageView);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Cliente IOT");

        final PreferencesManager sharedPreferences=new PreferencesManager(getApplicationContext());

        ipAddress=sharedPreferences.getIpAddress();
        port=sharedPreferences.getPort();

        securityCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!securityEnforced) {
                    securityEnforced = true;
                    Toast.makeText(getApplicationContext(), "El envío es Seguro!", Toast.LENGTH_LONG).show();
                    securityCheck.setText("Desactivar Seguridad");
                }
                else{
                    securityEnforced = false;
                    Toast.makeText(getApplicationContext(), "La seguridad Ha sido Desactivada!", Toast.LENGTH_LONG).show();
                    securityCheck.setText("Activar Seguridad");
                }
            }
        });

        pedalButton.setVisibility(View.GONE);
        reverseButton.setVisibility(View.GONE);

        driveCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!driveMode) {
                    driveMode = true;
                    Toast.makeText(getApplicationContext(), "Modo Manejo!", Toast.LENGTH_LONG).show();
                    driveCheck.setText("Desactivar Modo Manejo");
                    pedalButton.setVisibility(View.VISIBLE);
                    reverseButton.setVisibility(View.VISIBLE);
                    logo.setVisibility(View.GONE);
                }
                else{
                    driveMode = false;
                    Toast.makeText(getApplicationContext(), "Modo manejo Ha sido Desactivado!", Toast.LENGTH_LONG).show();
                    driveCheck.setText("Activar Modo Manejo");
                    pedalButton.setVisibility(View.GONE);
                    reverseButton.setVisibility(View.GONE);
                    logo.setVisibility(View.VISIBLE);
                }
            }
        });

        pedalButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // PRESSED
                        if(driveMode){
                            command="adelante";
                            new SendMessage(1,ipAddress,port, "ON").execute(command);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // RELEASED
                        if(driveMode){
                            command="alto";
                            new SendMessage(1,ipAddress,port, "T").execute(command);
                        }
                        break;
                }
                return false;
            }
        });

        reverseButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // PRESSED
                        if(driveMode){
                            command="atras";
                            new SendMessage(1,ipAddress,port, "ON").execute(command);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // RELEASED
                        if(driveMode){
                            command="alto";
                            new SendMessage(1,ipAddress,port, "T").execute(command);
                        }
                        break;
                }
                return false;
            }
        });

        tcpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                command = clientMsg.getText().toString();
                String commandID="T";
                    if(command.toLowerCase().contains("enciende la")){
                        command=command.replace("enciende la ","");
                        commandID="ON";
                    }
                    else if(command.toLowerCase().contains("apaga la")){
                        command=command.replace("apaga la ","");
                        commandID="OFF";
                    }
                    else if(command.toLowerCase().contains("abre la")){
                        command=command.replace("Abre la ","");
                        command=command.replace("abre la ","");
                        commandID="ON";
                    }
                    else if(command.toLowerCase().contains("cierra la")){
                        command=command.replace("Cierra la ","");
                        command=command.replace("cierra la ","");
                        commandID="OFF";
                    }
                    else{
                        commandID="T";
                    }
                new SendMessage(1,ipAddress,port, commandID).execute(command);
                Log.e(CLASS_TAG, ipAddress);
                Log.e(CLASS_TAG, String.valueOf(port));
            }
        });

        udpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSpeechRecognizer();
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

        startSpeechRecognizer();

        mOrientation = new Orientation(this);
    }

    class SendMessage extends AsyncTask<String, Void, String> {
        private int identifier;
        private String ipAddress;
        private int port;
        private String commandID;
        public String modifiedSentence="";
        byte[] sendData;
        byte[] receiveData = new byte[1024];
        boolean err_timedOut=false;
        boolean err_unknownHost=false;
        boolean err_unreachableHost=false;

        public SendMessage(int identifier, String ipAddress, int port, String commandID) {
            this.identifier=identifier;
            this.ipAddress=ipAddress;
            this.port=port;
            this.commandID=commandID;
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
                    String message;
                    String sentence = strings[0];
                    sentence = Normalizer.normalize(sentence, Normalizer.Form.NFD);
                    sentence = sentence.replaceAll("[^\\p{ASCII}]", "");
                    sentence = sentence.trim();
                    sentence = sentence.toUpperCase();
                    Log.e(CLASS_TAG, sentence);
                    String sentenceHash = md5(sentence);
                        if(securityEnforced){
                            message=commandID+sentenceHash;
                            }
                        else {
                            message=commandID+sentence;
                            }
                   Socket clientSocket = new Socket(ipAddress, port);
                   Log.e(CLASS_TAG, "socket inicializado");
                   DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                   BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                   outToServer.writeBytes(message + "\n");
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

    private String md5(String in) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.reset();
            digest.update(in.getBytes());
            byte[] a = digest.digest();
            int len = a.length;
            StringBuilder sb = new StringBuilder(len << 1);
            for (int i = 0; i < len; i++) {
                sb.append(Character.forDigit((a[i] & 0xf0) >> 4, 16));
                sb.append(Character.forDigit(a[i] & 0x0f, 16));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
        return null;
    }

    private void startSpeechRecognizer() {
        Intent intent = new Intent (RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,question );
        startActivityForResult(intent, REQUEST_SPEECH_RECOGNIZER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String commandID;

        if (requestCode == REQUEST_SPEECH_RECOGNIZER) {
            if (resultCode == RESULT_OK) {
                String[] results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).toArray(new String[0]);
                command = results[0];
                serverMsg.setText("Dijiste: " +command);
                    if(command.toLowerCase().contains("enciende la")){
                        command=command.replace("enciende la ","");
                        commandID="ON";
                    }
                    else if(command.toLowerCase().contains("apaga la")){
                        command=command.replace("apaga la ","");
                        commandID="OFF";
                    }
                    else if(command.toLowerCase().contains("abre la")){
                        command=command.replace("Abre la ","");
                        command=command.replace("abre la ","");
                        commandID="ON";
                    }
                    else if(command.toLowerCase().contains("cierra la")){
                        command=command.replace("Cierra la ","");
                        command=command.replace("cierra la ","");
                        commandID="OFF";
                    }
                    else{
                        commandID="T";
                    }
                new SendMessage(1,ipAddress,port, commandID).execute(command);
                startSpeechRecognizer();
            }
        }
    }

    @Override
    public void onOrientationChanged(float roll) {
        float izq=20;
        float der=-20;
        if(driveMode==true) {
            if (roll >= izq && !currentState.equalsIgnoreCase("izquierda")) {
                command="izquierda";
                currentState=command;
                new SendMessage(1, ipAddress, port, "T").execute(command);
                Log.e(CLASS_TAG, command);
                }
            if (roll <=der && !currentState.equalsIgnoreCase("derecha")) {
                command="derecha";
                currentState=command;
                new SendMessage(1, ipAddress, port, "T").execute(command);
                Log.e(CLASS_TAG, command);
                }
            else if(roll < izq && roll > der && !currentState.equalsIgnoreCase("centro")){
                command="centro";
                currentState=command;
                new SendMessage(1, ipAddress, port, "T").execute(command);
                Log.e(CLASS_TAG, command);
                }
            else{
                Log.e(CLASS_TAG, "No se comparo");
            }
            Log.e(CLASS_TAG, String.valueOf(roll));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mOrientation.startListening(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mOrientation.stopListening();
    }

}

