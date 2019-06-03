package com.mg.agenda.widget;

import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mg.agenda.R;


public class StickyHeader extends RecyclerView.ItemDecoration {

    private static final String TAG = StickyHeader.class.getSimpleName();
    private AgendaAdapter mAdapter;
    private int mHeaderHeight;


    public StickyHeader(AgendaAdapter adapter) {
        mAdapter = adapter;
    }


    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
        View topChild = parent.getChildAt(0);
        //Log.d(TAG, "onDrawOver_topChild: " + topChild);
        if (topChild == null) {
            return;
        }

        int topChildPosition = parent.getChildAdapterPosition(topChild);
        //Log.d(TAG, "onDrawOver_topChildPosition: " + topChildPosition);
        if (topChildPosition == RecyclerView.NO_POSITION) {
            return;
        }

        int headerPos = mAdapter.getGroupPositionForItem(topChildPosition);
        //Log.d(TAG, "onDrawOver_headerPos: " + topChildPosition);
        View currentHeader = getHeaderViewForItem(headerPos, parent);
        measureLayout(parent, currentHeader);
        int contactPoint = currentHeader.getBottom();
        View childInContact = getChildInContact(parent, contactPoint, headerPos);

        if (childInContact != null && mAdapter.isGroup(parent.getChildAdapterPosition(childInContact))) {
            moveHeader(c, currentHeader, childInContact);
            return;
        }

        drawHeader(c, currentHeader);
    }


    private View getHeaderViewForItem(int headerPosition, RecyclerView parent) {
        View headerView = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_item, parent, false);
        mAdapter.bindHeaderData(headerView, headerPosition);
        return headerView;
    }


    private void drawHeader(Canvas c, View header) {
        c.save();
        c.translate(0,  0);
        header.draw(c);
        c.restore();
    }


    private void moveHeader(Canvas c, View currentHeader, View nextHeader) {
        c.save();
        c.translate(0, nextHeader.getTop() - currentHeader.getHeight());
        currentHeader.draw(c);
        c.restore();
    }


    private View getChildInContact(RecyclerView parent, int contactPoint, int currentHeaderPos) {
        View childInContact = null;
        for (int i = 0; i < parent.getChildCount(); i++) {
            int heightTolerance = 0;
            View child = parent.getChildAt(i);

            //measure height tolerance with child if child is another header
            if (currentHeaderPos != i) {
                boolean isChildHeader = mAdapter.isGroup(parent.getChildAdapterPosition(child));
                if (isChildHeader) {
                    heightTolerance = mHeaderHeight - child.getHeight();
                }
            }

            //add heightTolerance if child top be in display area
            int childBottomPosition;
            if (child.getTop() > 0) {
                childBottomPosition = child.getBottom() + heightTolerance;
            } else {
                childBottomPosition = child.getBottom();
            }

            if (childBottomPosition > contactPoint) {
                if (child.getTop() <= contactPoint) {
                    // This child overlaps the contactPoint
                    childInContact = child;
                    break;
                }
            }
        }
        return childInContact;
    }


    private void measureLayout(ViewGroup parent, View view) {
        // Specs for parent (RecyclerView)
        int widthSpec = View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(parent.getHeight(), View.MeasureSpec.UNSPECIFIED);

        // Specs for children (headers)
        int childWidthSpec = ViewGroup.getChildMeasureSpec(widthSpec, parent.getPaddingLeft() + parent.getPaddingRight(), view.getLayoutParams().width);
        int childHeightSpec = ViewGroup.getChildMeasureSpec(heightSpec, parent.getPaddingTop() + parent.getPaddingBottom(), view.getLayoutParams().height);

        view.measure(childWidthSpec, childHeightSpec);

        view.layout(0, 0, view.getMeasuredWidth(), mHeaderHeight = view.getMeasuredHeight());
    }
}
