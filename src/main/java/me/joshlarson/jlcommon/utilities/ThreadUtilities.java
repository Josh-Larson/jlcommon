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
package me.joshlarson.jlcommon.utilities;

import me.joshlarson.jlcommon.log.Log;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.concurrent.ThreadFactory;

public class ThreadUtilities {
	
	public static ThreadFactory newThreadFactory(@Nonnull String pattern) {
		return new CustomThreadFactory(pattern, Thread.NORM_PRIORITY);
	}
	
	public static ThreadFactory newThreadFactory(@Nonnull String pattern, @Nonnegative int priority) {
		return new CustomThreadFactory(pattern, priority);
	}
	
	public static void safeRun(@Nonnull Runnable runnable) {
		try {
			runnable.run();
		} catch (Throwable t) {
			Log.e(t);
		}
	}
	
	private static class CustomThreadFactory implements ThreadFactory {
		
		private final String pattern;
		private final int priority;
		private int counter;
		
		public CustomThreadFactory(@Nonnull String pattern, @Nonnegative int priority) {
			this.pattern = pattern;
			this.priority = priority;
			this.counter = 0;
		}
		
		@Override
		@Nonnull
		public Thread newThread(@Nonnull Runnable r) {
			String name;
			if (pattern.contains("%d"))
				name = String.format(pattern, counter++);
			else
				name = pattern;
			Thread t = new Thread(r, name);
			t.setPriority(priority);
			return t;
		}
		
	}
	
}
