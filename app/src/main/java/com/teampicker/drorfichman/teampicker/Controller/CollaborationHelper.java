package com.teampicker.drorfichman.teampicker.Controller;

import android.content.Context;
import android.util.Log;

import com.teampicker.drorfichman.teampicker.Data.DbHelper;
import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.Data.PlayerParticipation;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by drorfichman on 9/17/16.
 */
public class CollaborationHelper {

    private static final int RECENT_GAMES = 50; // games back to look for
    private static final int MIN_GAMES_COUNT = 7; // games for player to play over RECENT_GAMES to consider effect
    private static final int MIN_GAMES_TOGETHER = 5; // games played with another player to consider effect
    private static final int WIN_RATE_MARGIN = 5; // win rate exceeded by to count effect

    public static class PredictionResult {
        public TeamCollaboration t1;
        public TeamCollaboration t2;
    }

    public static class TeamCollaboration {
        public int totalGood;
        public int totalBad;

        public HashMap<String, StatisticsData> notEnoughData = new HashMap<>();
        public HashMap<String, PlayerCollaboration> notAffected = new HashMap<>();
        public HashMap<String, PlayerCollaboration> affectedForGood = new HashMap<>();
        public HashMap<String, PlayerCollaboration> affectedForBad = new HashMap<>();

        public String getNotEnoughData() {
            String s = "";
            for (String p : notEnoughData.keySet()) {
                s += p + ". ";
            }
            return s;
        }

        public String getNotAffected() {
            String s = "";
            for (String p : notAffected.keySet()) {
                s += p + ". ";
            }
            return s;
        }

        public String getGoodEffect() {
            String s = "";
            for (String p : affectedForGood.keySet()) {
                s += p + ". ";
            }
            return s;
        }

        public String getBadEffect() {
            String s = "";
            for (String p : affectedForBad.keySet()) {
                s += p + ". ";
            }
            return s;
        }
    }

    static class PlayerCollaboration {
        StatisticsData stats;
        int good;
        int bad;

        PlayerCollaboration(StatisticsData statistics, int goodEffect, int badEffect) {
            stats = statistics;
            good = goodEffect;
            bad = badEffect;
        }
    }

    public static PredictionResult predictWinner(Context context, ArrayList<Player> team1, ArrayList<Player> team2) {
        PredictionResult result = new PredictionResult();
        result.t1 = processTeam(context, team1);
        result.t2 = processTeam(context, team2);

        return result;
    }

    private static TeamCollaboration processTeam(Context context, ArrayList<Player> team) {
        TeamCollaboration coll = new TeamCollaboration();

        for (Player p : team) {
            HashMap<String, PlayerParticipation> ps = DbHelper.getPlayersParticipationsStatistics(context, RECENT_GAMES, p.mName);
            // Log.d("STAT", p.mName + " Played " + p.statistics.gamesCount + " - won " + p.statistics.wins + "(" + p.statistics.getWinRate() + "%)");
            int goodEffect = 0;
            int badEffect = 0;
            if (p.statistics.gamesCount >= MIN_GAMES_COUNT) {
                for (Player with : team) {
                    PlayerParticipation pp = ps.get(with.mName);
                    if (pp != null) {
                        if (pp.statisticsWith.gamesCount >= MIN_GAMES_TOGETHER) {
                            if (pp.statisticsWith.getWinRate() > (p.statistics.getWinRate() + WIN_RATE_MARGIN)) {
                                goodEffect++;
                                // Log.d("STAT", "++WINS++ " + pp.mName + " played With " + pp.statisticsWith.gamesCount + " won " + pp.statisticsWith.wins + "(" + pp.statisticsWith.getWinRate() + "%)");
                            } else if (pp.statisticsWith.getWinRate() < (p.statistics.getWinRate() - WIN_RATE_MARGIN)) {
                                badEffect++;
                                // Log.d("STAT", "--LOSE-- " + pp.mName + " played With " + pp.statisticsWith.gamesCount + " won " + pp.statisticsWith.wins + "(" + pp.statisticsWith.getWinRate() + "%)");
                            }
                        }
                    }
                }
            }
            PlayerCollaboration pcoll = new PlayerCollaboration(p.statistics, goodEffect, badEffect);
            if (goodEffect == 0 && badEffect == 0) { // TODO - can be because of min_games or no_effect
                coll.notEnoughData.put(p.mName, p.statistics);
            } else if (goodEffect == badEffect) {
                coll.notAffected.put(p.mName, pcoll);
            } else if (goodEffect > badEffect) {
                coll.affectedForGood.put(p.mName, pcoll);
            } else { // if (goodEffect < badEffect) {
                coll.affectedForBad.put(p.mName, pcoll);
            }
            coll.totalBad += badEffect;
            coll.totalGood += goodEffect;
        }
        // Log.d("STAT", "Count " + coll.totalGood + "(" + coll.affectedForGood.size() + ")" + " Bad " + coll.totalBad + "(" + coll.affectedForBad.size() + ")");
        return coll;
    }
}
