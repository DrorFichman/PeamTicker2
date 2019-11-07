package com.teampicker.drorfichman.teampicker.Controller.TeamAnalyze;

import android.graphics.Color;

public enum EffectType {
    Positive(Color.GREEN),
    Negative(Color.RED),
    Equal(Color.BLACK),
    NotEnoughData(Color.BLACK);

    public int color;

    EffectType(int c) {
        color = c;
    }
}
