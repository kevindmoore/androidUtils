package com.mastertechsoftware.mvp.presenter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.mastertechsoftware.AndroidUtil.R;
import com.mastertechsoftware.activity.ActivityUtils;
/**
 * Utilities for Views & Presenters
 */
public class PresenterUtils {
	public interface DialogCallback {
		void finished(String value);
	}


	public static FrameLayout.LayoutParams getFrameLayout() {
		return new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
	}


	public static void showEditDialog(final Activity context, String title, String label, final DialogCallback callback) {
		LayoutInflater inflater=LayoutInflater.from(context);
		final View addView=inflater.inflate(R.layout.text_dialog, null);
		TextView textView = (TextView) addView.findViewById(R.id.dialog_label);
		textView.setText(label);

		// Bring up search dialog for searching podcast sites
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder
			.setTitle(title)
			.setView(addView)
			.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					TextView textView = (TextView) addView.findViewById(R.id.dialog_result);
					CharSequence value = textView.getText();
					if (value == null || value.length() == 0) {
						return;
					}
					String result = value.toString().trim();
					if (callback != null) {
						callback.finished(result);
					}
				}
			})
			.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// ignore, just dismiss
				}
			});
		AlertDialog alertDialog = builder.create();
		alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				ActivityUtils.showKeyboard(context, addView.findViewById(R.id.dialog_result));
			}
		});
		alertDialog.show();
	}

}
