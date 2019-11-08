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

    /**
     * Returns a 0.5-1 value
     */
    public static float getAlpha(int value, int max) {
        if (max == 0) return 1;
        float alpha = Math.abs((float) value / max);
        if (alpha > 1) return 1;
        if (alpha < 0.5) return (float) 0.5;
        return alpha;
    }
}
