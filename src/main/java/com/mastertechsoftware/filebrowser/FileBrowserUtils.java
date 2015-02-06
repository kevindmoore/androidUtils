package com.mastertechsoftware.filebrowser;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;
/**
 * File Browser Utils to use when not using the ugly file browser we have.
 */
public class FileBrowserUtils {

	public static void openFile(Activity context, String minmeType, int resultCode) {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType(minmeType);
		intent.addCategory(Intent.CATEGORY_OPENABLE);

		// special intent for Samsung file manager
		Intent sIntent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
		// if you want any file type, you can skip next line
		sIntent.putExtra("CONTENT_TYPE", minmeType);
		sIntent.addCategory(Intent.CATEGORY_DEFAULT);

		Intent chooserIntent;
		if (context.getPackageManager().resolveActivity(sIntent, 0) != null) {
			// it is device with samsung file manager
			chooserIntent = Intent.createChooser(sIntent, "Open file");
			chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{intent});
		} else {
			chooserIntent = Intent.createChooser(intent, "Open file");
		}

		try {
			context.startActivityForResult(chooserIntent, resultCode);
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(context.getApplicationContext(), "No suitable File Manager was found.", Toast.LENGTH_SHORT).show();
		}
	}

	public static void openGoogleDrive(Activity context, String minmeType, int resultCode) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ) {
			// ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file browser.
			Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

			// Filter to only show results that can be "opened", such as a file (as opposed to a list
			// of contacts or timezones)
			intent.addCategory(Intent.CATEGORY_OPENABLE);

			// Filter to show only images, using the image MIME data type.
			// If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
			// To search for all documents available via installed storage providers, it would be
			// "*/*".
			intent.setType(minmeType);

			try {
				context.startActivityForResult(intent, resultCode);
			} catch (android.content.ActivityNotFoundException ex) {
				Toast.makeText(context.getApplicationContext(), "No suitable File Manager was found.", Toast.LENGTH_SHORT).show();
			}
		}
	}
}
