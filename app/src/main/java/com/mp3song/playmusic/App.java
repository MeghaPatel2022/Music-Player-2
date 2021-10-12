package com.mp3song.playmusic;

import android.app.Application;

import com.mp3song.playmusic.utils.PreferenceUtil;


public class App extends Application {
    public static final boolean HIDE_INCOMPLETE_FEATURES = true;
    private static App mInstance;

    public static synchronized App getInstance() {
        return mInstance;
    }

    public PreferenceUtil getPreferencesUtility() {
        return PreferenceUtil.getInstance(App.this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}