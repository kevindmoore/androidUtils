package com.mastertechsoftware.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
/**
 * Utilities for creating/showing dialog
 */
public class DialogUtils {

	public static void showOKDialog(Context context, String title, String message, String okButton) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title).setPositiveButton(okButton, null).setMessage(message).show();
	}

	public static void showOKCancelDialog(Context context, String title, String message, String okButton, String cancelButton, DialogInterface.OnClickListener okClickListener, DialogInterface.OnClickListener cancelListener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title).setPositiveButton(okButton, okClickListener).setNegativeButton(cancelButton, cancelListener).setMessage(
			message).show();
	}

	public static void showThreeButtonDialog(Context context, String title, String message, DialogInterface.OnClickListener clickListener, String[] items) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title).setMessage(message).setPositiveButton(items[0], clickListener).setNegativeButton(items[1], clickListener).
			setNeutralButton(items[2], clickListener).show();
	}

	public static AlertDialog createViewDialog(Context context, String title, String message, View view) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title).setView(view).setMessage(
			message);
		return builder.create();
	}
}
