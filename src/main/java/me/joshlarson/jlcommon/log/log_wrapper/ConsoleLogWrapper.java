package me.joshlarson.jlcommon.log.log_wrapper;

import me.joshlarson.jlcommon.log.Log;
import me.joshlarson.jlcommon.log.Log.LogLevel;
import me.joshlarson.jlcommon.log.LogWrapper;

import javax.annotation.Nonnull;

public class ConsoleLogWrapper implements LogWrapper {
	
	private final Log.LogLevel level;
	
	/**
	 * Creates a ConsoleLogWrapper with a LogLevel of VERBOSE
	 */
	public ConsoleLogWrapper() {
		this(LogLevel.VERBOSE);
	}
	
	/**
	 * Creates a ConsoleLogWrapper with a custom LogLevel
	 *
	 * @param level the minimum LogLevel to print
	 */
	public ConsoleLogWrapper(@Nonnull Log.LogLevel level) {
		this.level = level;
	}
	
	@Override
	public void onLog(@Nonnull Log.LogLevel level, @Nonnull String str) {
		if (this.level.compareTo(level) > 0)
			return;
		if (level.compareTo(Log.LogLevel.WARN) >= 0)
			System.err.println(str);
		else
			System.out.println(str);
	}
	
}
