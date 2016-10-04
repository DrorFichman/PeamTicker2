package com.teampicker.drorfichman.teampicker.Data;

/**
 * Created by drorfichman on 10/3/16.
 */
public enum ResultEnum {
    Win("W"),
    Lose("L"),
    Tie("T"),
    NA("?");

    ResultEnum(String s) {
        sign = s;
    }

    private String sign;

    public String getChar() {
        return sign;
    }
}
