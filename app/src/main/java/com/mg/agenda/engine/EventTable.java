package com.mg.agenda.engine;


import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;


/**
 * Encapsulates all operations with event database cursor
 */
public class EventTable {

    private static final String TAG = EventTable.class.getSimpleName();
    public static final String[] PROJECTION = new String[]{
            CalendarContract.Instances.EVENT_ID,         // 0
            CalendarContract.Instances.ALL_DAY,          // 1
            CalendarContract.Instances.BEGIN,            // 2
            CalendarContract.Instances.END,              // 3
            CalendarContract.Instances.TITLE,            // 4
            CalendarContract.Instances.DISPLAY_COLOR,    // 5
            CalendarContract.Instances.EVENT_TIMEZONE    // 6
    };
    private static List<String> mProjectionList = Arrays.asList(PROJECTION);

    // The indices for the projection array above.
    private static final int ID_INDEX        = mProjectionList.indexOf(CalendarContract.Instances.EVENT_ID);
    private static final int ALLDAY_INDEX    = mProjectionList.indexOf(CalendarContract.Instances.ALL_DAY);
    private static final int START_INDEX     = mProjectionList.indexOf(CalendarContract.Instances.BEGIN);
    private static final int END_INDEX       = mProjectionList.indexOf(CalendarContract.Instances.END);
    private static final int TITLE_INDEX     = mProjectionList.indexOf(CalendarContract.Instances.TITLE);
    private static final int COLOR_INDEX     = mProjectionList.indexOf(CalendarContract.Instances.DISPLAY_COLOR);
    private static final int TIMEZONE_INDEX  = mProjectionList.indexOf(CalendarContract.Instances.EVENT_TIMEZONE);


    public static final String SORT_ORDER = CalendarContract.Instances.BEGIN + " ASC";

    private Context mContext;
    private Cursor mCursor;


    public EventTable(Context context) {
        mContext = context;
    }


    public boolean query(long fromDay, long tillDay) {
        Calendar beginTime = Calendar.getInstance();
        beginTime.setTimeInMillis(fromDay);
        long startMillis = beginTime.getTimeInMillis();

        Calendar endTime = Calendar.getInstance();
        endTime.setTimeInMillis(tillDay);
        long endMillis = endTime.getTimeInMillis();

        // Construct the query with the desired date range.
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, startMillis);
        ContentUris.appendId(builder, endMillis);

        // Submit the query
        mCursor = mContext.getContentResolver().query(builder.build(),
                PROJECTION,
                null,
                null,
                SORT_ORDER);

        if (mCursor == null) {
            Log.e(TAG, "calendar query error");
            return false;
        }

        return true;
    }


    public boolean moveToNext() {
        if (mCursor == null) return false;
        return mCursor.moveToNext();
    }


    public long getId() {
        if (mCursor == null) return -1;
        return mCursor.getLong(ID_INDEX);
    }


    public boolean isAllday() {
        if (mCursor == null) return false;
        return mCursor.getLong(ALLDAY_INDEX) == 1;
    }


    public long getStart() {
        if (mCursor == null) return -1;
        return mCursor.getLong(START_INDEX);
    }


    public long getEnd() {
        if (mCursor == null) return -1;
        return mCursor.getLong(END_INDEX);
    }


    public String getTitle() {
        if (mCursor == null) return "";
        return mCursor.getString(TITLE_INDEX);
    }


    public int getColor() {
        if (mCursor == null) return -1;
        return mCursor.getInt(COLOR_INDEX);
    }


    public String getTimezone() {
        if (mCursor == null) return "";
        return mCursor.getString(TIMEZONE_INDEX);
    }

}
