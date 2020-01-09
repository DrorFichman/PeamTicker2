package com.teampicker.drorfichman.teampicker.tools;

import android.content.Context;
import android.util.Log;

import androidx.preference.PreferenceManager;

public class SettingsHelper {

    public static final String SETTING_DIVIDE_ATTEMPTS = "divide_attempts";
    private static final String SETTING_DIVIDE_GRADE = "divide_grade_percentage";

    private static int getPreferenceValue(Context ctx, String preferenceKey, int defaultValue) {
        try {
            String value = PreferenceManager.getDefaultSharedPreferences(ctx).getString(preferenceKey, String.valueOf(defaultValue));
            return Integer.parseInt(value);
        } catch (Exception e) {
            Log.e(preferenceKey, "failed getting " + preferenceKey, e);
        }
        return defaultValue;
    }

    public static int getDivideAttemptsCount(Context ctx) {
        int divideAttempts = getPreferenceValue(ctx, SETTING_DIVIDE_ATTEMPTS, 50);
        return MathTools.getLimitedValue(divideAttempts, 1, 100);
    }

    public static class DivisionWeight {
        double grade;
        double chemistry;
        double stdDev;

        public double grade() {
            return grade / 100;
        }

        public double chemistry() {
            return chemistry / 100;
        }

        public double stdDev() {
            return stdDev / 100;
        }

        DivisionWeight(int g, int c, int s) {
            grade = g;
            chemistry = c;
            stdDev = s;
        }
    }

    public static DivisionWeight getDivisionWeight(Context ctx) {
        int gradeWeight = getPreferenceValue(ctx, SETTING_DIVIDE_GRADE, 20);
        int grade = MathTools.getLimitedValue(gradeWeight, 0, 100);
        int chemistry = (100 - grade) / 2;
        int stdDev = 100 - grade - chemistry;
        return new DivisionWeight(grade, chemistry, stdDev);
    }
}
