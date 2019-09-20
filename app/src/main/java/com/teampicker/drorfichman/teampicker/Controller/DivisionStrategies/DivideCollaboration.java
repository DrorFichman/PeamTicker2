package com.teampicker.drorfichman.teampicker.Controller.DivisionStrategies;

import android.content.Context;
import android.util.Log;

import com.teampicker.drorfichman.teampicker.Controller.CollaborationHelper;
import com.teampicker.drorfichman.teampicker.Controller.OptionalDivision;
import com.teampicker.drorfichman.teampicker.Data.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.annotation.NonNull;

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
