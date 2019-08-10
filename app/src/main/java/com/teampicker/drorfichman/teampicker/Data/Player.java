package com.teampicker.drorfichman.teampicker.Data;

import com.teampicker.drorfichman.teampicker.Controller.StatisticsData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by drorfichman on 7/27/16.
 */
public class Player implements Serializable, Comparable {
    public String mName;
    public int mGrade;
    public int mBirthYear;
    public int mBirthMonth;
    private int mAge;

    public boolean isComing;

    public ArrayList<PlayerGameStat> results = new ArrayList<>();
    public StatisticsData statistics;
    public boolean isGK;
    public boolean isDefender;
    public boolean isPlaymaker;
    public boolean isBreakable;

    private static final int RECENT_GAMES_COUNT = 10;

    public int gameResult;

    public Player(String name, int grade) {
        mName = name;
        mGrade = grade;
    }

    @Override
    public String toString() {
        return mName;
    }

    public int getSuggestedGrade() {
        if (results == null || results.size() < 5) {
            return mGrade;
        } else if (Math.abs(getAverageGrade() - mGrade) > 1) {
            return mGrade;
        } else {
            int suggestedGrade = getAverageGrade() + getSuccess() / 2;
            return Math.max(Math.min(suggestedGrade, 99), 1);
        }
    }

    public int getSuggestedGradeDiff() {
        return getSuggestedGrade() - mGrade;
    }

    private int getAverageGrade() {
        if (results != null && results.size() > 0) {
            int gameCount = 0;
            int historicGrade = 0;
            for (PlayerGameStat r : results) {
                if (r != null) {
                    historicGrade += r.grade;
                    gameCount++;
                }
                if (gameCount > RECENT_GAMES_COUNT) {
                    break;
                }
            }
            return historicGrade / gameCount;
        } else {
            return mGrade;
        }
    }

    // TODO improve
    public String getResults() {
        String s = "";
        int grade = 0;
        if (results != null) {
            for (PlayerGameStat r : results) {
                if (r != null) {
                    if (r.grade != grade) {
                        grade = r.grade;
                        s += "[" + String.valueOf(grade) + "->] ";
                    }
                    s += r.result.getChar() + " ";
                }
            }
        }
        return s;
    }

    public int getSuccess() {
        int gameCount = 0;
        int value = 0;
        for (PlayerGameStat r : results) {
            if (r != null) {
                int val = r.result.getValue();
                if (val <= 1 && val >= -1) {
                    value += r.result.getValue();
                    gameCount++;
                }
            }
            if (gameCount > RECENT_GAMES_COUNT) {
                break;
            }
        }
        return value;
    }

    public void setStatistics(StatisticsData statistics) {
        this.statistics = statistics;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof Player) {
            Player p2 = (Player) o;
            if (this.mGrade > p2.mGrade) {
                return -1;
            } else if (this.mGrade == p2.mGrade) {
                return 0;
            } else {
                return 1;
            }
        }

        return -1;
    }

    public int getAge() {
        if (mAge > 0)
            return mAge;
        else if (mBirthYear > 0) {
            int currYear = Calendar.getInstance().get(Calendar.YEAR);
            int currMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;

            return (currYear - mBirthYear) + (currMonth > mBirthMonth ? 0 : -1);
        } else {
            return -1;
        }
    }

    public void setAge(int age) {
        mAge = age;
    }

    public boolean hasAttributes() {
        return isGK || isPlaymaker || isDefender || isBreakable;
    }

    public String getAttributes() {
        if (!hasAttributes()) return "";

        String attributes = "";
        if (isBreakable) attributes += "B,";
        if (isGK) attributes += "GK,";
        if (isPlaymaker) attributes += "PM,";
        if (isDefender) attributes += "D,";
        return attributes.substring(0,attributes.length()-1);
    }
}
