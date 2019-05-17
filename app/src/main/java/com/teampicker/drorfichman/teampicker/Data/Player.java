package com.teampicker.drorfichman.teampicker.Data;

import android.util.Log;

import com.teampicker.drorfichman.teampicker.Controller.StatisticsData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by drorfichman on 7/27/16.
 */
public class Player implements Serializable, Comparable {
    public String mName;
    public int mGrade;
    public int mBirthYear;
    public int mBirthMonth;
    private int mAge;

    private boolean mIsGradeDisplayed;
    public boolean isComing;
    private boolean isMoved;
    private boolean isMissed;

    public ArrayList<PlayerGameStat> results = new ArrayList<>();
    public StatisticsData statistics;
    public boolean isGK;
    public boolean isDefender;
    public boolean isPlaymaker;

    private static final int RECENT_GAMES_COUNT = 10;

    public int gameResult;

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

    public String getSuggestedChange() {
        int suggest = getSuggestedGrade();
        if (suggest > mGrade) {
            return "+" + (suggest - mGrade);
        } else if (suggest < mGrade) {
            return "-" + (mGrade - suggest);
        } else {
            return "";
        }
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
}
