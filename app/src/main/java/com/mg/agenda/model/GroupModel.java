package com.mg.agenda.model;


import com.mg.agenda.engine.ItemDepot;
import com.mg.agenda.engine.Caltool;


public class GroupModel {

    private long mDay;


    public GroupModel(long day) {
        mDay = day;
    }


    public String getText() {
        String result = "";
        if (Caltool.getYear(mDay) == ItemDepot.get().getThisYear())
            result = Caltool.asDowMonthDay(mDay);
        else
            result = Caltool.asDowMonthDayYear(mDay);
        return result;
    }


    public String getRightText() {
        String result = "";
        long today = ItemDepot.get().getToday();
        if (today == mDay)
            result = ItemDepot.get().getTodayStr() ;
        else if (Caltool.moveDays(today, -1) == mDay)
            result = ItemDepot.get().getYesterdayStr();
        else if (Caltool.moveDays(today, 1) == mDay)
            result = ItemDepot.get().getTomorrowStr();
        return result;
    }

}
