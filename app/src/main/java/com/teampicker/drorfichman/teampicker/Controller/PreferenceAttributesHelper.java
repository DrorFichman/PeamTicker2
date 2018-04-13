package com.teampicker.drorfichman.teampicker.Controller;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by drorfichman on 7/29/16.
 */
public class PreferenceAttributesHelper {

    public static String PREF_PLAYER = "PREF_PLAYER_";

    public enum PlayerAttribute {
        isGK("_is_gk"),
        isDefender("_is_defender"),
        isPlaymaker("_is_playmaker");

        String attribute;

        PlayerAttribute(String att) {
            attribute = att;
        }
    }

    public static boolean getPlayerPreferences(Context context, String player, PlayerAttribute attribute) {
        String pref = getPlayerPrefix(player, attribute);
        return PreferenceHelper.getSharedPreference(context).getBoolean(pref, false);
    }

    public static void setPlayerPreferences(Context context, String player, PlayerAttribute attribute, boolean value) {
        String pref = getPlayerPrefix(player, attribute);
        PreferenceHelper.setSharedPreferenceBoolean(context, pref, value);
    }

    private static String getPlayerPrefix(String player, PlayerAttribute preference) {
        return PREF_PLAYER + player + preference.attribute;
    }

    public static void deletePlayerAttributes(Context context, String player) {
        setPlayerPreferences(context, player, PlayerAttribute.isDefender, false);
        setPlayerPreferences(context, player, PlayerAttribute.isGK, false);
        setPlayerPreferences(context, player, PlayerAttribute.isPlaymaker, false);
    }
}