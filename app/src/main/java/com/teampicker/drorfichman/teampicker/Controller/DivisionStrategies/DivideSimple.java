package com.teampicker.drorfichman.teampicker.Controller.DivisionStrategies;

import android.content.Context;

import com.teampicker.drorfichman.teampicker.Controller.OptionalDivision;

public class DivideSimple extends DividerBase {

    @Override
    int gradeOption(Context ctx, OptionalDivision option) {
        return 0;
    }

    @Override
    boolean preferNewOption(int selected, int another) {
        return false;
    }

    @Override
    int optionsCount() {
        return 1;
    }
}
