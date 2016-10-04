package com.teampicker.drorfichman.teampicker.Data;

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
    public boolean isSelected;

    public ArrayList<ResultEnum> results = new ArrayList<>(5);

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

    public String getResults() {
        String s = "";
        for (ResultEnum r : results) {
            s += r.getChar();
        }
        return s;
    }
}
