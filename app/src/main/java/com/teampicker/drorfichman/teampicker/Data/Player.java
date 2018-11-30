package com.teampicker.drorfichman.teampicker.Data;

import com.teampicker.drorfichman.teampicker.Controller.StatisticsData;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by drorfichman on 7/27/16.
 */
public class Player implements Serializable, Comparable {
    public String mName;
    public int mGrade;
    private boolean mIsGradeDisplayed;
    public boolean isComing;
    private boolean isMoved;
    private boolean isMissed;

    public ArrayList<PlayerGameStat> results = new ArrayList<>();
    public StatisticsData statistics;
    public boolean isGK;
    public boolean isDefender;
    public boolean isPlaymaker;

    public Player(String name, int grade) {
        mName = name;
        mGrade = grade;
        mIsGradeDisplayed = true;
    }

    public void showGrade(boolean show) {
        mIsGradeDisplayed = show;
    }

    public boolean isGradeDisplayed() {
        return mIsGradeDisplayed;
    }

    @Override
    public String toString() {
        return mName;
    }

    // TODO improve
    public String getResults() {
        String s = "";
        int grade = 0;
        for (PlayerGameStat r : results) {
            if (r != null) {
                if (r.grade != grade) {
                    grade = r.grade;
                    s += "[" + String.valueOf(grade) + "] ";
                }
                s += r.result.getChar() + " ";
            }
        }
        return s;
    }

    public int getSuccess() {
        int value = 0;
        for (PlayerGameStat r : results) {
            if (r != null) {
                value += r.result.getValue();
            }
        }
        return value;
    }

    public boolean isMoved() {
        return isMoved;
    }

    public void switchMoved(boolean moved) {
        if (!moved) {
            isMoved = false;
        } else {
            isMoved = !isMoved;
        }
    }

    public boolean isMissed() {
        return isMissed;
    }

    public void switchMissed() {
        isMissed = !isMissed;
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
}
