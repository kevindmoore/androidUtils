/*
 * @author Kevin Moore
 *
 */
package com.mastertechsoftware.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author Kevin Moore
 */
public class StackTraceOutput {

	/**
	 * Print out the stack trace that contains the package given
	 *
	 * @param msg		   message
	 * @param packagePrefix
	 */
	public static void printStackTrace(String msg, String packagePrefix) {
		System.out.println(msg);
		printStackTrace(packagePrefix);
		System.out.println("");
	}

	/**
	 * Print out the stack trace that contains the package given
	 *
	 * @param packagePrefix
	 */
	public static void printStackTrace(String packagePrefix) {
		Throwable throwable = new Throwable();
		StackTraceElement[] elements = throwable.getStackTrace();
		for (int i = 1; i < elements.length; i++) {
			StackTraceElement element = elements[i];
			String className = element.getClassName();
			if (className.contains("StackTraceOutput"))
				continue;
			if (packagePrefix == null || className.contains(packagePrefix)) {
				System.out.println(element.toString());
			}
		}
	}

	/**
	 * Given a Throwable object, return a string representation of the stack trace
	 *
	 * @param throwable
	 * @return String representation of the stack trace
	 */
	public static String getStackTrace(Throwable throwable) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(stringWriter);
		throwable.printStackTrace(writer);
		writer.close();
		return stringWriter.toString();
	}

	/**
	 * Get a stack trace with the given level. Create our own exception
	 *
	 * @param level
	 * @return String representation of the stack trace
	 */
	public static String getStackTrace(int level) {
		return getStackTrace(new Exception(), level);
	}

	/**
	 * Get a stack trace only level deep
	 *
	 * @param throwable
	 * @param level
	 * @return String representation of the stack trace
	 */
	public static String getStackTrace(Throwable throwable, int level) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(stringWriter);
		StackTraceElement[] stackTrace = throwable.getStackTrace();
		for (int i = 1; i < level && i < stackTrace.length; i++) {
			writer.println(stackTrace[i].toString());
		}
		writer.close();
		return stringWriter.toString();
	}

	/**
	 * Get a stack trace that contains the given filter for the given level
	 * @param throwable
	 * @param filter
	 * @param level
	 * @return String representation of the stack trace
	 */
	public static String getStackTrace(Throwable throwable, String filter, int level) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(stringWriter);
		StackTraceElement[] stackTrace = throwable.getStackTrace();
		int foundCount = 0;
		for (int i = 0; i < stackTrace.length; i++) {
			String trace = stackTrace[i].toString();
			// We always want the Caused by statement to show
			if (trace.startsWith("Caused by")) {
				writer.println(trace);
			} else if (foundCount < level && trace.matches(filter)) {
				writer.println(trace);
				foundCount++;
			}
		}
		writer.close();
		return stringWriter.toString();
	}

	/**
	 * Given a Throwable object, return a string representation of the first line in the stack trace
	 *
	 * @param throwable
	 * @return String representation of the stack trace
	 */
	public static String getFirstLineStackTrace(Throwable throwable) {
		return getStackTrace(throwable, 1);
	}

	/**
	 * Get the first stack trace line
	 *
	 * @return
	 */
	public static String getFirstLineStackTrace() {
		return getFirstLineStackTrace(new Exception());
	}

	/**
	 * Get the current stack trace.
	 *
	 * @return String representation of the stack trace
	 */
	public static String getStackTrace() {
		Throwable throwable = new Throwable();
		return getStackTrace(throwable);
	}
}
