package com.teampicker.drorfichman.teampicker.Controller;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by drorfichman on 7/29/16.
 */
public class PreferenceHelper {
    private final static String PREF_FILE = "PLAYERS";

    public static String PREF_CURR_GAME_INDEX = "PREF_CURR_GAME_INDEX";
    public static String PREF_MAX_GAME_INDEX = "PREF_MAX_GAME_INDEX";

    /**
     * Set a string shared preference
     * @param key - Key to set shared preference
     * @param value - Value for the key
     */
    static void setSharedPreferenceString(Context context, String key, String value){
        SharedPreferences settings = context.getSharedPreferences(PREF_FILE, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * Set a integer shared preference
     * @param key - Key to set shared preference
     * @param value - Value for the key
     */
    static void setSharedPreferenceInt(Context context, String key, int value){
        SharedPreferences settings = context.getSharedPreferences(PREF_FILE, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    /**
     * Set a Boolean shared preference
     * @param key - Key to set shared preference
     * @param value - Value for the key
     */
    static void setSharedPreferenceBoolean(Context context, String key, boolean value){
        SharedPreferences settings = context.getSharedPreferences(PREF_FILE, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    static SharedPreferences getSharedPreference(Context context){
        return context.getSharedPreferences(PREF_FILE, 0);
    }

    public static int getCurrGame(Context context){
        return PreferenceHelper.getSharedPreference(context).getInt(PreferenceHelper.PREF_CURR_GAME_INDEX, -1);
    }

    public static void setCurrGame(Context context, int curr){
        PreferenceHelper.setSharedPreferenceInt(context, PreferenceHelper.PREF_CURR_GAME_INDEX, curr);
    }

    public static void clearCurrGame(Context context){
        PreferenceHelper.setSharedPreferenceInt(context, PreferenceHelper.PREF_CURR_GAME_INDEX, -1);
    }

    public static int getMaxGame(Context context){
        return PreferenceHelper.getSharedPreference(context).getInt(PreferenceHelper.PREF_MAX_GAME_INDEX, 0);
    }

    public static void setMaxGame(Context context, int max){
        PreferenceHelper.setSharedPreferenceInt(context, PreferenceHelper.PREF_MAX_GAME_INDEX, max);
    }
}