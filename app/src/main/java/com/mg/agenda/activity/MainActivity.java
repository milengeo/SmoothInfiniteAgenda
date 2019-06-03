
package com.mg.agenda.activity;


import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.mg.agenda.R;
import com.mg.agenda.dialog.FindDialog;
import com.mg.agenda.dialog.OkayDialog;
import com.mg.agenda.dialog.PermissionDialog;
import com.mg.agenda.engine.Calman;
import com.mg.agenda.engine.Caltool;
import com.mg.agenda.engine.ItemDepot;
import com.mg.agenda.widget.AgendaAdapter;
import com.mg.agenda.widget.StickyHeader;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int ACTION_RESULT_RUNTIME_PERMISSION = 98765;

    private static final String[] NEEDED_PERMISSIONS = new String[]{
        Manifest.permission.READ_CALENDAR
    };

    private RecyclerView mEventView;
    private AgendaAdapter mAdapter;
    private Handler mHandler;
    private MenuItem mPermissionMeit, mTodayMeit, mFindMeit;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setContentView(R.layout.main_activity);
        String title = getString(R.string.app_name);
        setTitle(title);
        mHandler = new Handler();

        mEventView = findViewById(R.id.list_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.scrollToPositionWithOffset(ItemDepot.PIVOT, 0);
        mAdapter = new AgendaAdapter(this, layoutManager);

        mEventView.setAdapter(mAdapter);
        mEventView.setLayoutManager(layoutManager);
        mEventView.addItemDecoration(new StickyHeader(mAdapter));

        Calman.get().subscribe(CalmanListener);

        if (havePermissions()) {
            doInitialQuery();
        } else {
            mEventView.setVisibility(View.INVISIBLE);
            askPermissions();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        Calman.get().unsubscribe();
    }


    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mPermissionMeit = menu.findItem(R.id.action_permission);
        mTodayMeit = menu.findItem(R.id.action_today);
        mFindMeit = menu.findItem(R.id.action_find);
        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mPermissionMeit != null)
            mPermissionMeit.setVisible(!Calman.get().isPermitted());
        if (mTodayMeit != null)
            mTodayMeit.setVisible(Calman.get().isPermitted());
        if (mFindMeit != null)
            mFindMeit.setVisible(Calman.get().isPermitted());
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_permission) {
            askPermissions();
            return true;
        } else if (id == R.id.action_today) {
            goToday();
            return true;
        } else if (id == R.id.action_find) {
            doFind();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    // * permissions


    private boolean havePermissions() {
        for (String permission : NEEDED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    private void askPermissions() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        requestPermissions(NEEDED_PERMISSIONS, ACTION_RESULT_RUNTIME_PERMISSION);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        switch (requestCode) {
            case ACTION_RESULT_RUNTIME_PERMISSION: {

                // If request is cancelled, the result arrays are empty.
                boolean granted = true;
                for (int i=0; i<grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        granted = false;
                    }
                }

                if (granted) {
                    Log.d(TAG, "Asked permissions granted");
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            doInitialQuery();
                            invalidateOptionsMenu();
                            mEventView.setVisibility(View.VISIBLE);
                        }
                    });
                } else {
                    // permission denied
                    Log.d(TAG, "Asked permissions denied");
                    PermissionDialog.show(getFragmentManager());
                }
            }
        }
    }




    // * doers


    private void doInitialQuery() {
        Calman.get().setPermitted(true);
        ItemDepot.get().reset();
        Calman.get().query(ItemDepot.QUERY_ITEMS, ItemDepot.QUERY_ITEMS);
    }


    private void goToday() {
        Log.d(TAG, "goToday");
        mAdapter.scrollToday();
    }


    private void doFind() {
        Log.d(TAG, "doFind");
        new FindDialog().show(getFragmentManager());
    }



    // * FindDialog listener
    public void onFindDlgCallback(long from, long span) {
        long fromToday = (from-ItemDepot.get().getToday()) / Caltool.DAILY_MILLIS;
        Log.d(TAG, "onFind_11: " + fromToday + ", " + span);
        long slot = Calman.get().findSlot(from, span);
        if (slot > 0) {
            String slotStr = Caltool.asDowMonthDayHourMinute(slot);
            Log.d(TAG, "onFind slot: " + slotStr);
            OkayDialog.show(getFragmentManager(), "Next free slot", slotStr);
        } else {
            OkayDialog.show(getFragmentManager(), "Sorry!", "Cannot find any slot");
        }
    }



    // * Calman listener
    private Calman.Listener CalmanListener = new Calman.Listener() {
        public void onUpdateList() {
            Log.d(TAG, "Calman.Listener_onUpdateList");
            mAdapter.notifyDataSetChanged();
        }
    };

}