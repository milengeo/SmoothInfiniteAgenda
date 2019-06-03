package com.mg.agenda.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mg.agenda.R;
import com.mg.agenda.engine.Calman;
import com.mg.agenda.engine.ItemDepot;
import com.mg.agenda.engine.Caltool;
import com.mg.agenda.model.EventModel;
import com.mg.agenda.model.ItemModel;

import static com.mg.agenda.model.ItemModel.EVENT_TYPE;
import static com.mg.agenda.model.ItemModel.GROUP_TYPE;


public class AgendaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {



    private static final String TAG = AgendaAdapter.class.getSimpleName();
    private static final int ENOGH_ITEMS = 9;
    private LinearLayoutManager mLayoutManager;
    private String mAllStr, mDayStr;


    public AgendaAdapter(Context context, LinearLayoutManager layoutManager) {
        mLayoutManager = layoutManager;
        mAllStr = context.getString(R.string.all);
        mDayStr = context.getString(R.string.day);
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Log.d(TAG, "onCreateViewHolder_11: " + viewType);
        //long tick = SystemClock.elapsedRealtime();
        RecyclerView.ViewHolder result;
        switch (viewType) {
            case GROUP_TYPE:
                result = new GroupViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.group_item, parent, false));
                break;
            case EVENT_TYPE:
                result =  new EventViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.event_item, parent, false));
                break;
            default:
                result = new LabelViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.label_item, parent, false));
        }
        //tick = SystemClock.elapsedRealtime() - tick;
        //Log.d(TAG, "  onCreateViewHolder_99   = " + tick);
        return result;
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        //Log.d(TAG, "onBindViewHolder_11: " + (position-ItemDepot.PIVOT));
        //long tick = SystemClock.elapsedRealtime();
        if (holder instanceof GroupViewHolder) {
            ((GroupViewHolder) holder).bindData(position);
        } else if (holder instanceof EventViewHolder) {
            ((EventViewHolder) holder).bindData(position);
        } else if (holder instanceof LabelViewHolder) {
            ((LabelViewHolder) holder).bindData(position);
        }
        checkRoom(position);
        //tick = SystemClock.elapsedRealtime() - tick;
        //Log.d(TAG, "  onBindViewHolder_99   = " + tick);
    }


    @Override
    public int getItemViewType(int position) {
        return ItemDepot.get().getViewType(position);
    }


    @Override
    public int getItemCount() {
        int result = ItemDepot.get().getCount();
        //Log.d(TAG, "getItemCount: " + result);
        return result;
    }




    class LabelViewHolder extends RecyclerView.ViewHolder {
        private TextView mTextView;

        LabelViewHolder(View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.list_label_text);
        }

        void bindData(int position) {
            mTextView.setText(ItemDepot.get().getItemText(position));
            //Log.d(TAG, "LabelBind: " + position);
        }
    }


    class GroupViewHolder extends RecyclerView.ViewHolder {
        private TextView mLeftText, mRightText;

        GroupViewHolder(View itemView) {
            super(itemView);
            mLeftText = itemView.findViewById(R.id.list_group_text);
            mRightText = itemView.findViewById(R.id.list_group_right_text);
        }

        void bindData(int position) {
            setGroupText(position, mLeftText, mRightText);
            mLeftText.setText(ItemDepot.get().getItemText(position));
            //Log.d(TAG, "GroupBind: " + position);
        }
    }


    private void setGroupText(int position, TextView leftText, TextView rightText) {
        if (leftText != null)
            leftText.setText(ItemDepot.get().getItemText(position));
        if (rightText != null)
            rightText.setText(ItemDepot.get().getItemRightText(position));
    }


    class EventViewHolder extends RecyclerView.ViewHolder {
        private TextView mStartView;
        private TextView mEndView;
        private TextView mTextView;
        private View mBarView;

        EventViewHolder(View itemView) {
            super(itemView);
            mStartView = itemView.findViewById(R.id.list_event_start);
            mEndView = itemView.findViewById(R.id.list_event_end);
            mTextView = itemView.findViewById(R.id.list_event_text);
            mBarView = itemView.findViewById(R.id.list_event_bar);
        }

        void bindData(int position) {
            EventModel event = ItemDepot.get().getItem(position).asEvent();
            if (event == null) return;

            if (event.isAllday()) {
                mStartView.setText(mAllStr);
                mEndView.setText(mDayStr);
            } else {
                mStartView.setText(Caltool.asHourMinute(event.getStart()));
                mEndView.setText(Caltool.asHourMinute(event.getEnd()));
            }

            mTextView.setText(event.getTitle());
            mBarView.setBackgroundColor(event.getColor());
        }
    }


    /**
     * Check for enough available items
     */
    private void checkRoom(int position) {
        //Log.d(TAG, "checkRoom_position: " + position);
        int index, count, room=ItemDepot.AMPLE_ITEMS*100;
        if (position >= ItemDepot.PIVOT) {
            // future side
            index = position - ItemDepot.PIVOT;
            //Log.d(TAG, "checkRoom_future index: " + index);
            count = ItemDepot.get().getNewerCount();
            //Log.d(TAG, "checkRoom_future count: " + count);
            room = count - index;
            //Log.d(TAG, "checkRoom new room: " + room);
            if (room < ItemDepot.AMPLE_ITEMS) {
                Log.d(TAG, "checkRoom, not enough NEW: " + room);
                Calman.get().query(ItemDepot.QUERY_ITEMS, 0);
            }
        } else {
            // past side
            index = ItemDepot.PIVOT - position - 1;
            //Log.d(TAG, "checkRoom_past index: " + index);
            count = ItemDepot.get().getOlderCount();
            //Log.d(TAG, "checkRoom_past count: " + count);
            room = count - index;
            //Log.d(TAG, "checkRoom old room: " + room);
            if (room < ItemDepot.AMPLE_ITEMS) {
                Log.d(TAG, "checkRoom, not enough OLD: " + room);
                Calman.get().query(0, ItemDepot.QUERY_ITEMS);
            }
        }
    }



    // * sticky header


    public int getGroupPositionForItem(int itemPosition) {
        //Log.d(TAG, "getGroupPositionForItem_11: " + itemPosition);
        final int COUNT = 1000;
        int groupPosition = 0;
        int index = itemPosition;
        for (int i=0; i<COUNT; i--) {
            if (index < 0) break;
            ItemModel item = ItemDepot.get().getItem(index);
            if (item == null) break;
            if (item.getType() == ItemModel.GROUP_TYPE) {
                groupPosition = index;
                break;
            }
            index--;
        }
        //Log.d(TAG, "getGroupPositionForItem_99: " + groupPosition);
//        ItemModel item = ItemDepot.get().getItem(groupPosition);
//        if (item != null)
//            Log.d(TAG, "getGroupPositionForItem: " + item.getType());
        return groupPosition;
    }


    public void bindHeaderData(View headerView, int position) {
        //Log.d(TAG, "bindHeaderData_11: " + position);
        ItemModel item = ItemDepot.get().getItem(position);
        if (item == null) return;
        if (item.getType() != ItemModel.GROUP_TYPE) return;

        TextView leftText = headerView.findViewById(R.id.list_group_text);
        TextView rightText = headerView.findViewById(R.id.list_group_right_text);
        setGroupText(position, leftText, rightText);
    }


    // whether the item represents a group
    public boolean isGroup(int position) {
        ///Log.d(TAG, "isGroup_11: " + position);
        if (ItemDepot.get().getViewType(position) == GROUP_TYPE)
            return true;
        else
            return false;
    }



    // * helpers

    public void scrollToday() {
        mLayoutManager.scrollToPositionWithOffset(ItemDepot.PIVOT, 0);
        notifyDataSetChanged();
    }

}
