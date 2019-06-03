package com.mg.agenda.model;


import com.mg.agenda.engine.Caltool;

public class EventModel {

    private boolean mAllday;
    private long mStart, mEnd;
    private String mTitle;
    private int mColor;


    public EventModel(long start, long end, String title, int color) {
        mStart = start;
        mEnd = end;
        mTitle = title;
        mColor = color;
    }

    public EventModel clone() {
        EventModel result = new EventModel(mStart, mEnd, mTitle, mColor);
        return result;
    }


    public String getText() {
        return Caltool.asYearMonthDayHourMinute(mStart) + " " + Caltool.asYearMonthDayHourMinute(mEnd) + " " + mTitle;
    }


    public void setTitle(String title) {
        mTitle = title;
    }


    public String getTitle() {
        return mTitle;
    }


    public void setAllday(boolean allday) {
        mAllday = allday;
    }


    public boolean isAllday() {
        return mAllday;
    }


    public void setStart(long start) {
        mStart = start;
    }

    public long getStart() {
        return mStart;
    }


    public void setEnd(long end) {
        mEnd = end;
    }


    public long getEnd() {
        return mEnd;
    }


    public int getColor() {
        return mColor;
    }

}
