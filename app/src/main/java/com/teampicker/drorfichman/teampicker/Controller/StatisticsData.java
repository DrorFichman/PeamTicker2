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

    public StatisticsData() {
    }

    public StatisticsData(int games, int success, int wins) {
        this.gamesCount = games;
        this.successRate = success;
        this.wins = wins;
    }

    public int getWinRate() {
        if (gamesCount > 0) {
            return (wins * 100 / gamesCount);
        }
        return 0;
    }

    public String getWinRateDisplay() {
        if (gamesCount > 0) {
            return String.valueOf(getWinRate()) + "%";
        }
        return "-";
    }
}