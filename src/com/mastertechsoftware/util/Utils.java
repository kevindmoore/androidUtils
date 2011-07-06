package com.mastertechsoftware.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.text.Html;
import android.util.Log;
import android.view.Display;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

	public static boolean isLandscape(Activity activity) {
        Configuration configuration = activity.getResources().getConfiguration();
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return true;
        }
        final Display defaultDisplay = activity.getWindow().getWindowManager().getDefaultDisplay();
		int h = defaultDisplay.getHeight();
		int w = defaultDisplay.getWidth();
		return (w > h);
	}

	public static boolean isLargeScreen(Context context) {
		Configuration configuration = context.getResources().getConfiguration();
		// TODO - Change last size large to XLARGE
		if (((configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) ||
		 ((configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE)) {
			return true;
		}
		return false;
	}
	
	public static boolean isSmallScreen(Context context) {
		Configuration configuration = context.getResources().getConfiguration();
		if ((configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL) {
			return true;
		}
		return false;
	}

	public static boolean isNormalScreen(Context context) {
		Configuration configuration = context.getResources().getConfiguration();
		if ((configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL) {
			return true;
		}
		return false;
	}

	public static boolean isPortrait(Activity activity) {
		return !isLandscape(activity);
	}

    public static String cleanupURL(String url) {
        if (url == null || url.length() == 0) {
            Logger.error("Null or Empty URL");
            return null;
        }
        try {
            url = URLDecoder.decode(url, "UTF-8");
            int index = url.lastIndexOf("http");
            if (index > 0) {
                url = url.substring(index);
            }
        } catch (UnsupportedEncodingException e) {
            Logger.error(e);
        }
        return url;
    }

    public static String cleanupHTML(String html) {
        Pattern htmlPattern = Pattern.compile("&[^;]{1,4};", Pattern.DOTALL);
        Matcher matcher = htmlPattern.matcher(html);
        while (html.length() > 0 && matcher.find()) {
            html = Html.fromHtml(html).toString();
            matcher = htmlPattern.matcher(html);
        }
        return html;
    }

    public static boolean ensurePathExists(String directory) {
        java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(directory, "/");
        String path = "";
        while (tokenizer.hasMoreTokens()) {
            path += "/" + tokenizer.nextToken();
            if (!tokenizer.hasMoreTokens()) {
                return true;
            }
            File dir = new File(path);
            if (!dir.exists() && !dir.mkdir()) {
                Log.e("Utils", "Could not create directory " + dir.getAbsolutePath());
                return false;
            }
        }
		return true;
    }
}
