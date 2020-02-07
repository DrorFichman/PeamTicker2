package com.teampicker.drorfichman.teampicker.Controller.TeamDivision;

import android.content.Context;

import com.teampicker.drorfichman.teampicker.Controller.TeamAnalyze.Collaboration;
import com.teampicker.drorfichman.teampicker.Controller.TeamAnalyze.CollaborationHelper;
import com.teampicker.drorfichman.teampicker.Data.BuilderPlayerCollaborationStatistics;
import com.teampicker.drorfichman.teampicker.Data.TeamData;
import com.teampicker.drorfichman.teampicker.tools.MathTools;
import com.teampicker.drorfichman.teampicker.tools.SettingsHelper;

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

        // default : 40% team success (winRateDiff), 40% justice for edge players (collaborationStdDev), 20% personal abilities (gradeDiff)
        DivisionWeight weights = SettingsHelper.getDivisionWeight(ctx);

        int chemistryWinRateDiff = getChemistryWinRateDiff(collaborationData); // rank 0 - 20
        int chemistry = MathTools.getPercentageOf(chemistryWinRateDiff, 20);

        int collaborationStdDev = getCollaborationWinRateStdDevFromOptimal(collaborationData); // rank 0 - 30
        int stdDev = MathTools.getPercentageOf(collaborationStdDev, 30);

        int gradeDiff = getGradeDiff(); // rank 0 - 20
        int grade = MathTools.getPercentageOf(gradeDiff, 20);

        int res = (int) (((double) chemistry * weights.chemistry()) + ((double) stdDev * weights.stdDev()) + ((double) grade * weights.grade()));

/*
//        Log.d("DIV1", "WR : " + chemistry + " (" +  chemistryWinRateDiff + "), Std50 : " + stdDev + " (" +  collaborationStdDev + "), Grade : " + grade + " (" + gradeDiff + "), total : " + res);
//        int wr = Math.min(chemistryWinRateDiff / 2, 10);
//        int c = Math.min(collaborationStdDev / 3, 10);
//        int g = Math.min(gradeDiff / 4, 5);
//        Log.d("DIV2", "WR : " + chemistryWinRateDiff + " (" +  wr + "), Std50 : " + collaborationStdDev + " (" +  c + "), Grade : " + gradeDiff + " (" + g + "), total : " + (chemistryWinRateDiff + collaborationStdDev + gradeDiff) + " [" + (g + wr + c) + "]");
//        int res2 = wr + c + g;
//        Log.d("DIV3", res2 + " / 25 = " + res2*100/25 + " == " + res);
*/

        return res;
    }
}
