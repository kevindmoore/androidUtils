package com.mastertechsoftware.util.assets;

import android.content.Context;
import android.content.res.AssetManager;

import com.mastertechsoftware.util.log.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
/**
 * Utility class for Handling Assets
 */
public class AssetUtils {

	public static String readAssetFile(Context context, String fileName) {
		AssetManager assets = context.getAssets();
		BufferedReader reader = null;
		StringBuilder stringBuilder = new StringBuilder();
		try {
			reader = new BufferedReader(
				new InputStreamReader(assets.open(fileName)));

			// do reading, usually loop until end of file reading
			String mLine = reader.readLine();
			while (mLine != null) {
				//process line
				stringBuilder.append(mLine);
				mLine = reader.readLine();
			}
		} catch (IOException e) {
			Logger.error("Problems reading Asset", e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					//log the exception
				}
			}
		}
		return stringBuilder.toString();
	}
}
