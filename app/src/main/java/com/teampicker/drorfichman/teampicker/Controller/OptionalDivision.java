package com.teampicker.drorfichman.teampicker.Controller;

import android.content.Context;

/**
 * Created by drorfichman on 9/17/16.
 */
public class OptionalDivision {
    public TeamData players1 = new TeamData();
    public TeamData players2 = new TeamData();

    public int score() {
        return Math.abs(players1.getSum() - players2.getSum());
    }

    public int winRateStdDiv(Context ctx) {
        CollaborationHelper.Collaboration collaborationData = CollaborationHelper.getCollaborationData(ctx, players1.players, players2.players);
        int expectedStdDev1 = collaborationData.getExpectedWinRateStdDiv(players1.players);
        int expectedStdDev2 = collaborationData.getExpectedWinRateStdDiv(players2.players);

        int collaborationWinRate1 = collaborationData.getCollaborationWinRate(players1.players);
        int collaborationWinRate2 = collaborationData.getCollaborationWinRate(players2.players);

        int winRateDiff = Math.abs(collaborationWinRate1 - collaborationWinRate2);

        if (expectedStdDev1 > 0 && expectedStdDev2 > 0)
            return expectedStdDev1 + expectedStdDev2 + winRateDiff;
        else
            return -1;
    }
}
