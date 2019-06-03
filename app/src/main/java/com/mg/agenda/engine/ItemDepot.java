package com.mg.agenda.engine;


import android.content.Context;

import com.mg.agenda.R;
import com.mg.agenda.model.EventModel;
import com.mg.agenda.model.GroupModel;
import com.mg.agenda.model.ItemModel;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

/**
 * This class handles all available event items.
 */

public class ItemDepot {

    private static final String TAG = ItemDepot.class.getSimpleName();

    public static final int RANGE = Integer.MAX_VALUE;
    public static final int PIVOT = RANGE / 2;

    public static final int AMPLE_ITEMS = 33;
    public static final int QUERY_ITEMS = AMPLE_ITEMS * 2;

    private static final ItemDepot sInstance = new ItemDepot();
    private long mToday, mThisYear;  // the pivot day
    private long mOldestDay, mNewestDay;
    private List<ItemModel> mNewList = new Vector<ItemModel>();
    private List<ItemModel> mOldList = new Vector<ItemModel>();
    private Deque<ItemModel> mStack = new LinkedList<>();
    private boolean mOlderUpdate;
    private String mLoadingStr="", mYesterdayStr="", mTodayStr="", mTomorrowStr="";


    public static ItemDepot get() {
        return sInstance;
    }


    private ItemDepot() {
    }


    /**
     * One time setup, at the time of application creation
     * @param context
     */
    public void initialize(Context context) {
        reset();
        mLoadingStr = context.getString(R.string.loading);
        mYesterdayStr = context.getString(R.string.yesterday);
        mTodayStr = context.getString(R.string.today);
        mTomorrowStr = context.getString(R.string.tomorrow);
    }


    public void reset() {
        mNewList.clear();
        mOldList.clear();
        mToday = Caltool.getDayStart(Caltool.now());
        mThisYear = Caltool.getYear(Caltool.now());
        mNewestDay = mToday;
        mOldestDay = Caltool.moveDays(mToday, -1);
    }


    public long getToday() {
        return mToday;
    }


    public long getThisYear() {
        return mThisYear;
    }


    public long getNewestDay() {
        return mNewestDay;
    }


    public long getOldestDay() {
        return mOldestDay;
    }



    public void startDayUpdate(long day) {
        if (day >= mToday) {
            mOlderUpdate = false;
        } else {
            mOlderUpdate = true;
            mStack.clear();
        }
    }


    public void finishDayUpdate() {
        if (!mOlderUpdate) {
            mNewestDay = Caltool.moveDays(mNewestDay, 1);
        } else {
            mOldestDay = Caltool.moveDays(mOldestDay, -1);
            mOlderUpdate = false;
            while (!mStack.isEmpty()) {
                ItemModel item = mStack.pop();
                mOldList.add(item);
            }
        }
    }

    public boolean isOlderUpdate() {
        return mOlderUpdate;
    }

    public int getNewerCount() {
        return mNewList.size();
    }


    public int getOlderCount() {
        return mOldList.size();
    }


    public int getCount() {
        return RANGE;
    }


    public ItemModel getItem(int position) {
        //Log.d(TAG, "getItem: " + position + " size " + mNewList.size());
        int index;
        if (position >= PIVOT) {
            // newer items
            index = position - PIVOT;
            if (index < mNewList.size())
                return mNewList.get(index);
            else
                return null;
        } else {
            // older items
            index = PIVOT - position - 1;
            if (index < mOldList.size())
                return mOldList.get(index);
            else
                return null;
        }
    }


    public int getViewType(int position) {
        ItemModel item = getItem(position);
        if (item != null) {
            return item.getType();
        } else {
            return ItemModel.LABEL_TYPE;
        }
    }


    public String getItemText(int position) {
        ItemModel item = getItem(position);
        if (item != null) {
            return item.getText();
        } else {
            return mLoadingStr;
        }
    }


    public String getItemRightText(int position) {
        ItemModel item = getItem(position);
        if (item != null  &&  item.getType() == ItemModel.GROUP_TYPE) {
            GroupModel group = item.asGroup();
            if (group != null)
                return group.getRightText();
            else
                return "";
        } else {
            return "";
        }
    }



    public void addLabel(String label) {
        if (!mOlderUpdate) {
            mNewList.add(ItemModel.addLabel(label));
        } else {
            mStack.push(ItemModel.addLabel(label));
        }
    }


    public void addGroup(long moment) {
        if (!mOlderUpdate) {
            mNewList.add(ItemModel.addGroup(moment));
        } else {
            mStack.push(ItemModel.addGroup(moment));
        }
    }


    public void addEvent(EventModel event) {
        if (!mOlderUpdate) {
            mNewList.add(ItemModel.addEvent(event));
        } else {
            mStack.push(ItemModel.addEvent(event));
        }
    }


//    public void addEvent(long start, long end, String title, int color) {
//        if (!mOlderUpdate) {
//            mNewList.add(ItemModel.addEvent(start, end, title, color));
//        } else {
//            mStack.push(ItemModel.addEvent(start, end, title, color));
//        }
//    }


    public String getYesterdayStr() {
        return mYesterdayStr;
    }

    public String getTodayStr() {
        return mTodayStr;
    }

    public String getTomorrowStr() {
        return mTomorrowStr;
    }

}
