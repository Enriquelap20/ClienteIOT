package com.example.clienteiot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    public static String CLASS_TAG=LoginActivity.class.getSimpleName();
    //Widgets
    public EditText username;
    public EditText password;
    public Button loginButton;
    public CheckBox saveState;
    public CheckBox autoLogin;

    //Entradas Usuario
    public String userCheck;
    public String passCheck;

    //Base de Datos
    public String user1="Enrique";
    public String pass1="1234";
    public String user2="Veronica";
    public String pass2="5678";

    boolean rememberUser;
    boolean rememberLogin;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username=findViewById(R.id.username);
        password=findViewById(R.id.password);
        loginButton=findViewById(R.id.loginButton);
        saveState=findViewById(R.id.saveState);
        autoLogin=findViewById(R.id.autoLogin);

        final PreferencesManager sharedPreferences=new PreferencesManager(getApplicationContext());

        username.setText(sharedPreferences.getUsername());
        password.setText(sharedPreferences.getPassword());
        saveState.setChecked(sharedPreferences.isRememberUser());
        autoLogin.setChecked(sharedPreferences.isRememberLogin());

        if(sharedPreferences.isRememberLogin()){
            userCheck= sharedPreferences.getUsername();
            passCheck=sharedPreferences.getPassword();
            validateLogin(userCheck, passCheck);
        }
        else{
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    userCheck = username.getText().toString();
                    passCheck = password.getText().toString();

                    rememberUser = saveState.isChecked();

                    if (rememberUser) {
                        sharedPreferences.setUsername(userCheck);
                        sharedPreferences.setPassword(passCheck);
                        sharedPreferences.setRememberUser(rememberUser);

                        Log.e(CLASS_TAG, sharedPreferences.getUsername());
                        Log.e(CLASS_TAG, sharedPreferences.getPassword());
                        Log.e(CLASS_TAG, String.valueOf(sharedPreferences.isRememberUser()));
                    }
                    else {
                        sharedPreferences.setUsername("");
                        sharedPreferences.setPassword("");
                        sharedPreferences.setRememberUser(false);

                        Log.e(CLASS_TAG, sharedPreferences.getUsername());
                        Log.e(CLASS_TAG, sharedPreferences.getPassword());
                        Log.e(CLASS_TAG, String.valueOf(sharedPreferences.isRememberUser()));
                    }
                    validateLogin(userCheck, passCheck);
                }
            });

            saveState.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    rememberLogin=saveState.isChecked();
                    if (!rememberLogin) {
                        sharedPreferences.setUsername("");
                        sharedPreferences.setPassword("");
                        sharedPreferences.setRememberUser(false);
                        sharedPreferences.setRememberLogin(false);
                        autoLogin.setChecked(false);
                    }
                }
            });

            autoLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!autoLogin.isChecked()) {
                        sharedPreferences.setRememberLogin(false);
                    }
                    else{
                          if(saveState.isChecked()) {
                              sharedPreferences.setRememberLogin(true);
                          }
                          else{
                            saveState.setChecked(true);
                            sharedPreferences.setRememberLogin(true);
                            }
                    }
                }
            });
        }
    }

    private void validateLogin(String userCheck, String passCheck) {
        if((userCheck.equals(user1) && passCheck.equals(pass1)||(userCheck.equals(user2) && passCheck.equals(pass2)))){
            Intent homeScreen=new Intent(LoginActivity.this, HomeScreen.class);
            homeScreen.putExtra("name",userCheck);
            startActivity(homeScreen);
            LoginActivity.this.finish();
            Log.e(CLASS_TAG, "Login terminado");
        }
        else{
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.bad_login_msg), Toast.LENGTH_LONG).show();
        }
    }
}
