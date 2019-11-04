package com.teampicker.drorfichman.teampicker.Controller.TeamDivision;

import android.content.Context;

import com.teampicker.drorfichman.teampicker.Controller.TeamAnalyze.Collaboration;
import com.teampicker.drorfichman.teampicker.Controller.TeamAnalyze.CollaborationHelper;
import com.teampicker.drorfichman.teampicker.Data.BuilderPlayerCollaborationStatistics;
import com.teampicker.drorfichman.teampicker.Data.TeamData;

/**
 * Created by drorfichman on 9/17/16.
 */
public class OptionalDivision {
    public TeamData players1 = new TeamData();
    public TeamData players2 = new TeamData();

    /**
     * Get 2 teams diff of grade sum
     */
    public int getGradeDiff() {
        return Math.abs(players1.getSum() - players2.getSum());
    }

    /**
     * Get 2 team diff of total win rate
     */
    private int getChemistryWinRateDiff(Collaboration collaborationData) {
        int collaborationWinRate1 = collaborationData.getCollaborationWinRate(players1.players);
        int collaborationWinRate2 = collaborationData.getCollaborationWinRate(players2.players);

        return Math.abs(collaborationWinRate1 - collaborationWinRate2);
    }

    /**
     * Get 2 team sum of std dev from 50% win rate based on in-team collaboration
     */
    private int getCollaborationWinRateStdDevFromOptimal(Collaboration collaborationData) {
        int expectedStdDev1 = collaborationData.getExpectedWinRateStdDiv(players1.players);
        int expectedStdDev2 = collaborationData.getExpectedWinRateStdDiv(players2.players);
        if (expectedStdDev1 > 0 && expectedStdDev2 > 0)
            return expectedStdDev1 + expectedStdDev2;
        else
            return -1;
    }

    public int winRateStdDiv(Context ctx, BuilderPlayerCollaborationStatistics params) {
        Collaboration collaborationData = CollaborationHelper.getCollaborationData(ctx, players1.players, players2.players, params);

        // 40% team success (winRateDiff), 40% justice for edge players (collaborationStdDev), 20% personal abilities (gradeDiff)
        // Overall division grade calculated 0-25, the lower the better
        int chemistryWinRateDiff = getChemistryWinRateDiff(collaborationData); // rank out of 10 (0 - 20)
        int collaborationStdDev = getCollaborationWinRateStdDevFromOptimal(collaborationData); // rank out of 10 (0 - 30)
        int gradeDiff = getGradeDiff(); // rank out of 5 (0 - 20)

        int wr = getStandardScore(10, 2, chemistryWinRateDiff);
        int c = getStandardScore(10, 3, collaborationStdDev);
        int g = getStandardScore(5, 4, gradeDiff);

        // Log.d("DIV", "WR : " + chemistryWinRateDiff + " (" +  wr + "), Std50 : " + collaborationStdDev + " (" +  c + "), Grade : " + gradeDiff + " (" + g + "), total : " + (chemistryWinRateDiff + collaborationStdDev + gradeDiff) + " [" + (g + wr + c) + "]");

        return wr + c + g;
    }

    private int getStandardScore(int max, int steps, int value) {
        return Math.min(value / steps, max);
    }
}
