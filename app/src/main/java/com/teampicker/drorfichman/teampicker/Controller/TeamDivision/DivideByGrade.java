package com.teampicker.drorfichman.teampicker.Controller.TeamDivision;

import android.content.Context;

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