package com.teampicker.drorfichman.teampicker.Controller.DivisionStrategies;

import android.content.Context;

import com.teampicker.drorfichman.teampicker.Controller.OptionalDivision;

public class DivideByGrade extends DividerBase {

    @Override
    int optionsCount() {
        return 20;
    }

    @Override
    int gradeOption(Context ctx, OptionalDivision option) {
        return option.getGradeDiff();
    }

    @Override
    boolean preferNewOption(int selected, int another) {
        return another < selected;
    }
}