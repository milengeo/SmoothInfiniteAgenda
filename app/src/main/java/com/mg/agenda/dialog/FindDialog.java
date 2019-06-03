package com.mg.agenda.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.mg.agenda.R;
import com.mg.agenda.activity.MainActivity;
import com.mg.agenda.engine.Caltool;
import com.mg.agenda.engine.ItemDepot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class FindDialog extends DialogFragment {


    private static final String TAG = FindDialog.class.getSimpleName();
    private static boolean mShown;
    private List<String> mFromLabels = new ArrayList<>();
    private List<Long> mFromValues = new ArrayList<>();
    private Spinner mFromSpinner;

    private List<String> mSpanLabels = Arrays.asList("15 minutes", "half an hour", "one hour", "two hours");
    private List<Long> mSpanValues = Arrays.asList(15*Caltool.MINUTE_MILLIS, 30*Caltool.MINUTE_MILLIS,
            60*Caltool.MINUTE_MILLIS, 120*Caltool.MINUTE_MILLIS);
    private Spinner mSpanSpinner;



    public static void show(FragmentManager fragman) {
        if (mShown) return;
        new FindDialog().show(fragman, "FindDialog");
    }


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mShown = true;
		View dialogLayout = inflater.inflate(R.layout.find_dialog, container, false);
		final Dialog dialog = getDialog();
		dialog.setCanceledOnTouchOutside(false);

        mFromSpinner = dialogLayout.findViewById(R.id.spinner_from);
        mSpanSpinner = dialogLayout.findViewById(R.id.spinner_span);
        setupFromSpinner();
        setupSpanSpinner();

		final Button cancelButton = dialogLayout.findViewById(R.id.find_dlg_cancel);
		final Button okayButton = dialogLayout.findViewById(R.id.find_dlg_okay);

		cancelButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				}
		);

		okayButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
					    long fromDay = ItemDepot.get().getToday();
					    int fromIndex = mFromSpinner.getSelectedItemPosition();
					    if (fromIndex >= 0 && fromIndex < mFromValues.size())
					        fromDay = mFromValues.get(fromIndex);

					    long duration = 60;
					    int duraIndex = mSpanSpinner.getSelectedItemPosition();
					    if (duraIndex >= 0 && duraIndex < mSpanValues.size())
					        duration = mSpanValues.get(duraIndex);

						dialog.dismiss();
                        ((MainActivity) getActivity()).onFindDlgCallback(fromDay, duration);
					}
				}
		);

		return dialogLayout;
	}


    @Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
        mShown = false;
	}


	private void setupFromSpinner() {
        mFromLabels.clear();
        long day = ItemDepot.get().getToday();

        int count = 0;
        while (count < 5) {
            if (!Caltool.isWeekend(day)) {
                count++;
                mFromLabels.add(Caltool.asMonthDay(day));
                mFromValues.add(day);
            }
            day = Caltool.moveDays(day, 1);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_view_item, mFromLabels);
        adapter.setDropDownViewResource(R.layout.spinner_drop_item);
        mFromSpinner.setAdapter(adapter);
        mFromSpinner.setSelection(0);
    }


    private void setupSpanSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_view_item, mSpanLabels);
        adapter.setDropDownViewResource(R.layout.spinner_drop_item);
        mSpanSpinner.setAdapter(adapter);
        mSpanSpinner.setSelection(2);
    }

}
