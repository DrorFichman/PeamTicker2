package com.teampicker.drorfichman.teampicker.Data;

import java.util.ArrayList;

public class PlayerGameStat {

    public PlayerGameStat(ResultEnum res, int currGrade) {
        result = res;
        grade = currGrade;
    }

    public ResultEnum result;
    public int grade;
}
