package com.teampicker.drorfichman.teampicker.Data;

import com.teampicker.drorfichman.teampicker.Controller.StatisticsData;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by drorfichman on 7/27/16.
 */
public class Player implements Serializable {
    public String mName;
    public int mGrade;
    public boolean isGradeDisplayed;
    public boolean isComing;
    private boolean isMoved;

    public ArrayList<ResultEnum> results = new ArrayList<>();
    public StatisticsData statistics;

    public Player(String name, int grade) {
        mName = name;
        mGrade = grade;
        isGradeDisplayed = true;
    }

    public void showGrade(boolean show) {
        isGradeDisplayed = show;
    }

    @Override
    public String toString() {
        return mName;
    }

    // TODO remove
    public String getResults() {
        String s = "";
        for (ResultEnum r : results) {
            s += r.getChar() + " ";
        }
        return s;
    }

    public int getSuccess() {
        int value = 0;
        for (ResultEnum r : results) {
            if (r != null) {
                value += r.getValue();
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

    public void setStatistics(StatisticsData statistics) {
        this.statistics = statistics;
    }
}
