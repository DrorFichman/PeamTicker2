package com.teampicker.drorfichman.teampicker.Controller.TeamDivision;

import android.content.Context;

public class DivideCollaboration extends DividerBase {

    private static final int OPTIONS = 10;

    @Override
    int optionsCount() {
        return OPTIONS;
    }

    @Override
    int gradeOption(Context ctx, OptionalDivision option) {
        return option.winRateStdDiv(ctx);
    }

    @Override
    boolean preferNewOption(int selected, int another) {
        return (another < selected && another > 0) || (selected == -1);
    }
}
