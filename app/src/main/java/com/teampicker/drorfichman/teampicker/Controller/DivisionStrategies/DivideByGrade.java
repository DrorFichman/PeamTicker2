package com.teampicker.drorfichman.teampicker.Controller.DivisionStrategies;

import android.content.Context;
import android.util.Log;

import com.teampicker.drorfichman.teampicker.Controller.OptionalDivision;
import com.teampicker.drorfichman.teampicker.Controller.TeamData;
import com.teampicker.drorfichman.teampicker.Controller.TeamDivision;
import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.Data.PlayerAttribute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import androidx.annotation.NonNull;

public class DivideByGrade extends DividerBase {

    @Override
    int optionsCount() {
        return 20;
    }

    @Override
    int gradeOption(Context ctx, OptionalDivision option) {
        return option.score();
    }

    @Override
    boolean preferNewOption(int selected, int another) {
        return another < selected;
    }
}