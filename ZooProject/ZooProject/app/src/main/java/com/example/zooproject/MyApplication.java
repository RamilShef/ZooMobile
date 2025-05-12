package com.example.zooproject;

import android.app.Application;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        com.example.zooproject.AppLifecycleTracker.init(this);
    }
}