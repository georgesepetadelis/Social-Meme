package com.george.socialmeme.Helpers;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

public class AppHelper {
    public static boolean isNightModeEnabled(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("dark_mode", MODE_PRIVATE);
        return sharedPref.getBoolean("dark_mode", false);
    }
    public static boolean isAutoModeEnabled(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences("auto_mode", MODE_PRIVATE);
        return sharedPref.getBoolean("auto_mode", false);
    }
}
