/***********************************************************************************
 * MIT License                                                                     *
 *                                                                                 *
 * Copyright (c) 2019 Josh Larson                                                  *
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
package me.joshlarson.jlcommon.log.log_wrapper;

import me.joshlarson.jlcommon.log.Log;
import me.joshlarson.jlcommon.log.Log.LogLevel;
import me.joshlarson.jlcommon.log.LogWrapper;
import org.jetbrains.annotations.NotNull;

public class AnsiColorLogWrapper implements LogWrapper {
	
	private static final String TRACE_PREFIX = "\033[0;37m";
	private static final String DATA_PREFIX = "\033[0;34m";
	private static final String INFO_PREFIX = "\033[1;92m";
	private static final String WARN_PREFIX = "\033[0;93m";
	private static final String ERROR_PREFIX = "\033[0;30;103m";
	private static final String ASSERT_PREFIX = "\033[1;4;30;41m";
	
	private final Log.LogLevel level;
	private final String tracePrefix;
	private final String dataPrefix;
	private final String infoPrefix;
	private final String warnPrefix;
	private final String errorPrefix;
	private final String assertPrefix;
	
	/**
	 * Creates an AnsiColorLogWrapper with a LogLevel of TRACE
	 */
	public AnsiColorLogWrapper() {
		this(LogLevel.TRACE);
	}
	
	/**
	 * Creates an AnsiColorLogWrapper with a custom LogLevel
	 *
	 * @param level the minimum LogLevel to print
	 */
	public AnsiColorLogWrapper(@NotNull Log.LogLevel level) {
		this(level, TRACE_PREFIX, DATA_PREFIX, INFO_PREFIX, WARN_PREFIX, ERROR_PREFIX, ASSERT_PREFIX);
	}
	
	/**
	 * Creates an AnsiColorLogWrapper with a custom LogLevel and color code prefix for each log level
	 * 
	 * @param level the minimum log level to target (default: TRACE)
	 * @param tracePrefix the prefix to use for trace logging
	 * @param dataPrefix the prefix to use for data logging
	 * @param infoPrefix the prefix to use for info logging
	 * @param warnPrefix the prefix to use for warning logging
	 * @param errorPrefix the prefix to use for error logging
	 * @param assertPrefix the prefix to use for assert logging
	 */
	public AnsiColorLogWrapper(@NotNull LogLevel level, @NotNull String tracePrefix, @NotNull String dataPrefix, @NotNull String infoPrefix, @NotNull String warnPrefix, @NotNull String errorPrefix, @NotNull String assertPrefix) {
		this.level = level;
		this.tracePrefix = tracePrefix;
		this.dataPrefix = dataPrefix;
		this.infoPrefix = infoPrefix;
		this.warnPrefix = warnPrefix;
		this.errorPrefix = errorPrefix;
		this.assertPrefix = assertPrefix;
	}
	
	@Override
	public void onLog(@NotNull Log.LogLevel level, @NotNull String str) {
		if (this.level.compareTo(level) > 0)
			return;
		String colorCode;
		switch (level) {
			default:
			case TRACE:		colorCode = tracePrefix; break;
			case DATA:		colorCode = dataPrefix; break;
			case INFO:		colorCode = infoPrefix; break;
			case WARN:		colorCode = warnPrefix; break;
			case ERROR:		colorCode = errorPrefix; break;
			case ASSERT:	colorCode = assertPrefix; break;
		}
		System.out.print(colorCode + str + "\r\n\033[0;39;49m");
	}
	
}
