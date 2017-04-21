package com.byteshaft.projecthunger.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppGlobals extends Application {

    private static final String FIRST_RUN_MAP = "first_run_map";
    private static final String UNIQUE_DEVICE_ID = "unique_device_id";
    private static Context sContext;
    private static SharedPreferences sPreferences;

    public static Context getContext() {
        return sContext;
    }

    public static boolean isMapFirstRun() {
        return sPreferences.getBoolean(FIRST_RUN_MAP, true);
    }

    public static void setMapFirstRun(boolean firstRunMap) {
        sPreferences.edit().putBoolean(FIRST_RUN_MAP, firstRunMap).apply();
    }

    public static String getUniqueDeviceId() {
        return sPreferences.getString(UNIQUE_DEVICE_ID, null);
    }

    public static void putUniqueDeviceID(String id  ) {
        sPreferences.edit().putString(UNIQUE_DEVICE_ID, id).apply();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
        sPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }
}
