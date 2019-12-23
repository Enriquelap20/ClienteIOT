package com.example.clienteiot;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesManager {
    private static final String CLASS_TAG = PreferencesManager.class.getSimpleName();
    private PreferencesManager Instance;
    private Context context;
    private String loginPreferences = "MyPrefs" ;
    SharedPreferences sharedPreferences;

    private String username;
    private String password;
    private String ipAddress;
    int port;
    boolean rememberUser;
    boolean rememberLogin;

    public PreferencesManager(Context context) {
        this.context=context;
        this.sharedPreferences = context.getApplicationContext().getSharedPreferences(loginPreferences, 0);
    }

    /*public static synchronized PreferencesManager getInstance(Context contexto) {
        if (Instance == null) {
            Instance = new PreferencesManager(context);
        }
        context = contexto;
        return Instance;
    }*/

    public String getUsername() {
        username=sharedPreferences.getString("username", "");
        return username;
    }

    public void setUsername(String username) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username",username);
        editor.apply();
    }

    public String getPassword() {
        password=sharedPreferences.getString("password", "");
        return password;
    }

    public void setPassword(String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("password",password);
        editor.apply();
    }

    public String getIpAddress() {
        ipAddress=sharedPreferences.getString("ipAddress", "192.168.1.16");
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("ipAddress",ipAddress);
        editor.apply();
    }

    public int getPort() {
        port=sharedPreferences.getInt("port", 49000);
        return port;
    }

    public void setPort(int port) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("port",port);
        editor.apply();
    }

    public boolean isRememberUser() {
        rememberUser= sharedPreferences.getBoolean("saveState", false);
        return rememberUser;
    }

    public void setRememberUser(boolean rememberUser) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("saveState", rememberUser);
        editor.apply();
    }

    public boolean isRememberLogin() {
        rememberLogin=sharedPreferences.getBoolean("autoLogin", false);
        return rememberLogin;
    }

    public void setRememberLogin(boolean rememberLogin) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("autoLogin", rememberLogin);
        editor.apply();
    }
}
