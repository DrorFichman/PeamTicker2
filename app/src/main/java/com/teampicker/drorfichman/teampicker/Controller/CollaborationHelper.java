package com.teampicker.drorfichman.teampicker.Controller;

import android.content.Context;

import com.teampicker.drorfichman.teampicker.Data.DbHelper;
import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.Data.PlayerParticipation;
import com.teampicker.drorfichman.teampicker.tools.MathTools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by drorfichman on 9/17/16.
 */
public class CollaborationHelper {

    private static final int RECENT_GAMES = 50;         // games back to look for
    public static final int MIN_GAMES_ANALYSIS = 7;     //
    private static final int MIN_GAMES_TOGETHER = 5;    // games played with another player to consider effect
    private static final int WIN_RATE_MARGIN = 0;       // win rate exceeded by to count effect

    public enum EffectType {
        Positive,
        Negative,
        Equal,
        NotEnoughData
    }

    public static class Collaboration {
        public HashMap<String, PlayerCollaboration> players = new HashMap<>();

        public PlayerCollaboration getPlayer(String name) {
            return players.get(name);
        }

        /**
         * Gets team's overall historical success when playing with each other
         */
        public int getCollaborationWinRate(List<Player> players) {
            int winsAndLoses = 0;
            int wins = 0;
            for (Player p : players) {
                CollaborationHelper.PlayerCollaboration player = getPlayer(p.mName);
                if (player != null) {
                    winsAndLoses += player.overallCollaboratorsWinsAndLoses;
                    wins += player.overallCollaboratorsWins;
                }
            }
            if (winsAndLoses > 0)
                return (wins * 100 / winsAndLoses);
            else
                return 0;
        }

        public int getExpectedWinRateStdDiv(List<Player> players) {
            ArrayList<Integer> diffs = new ArrayList<>();
            for (Player p : players) {
                CollaborationHelper.PlayerCollaboration player = getPlayer(p.mName);
                if (player != null) {
                    int expectedWinRate = player.getExpectedWinRate();
                    if (expectedWinRate != -1) {
                        diffs.add(Math.abs(50 - expectedWinRate));
                    }
                }
            }

            if (diffs.size() > 0) return (int) MathTools.getStdDevFromDiffs(diffs);
            else return -1;
        }
    }

    public static class PlayerCollaboration {
        public String name;
        public int games;
        public int success;
        public int winRate;
        int wins;

        HashMap<String, EffectMargin> collaborators = new HashMap<>();
        HashMap<String, EffectMargin> opponents = new HashMap<>();

        int overallCollaboratorsGames = 0;
        int overallCollaboratorsWins = 0;
        int overallCollaboratorsSuccess = 0;

        int overallCollaboratorsWinRate = 0;
        int overallCollaboratorsWinRateCount = 0;

        // TODO use for win rate with opponents?
        int overallOpponentsGames = 0;
        int overallOpponentsWins = 0;
        int overallOpponentsSuccess = 0;
        int overallCollaboratorsWinsAndLoses;

        PlayerCollaboration(Player p) {
            name = p.mName;
            if (p.statistics != null) {
                games = p.statistics.gamesCount;
                wins = p.statistics.wins;
                success = p.statistics.successRate;
                winRate = p.statistics.getWinRate();
            }
        }

        void addCollaborator(String name, EffectMargin effectData) {
            collaborators.put(name, effectData);
            overallCollaboratorsGames += effectData.gamesWith;
            overallCollaboratorsWinsAndLoses += effectData.winsAndLosesWith;
            overallCollaboratorsWins += effectData.winsWith;
            overallCollaboratorsSuccess += effectData.successWith;

            if (effectData.gamesWith > MIN_GAMES_TOGETHER) {
                overallCollaboratorsWinRateCount++;
                overallCollaboratorsWinRate += effectData.winRateWith;
            }
        }

        void addOpponent(String name, EffectMargin effectData) {
            opponents.put(name, effectData);
            overallOpponentsGames += effectData.gamesWith;
            overallOpponentsWins += effectData.winsWith;
            overallOpponentsSuccess += effectData.successWith;
        }

        EffectMargin getCollaboratorEffect(String name) {
            return collaborators.get(name);
        }

        EffectMargin getOpponentEffect(String name) {
            return opponents.get(name);
        }

        public EffectMargin getEffect(String name) {
            EffectMargin collaboratorEffect = getCollaboratorEffect(name);
            if (collaboratorEffect != null)
                return collaboratorEffect;
            else
                return getOpponentEffect(name);
        }

        public int getExpectedWinRate() {
            if (overallCollaboratorsWinRateCount > 0)
                return overallCollaboratorsWinRate / overallCollaboratorsWinRateCount;
            else
                return -1;
        }

        public int getExpectedWinRateDiff() {
            int expectedWinRate = getExpectedWinRate();
            if (expectedWinRate < 0) {
                return 0;
            } else {
                return expectedWinRate - winRate;
            }
        }

        public String getExpectedWinRateString() {
            int n = getExpectedWinRate();
            if (n > 0) return String.valueOf(n) + '%';
            return "-";
        }
    }

    public static class EffectMargin {
        String collaborator;
        public String player;
        public EffectType effect;
        public int gamesWith;
        public int winsAndLosesWith; // TODO improve logic copy win rate from statistics

        int winsWith;
        int successWith;
        public int winRateWith;

        int winRateMarginWith;
        int successMarginWith;

        EffectMargin(Player player, PlayerParticipation with) {
            this.player = player.mName;
            collaborator = with.mName;
            gamesWith = with.statisticsWith.gamesCount;
            winsAndLosesWith = with.statisticsWith.getWinsAndLosesCount();
            winsWith = with.statisticsWith.wins;
            successWith = with.statisticsWith.successRate;
            winRateWith = with.statisticsWith.getWinRate();

            winRateMarginWith = with.statisticsWith.getWinRate() - player.statistics.getWinRate();
            successMarginWith = with.statisticsWith.successRate - player.statistics.successRate;
            if (gamesWith < MIN_GAMES_TOGETHER) {
                effect = EffectType.NotEnoughData;
            } else if (winRateMarginWith > WIN_RATE_MARGIN) {
                effect = EffectType.Positive;
            } else if (winRateMarginWith < -WIN_RATE_MARGIN) {
                effect = EffectType.Negative;
            } else {
                effect = EffectType.Equal;
            }
        }

        public String getSuccessWithString() {
            if (successWith > 0) return '+' + String.valueOf(successWith);
            return String.valueOf(successWith);
        }
    }

    public static Collaboration getCollaborationData(Context context, List<Player> team1, List<Player> team2) {
        Collaboration result = new Collaboration();
        processTeam(context, result, team1, team2);
        processTeam(context, result, team2, team1);

        return result;
    }

    private static void processTeam(Context context, Collaboration result, List<Player> team, List<Player> other) {

        for (Player currPlayer : team) {
            HashMap<String, PlayerParticipation> collaborationMap = DbHelper.getPlayersParticipationsStatistics(context, RECENT_GAMES, currPlayer.mName);
            PlayerCollaboration playerCollaboration = new PlayerCollaboration(currPlayer);
            if (currPlayer.statistics != null) {
                for (Player with : team) {
                    PlayerParticipation collaborationWith = collaborationMap.get(with.mName);
                    if (collaborationWith != null) {
                        EffectMargin collaboratorEffect = new EffectMargin(currPlayer, collaborationWith);
                        playerCollaboration.addCollaborator(with.mName, collaboratorEffect);
                    }
                }
                for (Player against : other) {
                    PlayerParticipation collaborationWith = collaborationMap.get(against.mName);
                    if (collaborationWith != null) {
                        EffectMargin collaboratorEffect = new EffectMargin(currPlayer, collaborationWith);
                        playerCollaboration.addOpponent(against.mName, collaboratorEffect);
                    }
                }
            }
            result.players.put(currPlayer.mName, playerCollaboration);
        }
    }
}
