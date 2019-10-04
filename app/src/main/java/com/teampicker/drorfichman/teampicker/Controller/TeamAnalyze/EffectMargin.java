package com.teampicker.drorfichman.teampicker.Controller.TeamAnalyze;

import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.Data.PlayerParticipation;

import static com.teampicker.drorfichman.teampicker.Controller.TeamAnalyze.CollaborationHelper.MIN_GAMES_TOGETHER;
import static com.teampicker.drorfichman.teampicker.Controller.TeamAnalyze.CollaborationHelper.WIN_RATE_MARGIN;

public class EffectMargin {
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
