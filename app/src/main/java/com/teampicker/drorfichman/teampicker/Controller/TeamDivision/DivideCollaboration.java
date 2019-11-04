package com.teampicker.drorfichman.teampicker.Controller.TeamDivision;

import android.content.Context;

import com.teampicker.drorfichman.teampicker.Data.BuilderPlayerCollaborationStatistics;
import com.teampicker.drorfichman.teampicker.Data.Player;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public class DivideCollaboration extends DividerBase {

    private static final int OPTIONS = 50;

    BuilderPlayerCollaborationStatistics params;

    @Override
    int optionsCount() {
        return OPTIONS;
    }

    @Override
    int gradeOption(Context ctx, OptionalDivision option) {
        return option.winRateStdDiv(ctx, params);
    }

    @Override
    public void divide(Context ctx, @NonNull ArrayList<Player> comingPlayers,
                       @NonNull List<Player> players1,
                       @NonNull List<Player> players2, TeamDivision.onTaskInProgress update) {
        params = new BuilderPlayerCollaborationStatistics().setCached();
        super.divide(ctx, comingPlayers, players1, players2, update);
    }

    @Override
    boolean preferNewOption(int selected, int another) {
        return (another < selected && another > 0) || (selected == -1);
    }
}
