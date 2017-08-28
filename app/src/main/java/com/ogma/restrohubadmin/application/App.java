package com.ogma.restrohubadmin.application;

import android.app.Application;

/**
 * Created by alokdas on 18/12/15.
 */
public class App extends Application {

    private AppSettings mAppSettings = null;

    public void setAppSettings(AppSettings obj) {
        // TODO Auto-generated method stub
        mAppSettings = obj;
    }

    public AppSettings getAppSettings() {
        // TODO Auto-generated method stub
        return mAppSettings;
    }
}
