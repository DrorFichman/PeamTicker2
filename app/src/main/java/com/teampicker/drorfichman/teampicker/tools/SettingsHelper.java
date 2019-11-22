package com.teampicker.drorfichman.teampicker.tools;

import android.content.Context;
import android.util.Log;

import androidx.preference.PreferenceManager;

public class SettingsHelper {

    public static int getDivideAttemptsCount(Context ctx) {
        int divideAttempts = 50; // default
        String value = PreferenceManager.getDefaultSharedPreferences(ctx).getString("divide_attempts", "50");
        try {
            divideAttempts = Integer.parseInt(value);
        } catch (Exception e) {
            Log.e("getDivideAttemptsCount", "failed getting divide_attempts : " + value + e);
        }
        return Math.min(divideAttempts, 100); // max
    }
}
