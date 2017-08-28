package com.ogma.restrohubadmin.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ogma.restrohubadmin.R;
import com.ogma.restrohubadmin.application.App;
import com.ogma.restrohubadmin.application.AppSettings;

public class Splash extends AppCompatActivity {

    private static final String TAG = Splash.class.getName();
    private static final int SPLASH_TIMEOUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        App app = (App) getApplication();
        app.setAppSettings(new AppSettings(this));
        if (app.getAppSettings().__isLoggedIn) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(Splash.this, Home.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }, SPLASH_TIMEOUT);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(Splash.this, Login.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }, SPLASH_TIMEOUT);
        }
    }
}
