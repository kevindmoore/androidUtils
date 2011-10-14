package com.mastertechsoftware.util.log;

import android.util.Log;
import com.mastertechsoftware.util.StackTraceOutput;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Date: Jun 21, 2010
 */
public class SDLogger {
    private static File sdFile;
    private static String directory = "/sdcard/com.mastertechsoftware/";
    private static String logFile = "Log.txt";
    private static boolean logFileSet = false;
    protected static SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm");

    public static void error(String msg, Throwable e) {
        initFile();
        if (sdFile != null ) {
            try {
                FileWriter writer = new FileWriter(sdFile, true);
                writer.write(dateFormat.format(new Date()) + "\n");
                if (msg != null && msg.length() > 0) {
                    writer.write(msg + "\n");
                }
                if (e != null) {
                    writer.write(StackTraceOutput.getStackTrace(e));
                }
                writer.close();

            } catch (IOException e1) {
                Log.e("SDLogger", "Problems Writting Error Log", e1);
            }
        }
    }

    private static void initFile() {
        if (sdFile == null) {
            sdFile = new File(directory + logFile);
            if (!sdFile.exists()) {
                java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(directory, "/");
                String path = "";
                while (tokenizer.hasMoreTokens()) {
                    path += "/" + tokenizer.nextToken();
                    File dir = new File(path);
                    if (!dir.exists() && !dir.mkdir()) {
                        Log.e("SDLogger", "Could not create directory " + dir.getAbsolutePath());
                        return;
                    }
                }
                try {
                    sdFile.createNewFile();
                } catch (IOException e) {
                    Log.e("SDLogger", "Could not create file " + sdFile.getAbsolutePath());
                }
            }
        }
    }

	public static void setDirectory(String directory) {
		SDLogger.directory = directory;
	}

	public static void setLogFile(String logFile) {
		SDLogger.logFile = logFile;
		logFileSet = true;
	}

	public static boolean isLogFileSet() {
		return logFileSet;
	}
}
