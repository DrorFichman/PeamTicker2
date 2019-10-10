package com.teampicker.drorfichman.teampicker.Data;

import com.teampicker.drorfichman.teampicker.tools.DateHelper;

import java.io.Serializable;
import java.util.Date;

public class PlayerGameStat implements Serializable {

    public PlayerGameStat(ResultEnum res, int currGrade, String date) {
        result = res;
        grade = currGrade;
        gameDateString = date;
    }

    public ResultEnum result;
    public int grade;
    String gameDateString;

    public Date getDate() {
        return DateHelper.getDate(this.gameDateString);
    }
}
