package com.teampicker.drorfichman.teampicker.Controller;

import android.content.Context;

import com.teampicker.drorfichman.teampicker.Data.Player;
import com.teampicker.drorfichman.teampicker.Data.PlayerAttribute;
import com.teampicker.drorfichman.teampicker.Data.PlayerDbHelper;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by drorfichman on 9/17/16.
 */
public class TeamData {
    List<Player> allPlayers = new ArrayList<>();
    public List<Player> players = new ArrayList<>();

    /**
     * @param ps players data
     * @param count consider top players count
     */
    public TeamData(List<Player> ps, int count) {
        allPlayers = ps;

        if (ps.size() > count) {
            List<Player> clone = TeamDivision.cloneList(ps);
            Collections.sort(clone);
            players = clone.subList(0, count);
        } else {
            players = ps;
        }
    }

    public TeamData(List<Player> ps) {
        this(ps, ps.size());
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

    public int getAllCount() {
        if (allPlayers != null) {
            return allPlayers.size();
        } else {
            return getCount();
        }
    }

    public int getCount() {
        return players.size();
    }

    public int getCount(List<PlayerAttribute> attributes) {
        int count = 0;
        for (Player player : players) {
            for (PlayerAttribute att : attributes) {
                if (player.isAttribute(att)) {
                    count++;
                    break;
                }
            }
        }
        return count;
    }

    public int getCount(PlayerAttribute attribute) {
        if (attribute == null) {
            return getCount();
        }

        int count = 0;
        for (Player player : players) {
            if (player.isAttribute(attribute)) {
                count++;
            }
        }
        return count;
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

    public int getAge() {
        int sum = 0;
        int count = 0;
        for (Player p : allPlayers) {
            int age = p.getAge();
            if (age > 0) {
                sum += age;
                count++;
            }
        }
        if (count > 0) {
            return sum / count;
        } else {
            return -1;
        }
    }

    public int getWinRate() {
        int totalGames = 0;
        int totalWins = 0;
        for (Player p : players) {
            if (p.statistics != null) {
                totalGames += p.statistics.getWinsAndLosesCount();
                totalWins += p.statistics.wins;
            }
        }
        if (totalGames > 0) {
            return (totalWins * 100 / totalGames);
        } else {
            return 0;
        }
    }
}
