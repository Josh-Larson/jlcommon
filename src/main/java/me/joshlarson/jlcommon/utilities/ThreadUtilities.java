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
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;

public class ThreadUtilities {
	
	public static ThreadFactory newThreadFactory(@NotNull String pattern) {
		return new CustomThreadFactory(pattern, Thread.NORM_PRIORITY);
	}
	
	public static ThreadFactory newThreadFactory(@NotNull String pattern, int priority) {
		return new CustomThreadFactory(pattern, priority);
	}
	
	public static void safeRun(@NotNull Runnable runnable) {
		try {
			runnable.run();
		} catch (Throwable t) {
			Log.e(t);
		}
	}
	
	public static void printActiveThreads() {
		ThreadGroup nibbleThreadGroup = Thread.currentThread().getThreadGroup();
		int threadCount = nibbleThreadGroup.activeCount();
		Thread[] threadsRaw = new Thread[threadCount];
		threadCount = nibbleThreadGroup.enumerate(threadsRaw);
		Collection<Thread> threads = Arrays.stream(threadsRaw, 0, threadCount).filter(t -> t.getState() != Thread.State.TERMINATED).collect(Collectors.toList());
		int maxLength = threads.stream().mapToInt(t -> t.getName().length()).max().orElse(4);
		if (maxLength < 4)
			maxLength = 4;
		
		Log.w("Active Threads: %d", threads.size());
		Log.w("+-%s---%s-+", createRepeatingDash(maxLength), createRepeatingDash(13));
		Log.w("| %-" + maxLength + "s | %-13s |", "Name", "State");
		Log.w("+-%s-+-%s-+", createRepeatingDash(maxLength), createRepeatingDash(13));
		for (Thread t : threads) {
			Log.w("| %-" + maxLength + "s | %-13s |", t.getName(), t.getState());
		}
		Log.w("+-%s---%s-+", createRepeatingDash(maxLength), createRepeatingDash(13));
	}
	
	private static String createRepeatingDash(int count) {
		return String.join("", Collections.nCopies(count, "-"));
	}
	
	private static class CustomThreadFactory implements ThreadFactory {
		
		private final String pattern;
		private final int priority;
		private int counter;
		
		public CustomThreadFactory(@NotNull String pattern, int priority) {
			this.pattern = pattern;
			this.priority = priority;
			this.counter = 0;
		}
		
		@Override
		@NotNull
		public Thread newThread(@NotNull Runnable r) {
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
