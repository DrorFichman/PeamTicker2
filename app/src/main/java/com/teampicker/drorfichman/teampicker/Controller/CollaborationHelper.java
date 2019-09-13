package com.teampicker.drorfichman.teampicker.Controller;

import android.content.Context;

import com.teampicker.drorfichman.teampicker.Data.DbHelper;
import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.Data.PlayerParticipation;

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
    private static final int WIN_RATE_MARGIN = 5;       // win rate exceeded by to count effect

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

        public int getCollaborationWinRate(List<Player> players) {
            int games = 0;
            int wins = 0;
            for (Player p : players) {
                CollaborationHelper.PlayerCollaboration player = getPlayer(p.mName);
                if (player != null) {
                    games += player.overallCollaboratorsGames;
                    wins += player.overallCollaboratorsWins;
                }
            }
            if (games > 0)
                return (wins * 100 / games);
            else
                return 0;
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

        int getExpectedWinRate() {
            if (overallCollaboratorsWinRateCount > 0)
                return overallCollaboratorsWinRate / overallCollaboratorsWinRateCount;
            else
                return -1;
        }

        public EffectType getExpectedWinRateType() {
            int expectedWinRate = getExpectedWinRate();
            if (expectedWinRate < 0)
                return EffectType.NotEnoughData;
            else if (expectedWinRate > (winRate + WIN_RATE_MARGIN))
                return EffectType.Positive;
            else if (expectedWinRate < (winRate - WIN_RATE_MARGIN))
                return EffectType.Negative;
            else
                return EffectType.Equal;
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

        int winsWith;
        int successWith;
        public int winRateWith;

        int winRateMarginWith;
        int successMarginWith;

        EffectMargin(Player player, PlayerParticipation with) {
            this.player = player.mName;
            collaborator = with.mName;
            gamesWith = with.statisticsWith.gamesCount;
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

    public static Collaboration getCollaborationData(Context context, ArrayList<Player> team1, ArrayList<Player> team2) {
        Collaboration result = new Collaboration();
        processTeam(context, result, team1, team2);
        processTeam(context, result, team2, team1);

        return result;
    }

    private static void processTeam(Context context, Collaboration result, ArrayList<Player> team, ArrayList<Player> other) {

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
