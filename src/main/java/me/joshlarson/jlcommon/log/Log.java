/***********************************************************************************
 * MIT License                                                                     *
 *                                                                                 *
 * Copyright (c) 2018 Josh Larson                                                  *
 *                                                                                 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy    *
 * of this software and associated documentation files (the "Software"), to deal   *
 * in the Software without restriction, including without limitation the rights    *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell       *
 * copies of the Software, and to permit persons to whom the Software is           *
 * furnished to do so, subject to the following conditions:                        *
 *                                                                                 *
 * The above copyright notice and this permission notice shall be included in all  *
 * copies or substantial portions of the Software.                                 *
 *                                                                                 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR      *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,        *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE     *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER          *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,   *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE   *
 * SOFTWARE.                                                                       *
 ***********************************************************************************/
package me.joshlarson.jlcommon.log;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Log {
	
	private static Log INSTANCE = null;
	
	private final List<LogWrapper> wrappers;
	private final DateTimeFormatter timeFormat;
	
	private Log() {
		this.wrappers = new ArrayList<>();
		this.timeFormat = DateTimeFormatter.ofPattern("dd-MM-yy HH:mm:ss.SSS").withZone(ZoneId.systemDefault());
	}
	
	private void logAddWrapper(@Nonnull LogWrapper wrapper) {
		wrappers.add(wrapper);
	}
	
	private void logClearWrappers() {
		wrappers.clear();
	}
	
	private void logImplementation(@Nonnull LogLevel level, @Nonnull String str, Object... args) {
		String date = timeFormat.format(Instant.now());
		String logStr;
		if (args.length == 0)
			logStr = date + ' ' + level.getChar() + ": " + str;
		else
			logStr = date + ' ' + level.getChar() + ": " + String.format(str, args);
		for (LogWrapper wrapper : wrappers) {
			wrapper.onLog(level, logStr);
		}
	}
	
	@Nonnull
	private static synchronized Log getInstance() {
		if (INSTANCE == null)
			INSTANCE = new Log();
		return INSTANCE;
	}
	
	public static void addWrapper(@Nonnull LogWrapper wrapper) {
		getInstance().logAddWrapper(wrapper);
	}
	
	public static void clearWrappers() {
		getInstance().logClearWrappers();
	}
	
	/**
	 * Logs the string to the server log file, formatted to display the log severity, time and message.
	 *
	 * @param level the log level of this message between VERBOSE and ASSERT
	 * @param str   the format string for the log
	 * @param args  the string format arguments, if specified
	 */
	private static void log(@Nonnull LogLevel level, @Nonnull String str, Object... args) {
		synchronized (getInstance()) {
			getInstance().logImplementation(level, str, args);
		}
	}
	
	/**
	 * Logs the string to the server log file, formatted to display the log severity as VERBOSE, as well as the time and message.
	 *
	 * @param message the format string for the log
	 * @param args    the string format arguments, if specified
	 */
	public static void v(@Nonnull String message, Object... args) {
		log(LogLevel.VERBOSE, message, args);
	}
	
	/**
	 * Logs the string to the server log file, formatted to display the log severity as DEBUG, as well as the time and message.
	 *
	 * @param message the format string for the log
	 * @param args    the string format arguments, if specified
	 */
	public static void d(@Nonnull String message, Object... args) {
		log(LogLevel.DEBUG, message, args);
	}
	
	/**
	 * Logs the string to the server log file, formatted to display the log severity as INFO, as well as the time and message.
	 *
	 * @param message the format string for the log
	 * @param args    the string format arguments, if specified
	 */
	public static void i(@Nonnull String message, Object... args) {
		log(LogLevel.INFO, message, args);
	}
	
	/**
	 * Logs the string to the server log file, formatted to display the log severity as WARN, as well as the time and message.
	 *
	 * @param message the format string for the log
	 * @param args    the string format arguments, if specified
	 */
	public static void w(@Nonnull String message, Object... args) {
		log(LogLevel.WARN, message, args);
	}
	
	/**
	 * Logs the exception to the server log file, formatted to display the log severity as WARN, as well as the time, and tag.
	 *
	 * @param exception the exception to print
	 */
	public static void w(@Nonnull Throwable exception) {
		printException(LogLevel.WARN, exception);
	}
	
	/**
	 * Logs the string to the server log file, formatted to display the log severity as ERROR, as well as the time and message.
	 *
	 * @param message the format string for the log
	 * @param args    the string format arguments, if specified
	 */
	public static void e(@Nonnull String message, Object... args) {
		log(LogLevel.ERROR, message, args);
	}
	
	/**
	 * Logs the exception to the server log file, formatted to display the log severity as ERROR, as well as the time, and tag.
	 *
	 * @param exception the exception to print
	 */
	public static void e(@Nonnull Throwable exception) {
		printException(LogLevel.ERROR, exception);
	}
	
	/**
	 * Logs the string to the server log file, formatted to display the log severity as ASSERT, as well as the time and message.
	 *
	 * @param message the format string for the log
	 * @param args    the string format arguments, if specified
	 */
	public static void a(@Nonnull String message, Object... args) {
		log(LogLevel.ASSERT, message, args);
	}
	
	/**
	 * Logs the exception to the server log file, formatted to display the log severity as ASSERT, as well as the time, and tag.
	 *
	 * @param exception the exception to print
	 */
	public static void a(@Nonnull Throwable exception) {
		printException(LogLevel.ASSERT, exception);
	}
	
	private static void printException(@Nonnull LogLevel level, @Nonnull Throwable exception) {
		synchronized (getInstance()) {
			printException(level, exception, 0);
		}
	}
	
	private static void printException(@Nonnull LogLevel level, @Nonnull Throwable exception, int depth) {
		Log instance = getInstance();
		String depthString = createExceptionDepthString(depth);
		String header1 = String.format("Exception in thread \"%s\" %s: %s", Thread.currentThread().getName(), exception.getClass().getName(), exception.getMessage());
		String header2 = String.format("Caused by: %s: %s", exception.getClass().getCanonicalName(), exception.getMessage());
		StackTraceElement[] elements = exception.getStackTrace();
		instance.logImplementation(level, depthString + header1);
		instance.logImplementation(level, depthString + header2);
		for (StackTraceElement e : elements) {
			instance.logImplementation(level, depthString + "    " + e.toString());
		}
		if (exception.getCause() != null)
			printException(level, exception.getCause(), depth + 1);
	}
	
	private static String createExceptionDepthString(int depth) {
		StringBuilder str = new StringBuilder(depth * 2);
		for (int i = 0; i < depth; i++)
			str.append("  ");
		return str.toString();
	}
	
	public enum LogLevel {
		VERBOSE('V'),
		DEBUG('D'),
		INFO('I'),
		WARN('W'),
		ERROR('E'),
		ASSERT('A');
		
		private final char c;
		
		LogLevel(char c) {
			this.c = c;
		}
		
		public char getChar() { return c; }
	}
	
}
