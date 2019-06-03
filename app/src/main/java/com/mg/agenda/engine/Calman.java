package com.mg.agenda.engine;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.mg.agenda.R;
import com.mg.agenda.model.EventModel;

import java.util.Collections;
import java.util.LinkedList;


/**
 * A singleton class responsible for processing of search queries.
 * Its lifecycle is linked to the application.
 */


public class Calman {

    /**
     * Callback interface for events
     */
    public interface Listener {
        void onUpdateList();
    }

    private static final String TAG = Calman.class.getSimpleName();
    private static final boolean SIMULATION = false;
    private static final int FIND_FROM_HOUR = 9;
    private static final int FIND_FROM_MINUTE = 0;
    private static final int FIND_TILL_HOUR = 17;
    private static final int FIND_TILL_MINUTE = 0;

    private static final Calman sInstance = new Calman();
    private Context mContext;  // application context
    private Handler mHandler;
    private Listener mListener;
    private volatile boolean mQuerying;
    private LinkedList<EventModel> mListFeed = new LinkedList<>();
    private LinkedList<EventModel> mFindFeed = new LinkedList<>();
    private LinkedList<EventModel> mLongFeed = new LinkedList<>();
    private LinkedList<EventModel> mDayEvents = new LinkedList<>();
    private String mNoEventStr = "";
    private int mNewDays, mOldDays;
    private boolean mPermitted = false;


    public static Calman get() {
        return sInstance;
    }


    private Calman() {
    }


    /**
     * One time setup, at the time of application creation
     * @param context
     */
    public void initialize(Context context) {
        mContext = context;
        mHandler = new Handler(); // Handler of the main thread
        mNoEventStr = context.getResources().getString(R.string.no_event);
    }


    public void subscribe(Listener listener) {
        mListener = listener;
    }

    public void unsubscribe() {
        mListener = null;
    }


    public boolean isPermitted() {
        return mPermitted;
    }

    public void setPermitted(boolean permitted) {
        mPermitted = permitted;
    }


    // *** queries

    public void query(int newDays, int oldDays) {
        if (!mPermitted) return;
        if (mQuerying) return;
        Log.d(TAG, " * query, new: " + newDays + ", old: " + oldDays);
        mQuerying = true;
        mNewDays = newDays;
        mOldDays = oldDays;
        QueryTask task = new QueryTask();
        task.start();
    }


    /**
     * The thread for queries
     */
    private class QueryTask extends Thread {
        @Override
        public void run() {
            if (mNewDays > 0) {
                doNewQuery();
            }
            if (mOldDays > 0) {
                doOldQuery();
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    onQueryDone();
                }
            });
        }
    }


    private void onQueryDone() {
        Log.d(TAG, " * onQueryDone, new: " + ItemDepot.get().getNewerCount()
            + " old: " + ItemDepot.get().getOlderCount());
        if (mListener != null) {
            mListener.onUpdateList();
        }
        mQuerying = false;
    }


    private void doNewQuery(){
        long fromDay = ItemDepot.get().getNewestDay();
        long tillDay = Caltool.moveDays(fromDay, mNewDays -1);
        Log.d(TAG, " * doNewQuery_1: " + Caltool.getDayTag(fromDay) + "/" + Caltool.getDayTag(tillDay));

        loadListFeed(fromDay, tillDay);

        mLongFeed.clear();
        long day = fromDay;
        while (day <= tillDay) {
            takeDayEvents(day);
            day = Caltool.moveDays(day, 1);
        }
    }


    private void doOldQuery(){
        long endDay = ItemDepot.get().getOldestDay();
        long startDay = Caltool.moveDays(endDay, -(mOldDays -1));
        Log.d(TAG, " * doOldQuery_1: " + Caltool.getDayTag(startDay) + "/" + Caltool.getDayTag(endDay));

        loadListFeed(startDay, endDay);
        Collections.reverse(mListFeed);

        long day = endDay;
        while (day >= startDay) {
            //Log.d(TAG, "doOldQuery_2: " + Caltool.getDayTag(day));
            takeDayEvents(day);
            day = Caltool.moveDays(day, -1);
        }
    }


    private void takeDayEvents(long day) {
        Log.d(TAG, "takeDayEvents: " + Caltool.asMonthDayHourMinute(day));
        ItemDepot.get().startDayUpdate(day);
        ItemDepot.get().addGroup(day);

        mDayEvents.clear();
        pullDayEvents(day);
        if (mDayEvents.size() == 0) {
            //Log.d(TAG, "takeDayEvents NONE");
            ItemDepot.get().addLabel(mNoEventStr);
        } else {
            for (EventModel event : mDayEvents) {
                Log.d(TAG, "takeDayEvents event: " + event.getTitle());
                ItemDepot.get().addEvent(event);
            }
        }
        ItemDepot.get().finishDayUpdate();
    }


    private void pullDayEvents(long dayStart) {
        long dayEnd = Caltool.getDayEnd(dayStart);

        if (ItemDepot.get().isOlderUpdate()) {
            // old list
            pullOldEvents(dayStart, dayEnd);

        } else {
            // new list
            while (mListFeed.size() > 0) {
                if (!pullNextNew(dayStart, dayEnd))
                    break;
            }
            pullLongNew(dayStart, dayEnd);
        }
    }


    // * new events

    private boolean pullNextNew(long dayStart, long dayEnd) {
//        Log.d(TAG, " = pullNextNew day: "
//                + " s" + Caltool.asMonthDayHourMinute(dayStart)
//                + " e" + Caltool.asMonthDayHourMinute(dayEnd));
        EventModel event = mListFeed.peek();
        if (event == null) return false;
//        Log.d(TAG, "pullNextNew peek: " + event.getTitle()
//                + " s" + Caltool.asMonthDayHourMinute(event.getStart())
//                + " e" + Caltool.asMonthDayHourMinute(event.getEnd()));
        if (!isBetween(event.getStart(), dayStart, dayEnd)) {
            return false;
        }

        // consume it
        mListFeed.pop();

        if (wholeEvent(event, dayStart, dayEnd)) {
            event.setAllday(true);
        }

        long nextDay = dayEnd + Caltool.MINUTE_MILLIS;
        if (event.getEnd() > nextDay) {
//            Log.d(TAG, " * pullNextNew to add long: " + event.getTitle()
//                    + " e" + Caltool.asMonthDayHourMinute(event.getEnd())
//                    + " nd" + Caltool.asMonthDayHourMinute(nextDay));

            EventModel dayEvent = event.clone();
            dayEvent.setEnd(dayEnd);

            event.setStart(nextDay);
            mLongFeed.add(event);
//            Log.d(TAG, " * pullNextNew added long: " + event.getTitle()
//                    + " s" + Caltool.asMonthDayHourMinute(event.getStart())
//                    + " e" + Caltool.asMonthDayHourMinute(event.getEnd()));
//
//            Log.d(TAG, " * pullNextNew take first long: " + dayEvent.getTitle()
//                    + " s" + Caltool.asMonthDayHourMinute(dayEvent.getStart())
//                    + " e" + Caltool.asMonthDayHourMinute(dayEvent.getEnd()));
            mDayEvents.add(dayEvent);
            return true;
        }

        Log.d(TAG, "pullNextNew pulling: " + event.getTitle());
        mDayEvents.add(event);
        return true;
    }


    private void pullLongNew(long dayStart, long dayEnd) {
        if (mLongFeed.size() == 0) return;
//        Log.d(TAG, "  pullLongNew_11: " + Caltool.asMonthDayHourMinute(dayStart) + ", "
//                                        + Caltool.asMonthDayHourMinute(dayEnd));

        // first, remove the past events
        while (mLongFeed.size() > 0) {
            EventModel event = mLongFeed.peek();
            if (event.getEnd() < dayStart) {
                Log.d(TAG, "pullLongNew_remove_past: " + Caltool.asMonthDayHourMinute(event.getEnd()));
                mLongFeed.pop();
            } else {
                break;
            }
        }

        // second, are there events crossing this day
        for (EventModel longEvent : mLongFeed) {
            if (longEvent.getStart() < dayEnd && longEvent.getEnd() > dayStart) {
//                Log.d(TAG, "   * pullLongNew crossing: " + longEvent.getTitle()
//                        + " s" + Caltool.asMonthDayHourMinute(longEvent.getStart())
//                        + " e" + Caltool.asMonthDayHourMinute(longEvent.getEnd())
//                        + " d" + Caltool.asMonthDayHourMinute(dayStart));

                longEvent.setStart(Caltool.moveDays(longEvent.getStart(), 1));
//                Log.d(TAG, "pullLongNew move start: " + longEvent.getTitle()
//                        + " " + Caltool.asDowMonthDayHourMinute(dayStart));

                EventModel dayEvent = longEvent.clone();
                dayEvent.setStart(dayStart);

                if (dayEvent.getEnd() > dayEnd) {
                    dayEvent.setEnd(dayEnd);
//                    Log.d(TAG, " * pullLongNew limit the end: " + dayEvent.getTitle()
//                            + " s" + Caltool.asMonthDayHourMinute(dayEvent.getStart())
//                            + " e" + Caltool.asMonthDayHourMinute(dayEvent.getEnd()));
                }

                if (wholeEvent(dayEvent, dayStart, dayEnd)) {
//                    Log.d(TAG, " * pullLongNew whole: " + dayEvent.getTitle()
//                            + " s" + Caltool.asMonthDayHourMinute(dayEvent.getStart())
//                            + " e" + Caltool.asMonthDayHourMinute(dayEvent.getEnd()));
                    dayEvent.setAllday(true);
                }

//                Log.d(TAG, " * pullLongNew taking: " + dayEvent.getTitle()
//                        + " s" + Caltool.asMonthDayHourMinute(dayEvent.getStart())
//                        + " e" + Caltool.asMonthDayHourMinute(dayEvent.getEnd()));
                mDayEvents.add(dayEvent);
            }
        }
    }






    // * old events

    private void pullOldEvents(long dayStart, long dayEnd) {
        while (mListFeed.size() > 0) {
             if (!pullNextOld(dayStart, dayEnd))
                 break;
        }
    }



    private boolean pullNextOld(long dayStart, long dayEnd) {
        EventModel event = mListFeed.peek();
        if (event == null) return false;
        //Log.d(TAG, "pullEvents: " + event.getTitle() + " " + Caltool.getDayTag(dayStart));
        if (!isBetween(event.getStart(), dayStart, dayEnd)) {
            return false;
        }

        mListFeed.pop();

        if (isBetween(dayStart, event.getStart(), event.getEnd())
                && isBetween(dayEnd, event.getStart(), event.getEnd())) {
            event.setAllday(true);
        }
        mDayEvents.add(event);

        return true;
    }


    private boolean isBetween(long moment, long start, long end) {
        if (moment < start) {
            return false;
        }
        if (moment > end) {
            return false;
        }
        return true;
    }



    private boolean wholeEvent(EventModel event, long dayStart, long dayEnd) {
        if (event.getStart() <= dayStart && event.getEnd() >= dayEnd)
            return true;
        else
            return false;
    }


    private void loadListFeed(long fromDay, long tillDay) {
        if (SIMULATION) {
            simulateFeed(fromDay, tillDay);
            return;
        }

        EventTable table = new EventTable(mContext);
        table.query(fromDay, tillDay);

        while (table.moveToNext()) {
            long start = table.getStart();
            long end = table.getEnd();
            String title = table.getTitle();
            int color = table.getColor();
            if (table.isAllday()) {
                start = Caltool.utcToLocal(start);
                end = Caltool.utcToLocal(end);
            }
            mListFeed.add(new EventModel(start, end, title, color));
//            Log.d(TAG, "loadFeed: " + title +
//                " " + Caltool.getDayTag(start) +
//                " " + Caltool.getDayTag(end) +
//                " " + table.getTimezone()
//            );
        }
    }



    // * find slot

    public long findSlot(long fromDay, long span) {
        long result;
        Log.d(TAG, "findSlot_11: " + Caltool.asYearMonthDayHourMinuteSecond(fromDay) + ", " + span/Caltool.MINUTE_MILLIS);
        long tillDay = Caltool.moveDays(fromDay, 28);
        loadFindFeed(fromDay, tillDay);

        long day = fromDay;
        while (day <= tillDay) {
            result = checkDay(day, span);
            if (result > 0) {
                Log.d(TAG, "findSlot: " + Caltool.asYearMonthDayHourMinuteSecond(result));
                return result;
            }
            day = Caltool.moveDays(day, 1);
        }
        return 0;
    }


    private long checkDay(long day, long span) {
        if (Caltool.isWeekend(day)) return 0;
        Log.d(TAG, "checkDay_11: " + Caltool.asHourMinuteSecond(day));
        long from = Caltool.getMoment(day, FIND_FROM_HOUR, FIND_FROM_MINUTE);
        long till = Caltool.getMoment(day, FIND_TILL_HOUR, FIND_TILL_MINUTE) - span;
        long duration = till - from;
        Log.d(TAG, "duration: " + duration/Caltool.MINUTE_MILLIS);

        long slot = from;
        while (slot <= till) {
            if (fits(slot, span)) {
                Log.d(TAG, "checkDay_fits: " + Caltool.asHourMinuteSecond(slot));
                return slot;
            } else {
                slot = skipNext();
                Log.d(TAG, "checkDay_skipNext: " + Caltool.asHourMinuteSecond(day));
           }
        }
        return 0;
    }


    private boolean fits(long slot, long span) {
        Log.d(TAG, "fits_11: " + Caltool.asHourMinuteSecond(slot));

        // first, remove the older events
        while (mFindFeed.size() > 0) {
            EventModel event = mFindFeed.peek();
            if (event.getEnd() < slot) {
                Log.d(TAG, "fits_remove_older: " + Caltool.asHourMinuteSecond(event.getEnd()));
                mFindFeed.pop();
            } else {
                break;
            }
        }

        // second, is there enough room?
        EventModel event = mFindFeed.peek();
        if (event == null) return false;
        if (event.getStart() >= slot + span) {
            return true;
        }
        return false;
    }


    private long skipNext() {
        if (mFindFeed.size() == 0) return 0;
        EventModel event = mFindFeed.pop();
        long result = event.getEnd();
        Log.d(TAG, "skipNext: " + Caltool.asHourMinuteSecond(result));
        return result;
    }


    private void loadFindFeed(long fromDay, long tillDay) {
        Log.d(TAG, "takefeed_11: " + Caltool.asYearMonthDayHourMinuteSecond(fromDay)
            + Caltool.asYearMonthDayHourMinuteSecond(tillDay));

        mFindFeed.clear();
        EventTable table = new EventTable(mContext);
        table.query(fromDay, tillDay);

        while (table.moveToNext()) {
            boolean allday = table.isAllday();
            long start = table.getStart();
            long end = table.getEnd();
            String title = table.getTitle();
            int color = table.getColor();
            if (allday) {
                start = Caltool.utcToLocal(start);
                end = Caltool.utcToLocal(end);
            }
            mFindFeed.add(new EventModel(start, end, title, color));
            Log.d(TAG, "takeFeed: " + title +
                " " + allday +
                " " + Caltool.asYearMonthDayHourMinuteSecond(start) +
                " " + Caltool.asYearMonthDayHourMinuteSecond(end)
            );
        }
    }




    // * simulation



    private void simulateFeed(long fromDay, long tillDay) {
        final int COLOR = 0xff000088;
        final long GAP = 1000 * 60 * 11;
        long todayNumber = ItemDepot.get().getToday() / Caltool.DAILY_MILLIS;

        mListFeed.clear();
        long day = fromDay;
        while (day <= tillDay) {
            long dayNumber = day / Caltool.DAILY_MILLIS;
            long mark = dayNumber % 4;
            if (mark == 1) {
                Log.d(TAG, "simulateFeed whole: " + dayNumber);
                mListFeed.add(new EventModel(day, day,
                    "My whole day event " + (dayNumber-todayNumber), COLOR));
            } else if (mark == 3) {
                Log.d(TAG, "simulateFeed event: " + dayNumber);
                mListFeed.add(new EventModel(day + 1 * GAP, day + 2 * GAP, "my event 1 " + (dayNumber-todayNumber), COLOR));
//                mListFeed.add(new EventModel(day + 2 * GAP, day + 3 * GAP, "my event 2 " + (dayNumber-todayNumber), COLOR));
//                mListFeed.add(new EventModel(day + 3 * GAP, day + 4 * GAP, "my event 3 " + (dayNumber-todayNumber), COLOR));
//                mListFeed.add(new EventModel(day + 4 * GAP, day + 5 * GAP, "my event 4 " + (dayNumber-todayNumber), COLOR));
//                mListFeed.add(new EventModel(day + 5 * GAP, day + 6 * GAP, "my event 5 " + (dayNumber-todayNumber), COLOR));
            }
            day = Caltool.moveDays(day, 1);
        }
    }


}
