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
package me.joshlarson.jlcommon.log.log_wrapper;

import me.joshlarson.jlcommon.log.Log;
import me.joshlarson.jlcommon.log.Log.LogLevel;
import me.joshlarson.jlcommon.log.LogWrapper;

import javax.annotation.Nonnull;

public class ConsoleLogWrapper implements LogWrapper {
	
	private final Log.LogLevel level;
	
	/**
	 * Creates a ConsoleLogWrapper with a LogLevel of TRACE
	 */
	public ConsoleLogWrapper() {
		this(LogLevel.TRACE);
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
