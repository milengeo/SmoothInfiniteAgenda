package com.mg.agenda.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.mg.agenda.R;

@SuppressWarnings("deprecation")


public class OkayDialog extends DialogFragment {

    private static boolean mShown;
	private static String mTitle, mMessage;




	public static void show(FragmentManager fragman, String title, String message) {
	    if (mShown) return;
		mTitle = title;
		mMessage = message;
		new OkayDialog().show(fragman, "OkayDialog");
	}



	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    mShown = true;
		View dialogLayout = inflater.inflate(R.layout.okay_dialog, container, false);
		final Dialog dialog = getDialog();
		dialog.setCanceledOnTouchOutside(false);

		TextView tvTitle = dialogLayout.findViewById(R.id.okay_dlg_title);
		tvTitle.setText(mTitle);

		TextView tvMessage = dialogLayout.findViewById(R.id.okay_dlg_message);
		tvMessage.setText(mMessage);

		Button okButton = dialogLayout.findViewById(R.id.okay_dlg_button);
		okButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v)	{
						dialog.dismiss();
					}
				}
		);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

		return dialogLayout;
	}


	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		mShown = false;
	}


}
