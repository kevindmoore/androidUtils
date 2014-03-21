package com.mastertechsoftware.util.log;

import android.os.Environment;
import android.util.Log;

import com.mastertechsoftware.util.StackTraceOutput;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Date: Jun 21, 2010
 */
public class SDLogger {
    public static final String LOG_DIRECTORY = "com.mastertechsoftware/";
    private static File sdFile;
    private static String directory;
    private static String logFile = "Log.txt";
    private static boolean logFileSet = false;
    protected static SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm", Locale.US);
	protected static int error_file_size = -1; // Unlimited
	protected static int application_log_lines = -1; // Unlimited
	protected static int max_log_lines = -1; // Unlimited
	private static String application_filter = "com\\.mastertechsoftware.*";
	private static String system_filter = "(com\\.android.*)|(android\\..*)|(org\\.apache\\..*)|(java\\..*)";

    static {
        directory = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (!directory.endsWith("/")) {
            directory += "/";
        }
        directory += LOG_DIRECTORY;
    }

    /**
	 * Write the stack trace and msg
	 * @param msg
	 * @param e
	 */
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
                    String localizedMessage = e.getLocalizedMessage();
                    String name = e.getClass().getName();
                    if (localizedMessage != null) {
                        writer.write(localizedMessage + ":" + name + "\n");
                    } else {
                        writer.write(name + "\n");
                    }
                    if (application_log_lines != -1 || max_log_lines != -1) {
                        StackTraceElement[] stackTraces = e.getStackTrace();
                        int appLineCount = 0;
                        for (int i = 0; i < stackTraces.length; i++) {
                            String trace = stackTraces[i].toString();
                            if (trace.startsWith("Caused by")) {
                                writer.write(trace + "\n");
                            } else if (application_log_lines != -1 && trace.matches(application_filter) && appLineCount < application_log_lines) {
                                writer.write("\tat " + trace + "\n");
                                appLineCount++;
                            } else if ((max_log_lines != -1 && i >= max_log_lines)) {
                                break;
                            } else {
                                writer.write("\tat " + trace + "\n");
                            }
                        }
					} else {
                    	writer.write(StackTraceOutput.getStackTrace(e));
					}
                }
                writer.close();

            } catch (IOException e1) {
                Log.e("SDLogger", "Problems Writing Error Log", e1);
            }
        }
    }

	/**
	 * Write the stack trace and msg only to the given level
	 * @param msg
	 * @param e
	 * @param level
	 */
    public static void error(String msg, Throwable e, int level) {
        initFile();
        if (sdFile != null ) {
            try {
                FileWriter writer = new FileWriter(sdFile, true);
                writer.write(dateFormat.format(new Date()) + "\n");
                if (msg != null && msg.length() > 0) {
                    writer.write(msg + "\n");
                }
                if (e != null) {
                    String localizedMessage = e.getLocalizedMessage();
                    String name = e.getClass().getName();
                    if (localizedMessage != null) {
                        writer.write(localizedMessage + ":" + name + "\n");
                    } else {
                        writer.write(name + "\n");
                    }
                    writer.write(StackTraceOutput.getStackTrace(e, level));
                }
                writer.close();

            } catch (IOException e1) {
                Log.e("SDLogger", "Problems Writing Error Log", e1);
            }
        }
    }

	/**
	 * Log a message to the file
	 * @param msg
	 */
    public static void log(String msg) {
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
                Log.e("SDLogger", "Problems Writing Error Log", e1);
            }
        }
    }

	/**
	 * Initial the sd file
	 */
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
					boolean created = sdFile.createNewFile();
					if (!created) {
						Log.e("SDLogger", "Could not create  " + sdFile.getAbsolutePath());
					}
				} catch (IOException e) {
                    Log.e("SDLogger", "Could not create file " + sdFile.getAbsolutePath());
                }
            }
        } else if (error_file_size != -1) {
			if (sdFile.length() > error_file_size) {
				boolean deleted = sdFile.delete();
				if (!deleted) {
					Log.e("SDLogger", "Could not delete  " + sdFile.getAbsolutePath());
					return;
				}
				try {
					boolean created = sdFile.createNewFile();
					if (!created) {
						Log.e("SDLogger", "Could not create  " + sdFile.getAbsolutePath());
					}
				} catch (IOException e) {
					Log.e("SDLogger", "Could not create file " + sdFile.getAbsolutePath());
				}
			}
		}
    }

	public static void setSDFileSize(int file_size) {
		SDLogger.error_file_size = file_size;
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

	public static void setApplicationLogLines(int application_log_lines) {
		SDLogger.application_log_lines = application_log_lines;
	}

	public static void setMaxLogLines(int system_log_lines) {
		SDLogger.max_log_lines = system_log_lines;
	}

	public static void setApplicationFilter(String application_filter) {
		SDLogger.application_filter = application_filter;
	}

	public static void setSystemFilter(String system_filter) {
		SDLogger.system_filter = system_filter;
	}
}
