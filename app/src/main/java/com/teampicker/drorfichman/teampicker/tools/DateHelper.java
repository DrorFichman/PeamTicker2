package com.teampicker.drorfichman.teampicker.tools;

import android.content.Context;
import android.text.format.DateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateHelper {

    private static final String FORMAT = "yyyy-MM-dd";

    public static String getNow() {
        return getDate(System.currentTimeMillis());
    }

    public static String getDate(long millis) {
        return DateFormat.format(FORMAT, millis).toString();
    }

    public static Date getDate(String date) {
        try {
            return new SimpleDateFormat(FORMAT, Locale.getDefault()).parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    public static String getDisplayDate(Context ctx, String date) {
        return DateFormat.getDateFormat(ctx).format(getDate(date));
    }
}
