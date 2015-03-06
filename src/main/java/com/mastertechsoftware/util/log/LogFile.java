package com.mastertechsoftware.util.log;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
/**
 * Helper class for writing to a file
 */
public class LogFile {
	public static final String LOG_DIRECTORY = "com.mastertechsoftware/";
	private File sdFile;
	private static String externalDirectory;
	private String directory = "/sdcard/com.mastertechsoftware/";
	private String logFile = "Log.txt";
	protected static SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm");

	static {
		externalDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
		if (!externalDirectory.endsWith("/")) {
			externalDirectory += "/";
		}
		externalDirectory += LOG_DIRECTORY;
	}

	public void setDirectoryName(String directoryName) {
		directory = externalDirectory + directoryName;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public void setLogFile(String logFile) {
		this.logFile = logFile;
	}

	/**
	 * Write the msg
	 * @param msg
	 */
	public void write(String msg) {
		initFile();
		if (sdFile != null ) {
			try {
				FileWriter writer = new FileWriter(sdFile, true);
				writer.write(dateFormat.format(new Date()) + "\n");
				if (msg != null && msg.length() > 0) {
					writer.write(msg + "\n");
				}
				writer.close();

			} catch (IOException e1) {
				Log.e("LogFile", "Problems Writing Log", e1);
			}
		}
	}

	/**
	 * Initial the sd file
	 */
	private void initFile() {
		if (logFile == null) {
			Logger.error("LogFile:initFile. Logfile is null");
			return;
		}
		if (sdFile == null) {
			sdFile = new File(directory + logFile);
			if (!sdFile.exists()) {
				java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(directory, "/");
				String path = "";
				while (tokenizer.hasMoreTokens()) {
					path += "/" + tokenizer.nextToken();
					File dir = new File(path);
					if (!dir.exists() && !dir.mkdir()) {
						Logger.error("Could not create directory " + dir.getAbsolutePath());
						return;
					}
				}
				try {
					boolean created = sdFile.createNewFile();
					if (!created) {
						Logger.error("Could not create  " + sdFile.getAbsolutePath());
					}
				} catch (IOException e) {
					Logger.error("Could not create file " + sdFile.getAbsolutePath());
				}
			}
		}
	}
}
