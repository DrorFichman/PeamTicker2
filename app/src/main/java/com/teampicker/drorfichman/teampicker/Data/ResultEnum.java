package com.teampicker.drorfichman.teampicker.Data;

import android.app.Application;

/**
 * Created by drorfichman on 10/3/16.
 */
public enum ResultEnum {
    Win("W", 1),
    Lose("L", -1),
    Tie("T", 0),
    NA("?", PlayerGamesDbHelper.EMPTY_RESULT);

    ResultEnum(String s, int v) {
        sign = s;
        value = v;
    }

    private final int value;
    private final String sign;

    public String getChar() {
        return sign;
    }

    public int getValue() {
        if (value != PlayerGamesDbHelper.EMPTY_RESULT) {
            return value;
        }
        return 0;
    }

    public static ResultEnum getResultFromOrdinal(int res) {
        for (ResultEnum r : ResultEnum.values()) {
            if (r.ordinal() == res) {
                return r;
            }
        }
        return null;
    }
}
