package com.mg.agenda.model;


public class ItemModel {

    public static final int LABEL_TYPE = 0;
    public static final int GROUP_TYPE = 1;
    public static final int EVENT_TYPE = 2;


    private int mType = LABEL_TYPE;
    private Object mData;
    private String mLabel = "";


    public static ItemModel addLabel(String label) {
        ItemModel item = new ItemModel();
        item.mLabel = label;
        return item;
    }


    public static ItemModel addGroup(long start) {
        ItemModel item = new ItemModel();
        item.mType = GROUP_TYPE;
        item.mData = new GroupModel(start);
        return item;
    }


    public static ItemModel addEvent(EventModel event) {
        ItemModel item = new ItemModel();
        item.mType = EVENT_TYPE;
        item.mData = event;
        return item;
    }


//    public static ItemModel addEvent(long start, long end, String title, int color) {
//        ItemModel item = new ItemModel();
//        item.mType = EVENT_TYPE;
//        item.mData = new EventModel(start, end, title, color);
//        return item;
//    }


    public String getText() {
        if (mType == GROUP_TYPE)
            return ((GroupModel) mData).getText();
        else if (mType == EVENT_TYPE)
            return ((EventModel) mData).getText();
        else
            return mLabel;
    }


    public int getType() {
        return mType;
    }


    public GroupModel asGroup() {
        if (mData instanceof GroupModel)
            return (GroupModel) mData;
        else
            return null;
    }


    public EventModel asEvent() {
        if (mData instanceof EventModel)
            return (EventModel) mData;
        else
            return null;
    }

}
