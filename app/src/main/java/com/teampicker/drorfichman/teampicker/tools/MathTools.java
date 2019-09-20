package com.teampicker.drorfichman.teampicker.tools;

import java.util.List;

public class MathTools {

    public static double getStdDevFromDiffs(List<Integer> list) {

        int total = 0;
        for (Integer diff : list) {
            total += Math.pow(diff, 2);
        }

        if (list.size() > 0) return Math.sqrt(total / list.size());
        else return -1;
    }
}
