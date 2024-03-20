package com.example.myapplication;

import android.app.Application;

public class EventBuddy extends Application{

    @Override
    public void onCreate() {
        super.onCreate();


    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        GlobalState.destroy();
    }



}
