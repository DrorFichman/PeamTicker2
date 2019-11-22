package com.teampicker.drorfichman.teampicker.Controller.TeamDivision;

import android.content.Context;

public class DivideByGrade extends DividerBase {

    @Override
    int gradeOption(Context ctx, OptionalDivision option) {
        return option.getGradeDiff();
    }

    @Override
    boolean preferNewOption(int current, int another) {
        return another < current;
    }
}