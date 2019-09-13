package com.teampicker.drorfichman.teampicker.Data;

import java.io.Serializable;

public class PlayerGameStat implements Serializable {

    public PlayerGameStat(ResultEnum res, int currGrade) {
        result = res;
        grade = currGrade;
    }

    public ResultEnum result;
    public int grade;
}
