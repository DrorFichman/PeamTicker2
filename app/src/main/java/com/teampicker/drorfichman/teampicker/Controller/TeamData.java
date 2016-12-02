package com.teampicker.drorfichman.teampicker.Controller;

import com.teampicker.drorfichman.teampicker.Data.Player;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by drorfichman on 9/17/16.
 */
public class TeamData {
    public List<Player> players = new ArrayList<>();

    public TeamData(List<Player> ps) {
        players = ps;
    }

    public TeamData() {
    }

    public int getSum() {
        int sum = 0;
        for (Player p : players)
            sum += p.mGrade;
        return sum;
    }

    public double getMean() {
        if (players.size() == 0) {
            return 0;
        }
        return getSum() / players.size();
    }

    public int getCount() {
        return players.size();
    }

    private double getVariance() {
        if (players.size() == 0) {
            return 0;
        }
        double mean = getMean();
        double diff = 0;
        for (Player p : players)
            diff += (p.mGrade - mean) * (p.mGrade - mean);
        return diff / players.size();
    }

    public BigDecimal getStdDev() {
        return new BigDecimal(Math.sqrt(getVariance())).round(new MathContext(4));
    }

    public int getSuccess() {
        if (players.size() == 0) {
            return 0;
        }
        int success = 0;
        for (Player p : players) {
            success += p.getSuccess();
        }
        return success;
    }

    public BigDecimal getAverage() {
        if (getCount() != 0) {
            return new BigDecimal((double) getSum() / getCount()).round(new MathContext(3));
        } else {
            return BigDecimal.valueOf(0);
        }
    }
}
