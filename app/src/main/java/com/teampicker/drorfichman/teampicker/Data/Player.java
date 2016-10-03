package com.teampicker.drorfichman.teampicker.Data;

import java.io.Serializable;

/**
 * Created by drorfichman on 7/27/16.
 */
public class Player implements Serializable {
    public String mName;
    public int mGrade;
    public boolean isGradeDisplayed;
    public boolean isComing;
    public boolean isSelected;

    public Player(String name, int grade) {
        mName = name;
        mGrade = grade;
        isGradeDisplayed = true;
        // mDisplayGrade = grade;
    }

    public void showGrade(boolean show) {
        isGradeDisplayed = show;
//        if (show) {
//            mDisplayGrade = mGrade;
//        } else {
//            mDisplayGrade = 0;
//        }
    }

    @Override
    public String toString() {
        return mName;
//        return mName +
//                (mDisplayGrade != 0 ? " (" + mGrade + ")" : "") +
//                (isSelected ? " <S>" : "");
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
