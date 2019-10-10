package com.teampicker.drorfichman.teampicker.tools;

import android.text.format.DateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateHelper {

    private static final String FORMAT = "dd-MM-yyyy";
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat(FORMAT, Locale.getDefault());;

    public static String getNow() {
        return DateFormat.format(FORMAT, System.currentTimeMillis()).toString();
    }

    public static String getDate(Calendar cal) {
        return DateFormat.format(FORMAT, cal.getTimeInMillis()).toString();
    }

    public static Date getDate(String date) {
        try {
            return DateHelper.dateFormat.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }
}
