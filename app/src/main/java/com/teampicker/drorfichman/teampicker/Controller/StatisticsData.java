package com.teampicker.drorfichman.teampicker.Controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Created by drorfichman on 10/8/16.
 */
public class StatisticsData implements Serializable {

    public int wins;
    public int gamesCount;
    public int successRate;

    // games = wins + loses + ties => ties = games - wins - loses
    // success = wins - loses => loses = wins - success

    public StatisticsData() {
    }

    public StatisticsData(int games, int success, int wins) {
        this.gamesCount = games;
        this.successRate = success;
        this.wins = wins;
    }

    public int getWinsAndLosesCount() {
        int loses = wins - successRate;
        return (wins + loses);
    }

    public int getWinRate() {
        int countableGames = getWinsAndLosesCount();
        if (countableGames > 0) {
            return (wins * 100 / countableGames);
        }
        return 0;
    }

    public String getWinRateDisplay() {
        if (gamesCount > 0) {
            return getWinRate() + "%";
        }
        return "-";
    }
}