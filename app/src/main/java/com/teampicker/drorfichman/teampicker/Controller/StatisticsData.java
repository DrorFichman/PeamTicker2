package com.teampicker.drorfichman.teampicker.Controller;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Created by drorfichman on 10/8/16.
 */
public class StatisticsData {

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

    public String getWinRate() {
        if (gamesCount > 0) {
            return new BigDecimal((float) wins * 100 / (float) gamesCount).round(new MathContext(3)).toString();
        }
        return "0";
    }
}