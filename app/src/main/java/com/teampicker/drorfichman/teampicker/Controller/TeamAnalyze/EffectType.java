package com.teampicker.drorfichman.teampicker.Controller.TeamAnalyze;

import android.graphics.Color;

public enum EffectType { // TODO remove?
    Positive(Color.GREEN),
    Negative(Color.RED),
    Equal(Color.BLACK),
    NotEnoughData(Color.BLACK);

    private int color;

    EffectType(int c) {
        color = c;
    }

    public int getColor() {
        return color;
    }
}
