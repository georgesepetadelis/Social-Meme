package com.george.socialmeme.Helpers;

import static android.content.Context.MODE_PRIVATE;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class AppHelper {
    public static boolean isNightModeEnabled(Context context) {

        SaverHelper saverHelper = new SaverHelper(context, "theme_mode");
        boolean isTimeBasedThemeEnabled = saverHelper.getSaverValue("theme_mode", "none").equals("Time based");
        boolean isSystemThemeEnabled = saverHelper.getSaverValue("theme_mode", "none").equals("System theme");

        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);

        if (isTimeBasedThemeEnabled) {
            return currentHour >= 20 || currentHour <= 6;
        } else if (isSystemThemeEnabled) {
            int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
        } else {
            SharedPreferences sharedPref = context.getSharedPreferences("dark_mode", MODE_PRIVATE);
            return sharedPref.getBoolean("dark_mode", false);
        }

    }

    public static boolean isAppInstalledFromPlayStore(Context context) {
        List<String> validInstallers = new ArrayList<>(Arrays.asList("com.android.vending", "com.google.android.feedback"));
        final String installer = context.getPackageManager().getInstallerPackageName(context.getPackageName());
        return installer != null && validInstallers.contains(installer);
    }

    public static void updateNightModeState(boolean nightModeEnabled, Context context) {

        SharedPreferences sharedPref = context.getSharedPreferences("dark_mode", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("dark_mode", nightModeEnabled);
        editor.apply();
    }

}
