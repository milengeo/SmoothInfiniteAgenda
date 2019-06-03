package com.mg.agenda.engine;

import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class Caltool {

    private static final String TAG = Caltool.class.getSimpleName();

    public static final long SECOND_MILLIS = 1000;
    public static final long MINUTE_MILLIS = SECOND_MILLIS * 60;
    public static final long HOUR_MILLIS = MINUTE_MILLIS * 60;
    public static final long DAILY_MILLIS = HOUR_MILLIS * 24;
    public static final long WEEK_MILLIS = DAILY_MILLIS * 7;


    /**
     * Gets today time in milliseconds
     * @return  today time in milliseconds
     */
    public static long now() {
        return System.currentTimeMillis();
    }


    public static long getMoment(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);
        return calendar.getTimeInMillis();
    }


    public static long getMoment(long moment, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(moment);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);
        return calendar.getTimeInMillis();
    }


    public static String asMonthDay(long moment) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(moment);
        Date date = calendar.getTime();
        SimpleDateFormat timeFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
        String result = timeFormat.format(date);
        return result;
    }

    public static String asMonthDayHourMinute(long moment) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(moment);
        Date date = calendar.getTime();
        SimpleDateFormat timeFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
        String result = timeFormat.format(date);
        return result;
    }

    public static String asDowMonthDay(long moment) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(moment);
        Date date = calendar.getTime();
        SimpleDateFormat timeFormat = new SimpleDateFormat("EEEE, MMM dd", Locale.getDefault());
        String result = timeFormat.format(date);
        return result;
    }


    public static String asDowMonthDayYear(long moment) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(moment);
        Date date = calendar.getTime();
        SimpleDateFormat timeFormat = new SimpleDateFormat("EEEE, MMM dd yyyy", Locale.getDefault());
        String result = timeFormat.format(date);
        return result;
    }


    public static String asDowMonthDayHourMinute(long moment) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(moment);
        Date date = calendar.getTime();
        SimpleDateFormat timeFormat = new SimpleDateFormat("EEEE, MMM dd HH:mm", Locale.getDefault());
        String result = timeFormat.format(date);
        return result;
    }

    
    
    public static String asMonthDayYear(long moment) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(moment);
        Date date = calendar.getTime();
        SimpleDateFormat timeFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String result = timeFormat.format(date);
        return result;
    }


    public static String asYearMonthDay(long moment) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(moment);
        Date date = calendar.getTime();
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String result = timeFormat.format(date);
        return result;
    }


    public static String asYearMonthDayHourMinute(long moment) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(moment);
        Date date = calendar.getTime();
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd  HH:mm", Locale.getDefault());
        String result = timeFormat.format(date);
        return result;
    }


    public static String asHourMinute(long moment) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(moment);
        Date date = calendar.getTime();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String result = timeFormat.format(date);
        return result;
    }


    public static String asHourMinuteSecond(long moment) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(moment);
        Date date = calendar.getTime();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String result = timeFormat.format(date);
        return result;
    }


    public static String asYearMonthDayHourMinuteSecond(long moment) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(moment);
        Date date = calendar.getTime();
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
//        TimeZone timeZone = TimeZone.getDefault();
//        timeFormat.setTimeZone(timeZone);
//        Log.d(TAG, "timezone: " + timeZone.getDisplayName());
        String result = timeFormat.format(date);
        return result;
    }


    public static boolean isWeekend(long day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(day);
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) return true;
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) return true;
        return false;
    }



    public static long moveMinutes(long moment, int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(moment);
        calendar.add(Calendar.MINUTE, minutes);
        return calendar.getTimeInMillis();
    }


    public static long moveDays(long moment, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(moment);
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar.getTimeInMillis();
    }


    public static long getDayStart(long moment) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(moment);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);
        return calendar.getTimeInMillis();
    }


    public static long getDayEnd(long moment) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(getDayStart(moment));
        calendar.add(Calendar.DAY_OF_MONTH, 1);
         calendar.add(Calendar.MINUTE, -1);
        return calendar.getTimeInMillis();
    }


    public static long getDayCount(long moment) {
        return moment / Caltool.DAILY_MILLIS;
    }


    public static long moveMonths(long moment, int months) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(moment);
        calendar.add(Calendar.MONTH, months);
        return calendar.getTimeInMillis();
    }


    public static long getMonthStart(long moment) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(moment);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return calendar.getTimeInMillis();
    }


    public static long getMonthEnd(long moment) {
        Calendar calendar =Calendar.getInstance();
        calendar.setTimeInMillis(getMonthStart(moment));
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.MINUTE, -1);
        return calendar.getTimeInMillis();
    }


    public static int getYear(long moment) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(moment);
        return calendar.get(Calendar.YEAR);
    }


    public static long utcToLocal(long utc) {
        long offset = TimeZone.getDefault().getRawOffset() + TimeZone.getDefault().getDSTSavings();
        long result = utc - offset;
        return result;
    }


    public static String getDayTag(long day) {
        return " " + getDayCount(day) + " " + asYearMonthDay(day);
    }


    public static void selfTest() {
        Log.d(TAG, " ");
        Log.d(TAG, " ");
        Log.d(TAG, " ******** ");
        Log.d(TAG, "Caltool.asMmmdy(): " + Caltool.asYearMonthDay(Caltool.now()));
        Log.d(TAG, "Caltool.asYmd(): " + Caltool.asYearMonthDay(Caltool.now()));
        Log.d(TAG, "Caltool.asYmdhms(): " + Caltool.asYearMonthDayHourMinuteSecond(Caltool.now()));

        Log.d(TAG, " ");
        Log.d(TAG, "Next day: " + Caltool.asYearMonthDay(Caltool.moveDays(Caltool.now(), 1)));
        Log.d(TAG, "Prev day: " + Caltool.asYearMonthDay(Caltool.moveDays(Caltool.now(), -1)));

        Log.d(TAG, " ");
        Log.d(TAG, "Next month: " + Caltool.asYearMonthDay(Caltool.moveMonths(Caltool.now(), 1)));
        Log.d(TAG, "Prev month: " + Caltool.asYearMonthDay(Caltool.moveMonths(Caltool.now(), -1)));

        Log.d(TAG, " ");
        Log.d(TAG, "Next year: " + Caltool.asYearMonthDay(Caltool.moveMonths(Caltool.now(), 12)));
        Log.d(TAG, "Prev year: " + Caltool.asYearMonthDay(Caltool.moveMonths(Caltool.now(), -12)));

        Log.d(TAG, " ");
        Log.d(TAG, "Month start: " + Caltool.asYearMonthDayHourMinuteSecond(Caltool.getMonthStart(Caltool.now())));
        Log.d(TAG, "Month end: " + Caltool.asYearMonthDayHourMinuteSecond(Caltool.getMonthEnd(Caltool.now())));
    }


}
