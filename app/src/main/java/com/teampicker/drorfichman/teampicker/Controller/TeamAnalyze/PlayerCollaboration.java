package com.teampicker.drorfichman.teampicker.Controller.TeamAnalyze;

import com.teampicker.drorfichman.teampicker.Data.Player;

import java.util.HashMap;

import static com.teampicker.drorfichman.teampicker.Controller.TeamAnalyze.CollaborationHelper.MIN_GAMES_TOGETHER;

public class PlayerCollaboration {
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
